package com.example.linuxlearning.service;

import com.example.linuxlearning.common.BadRequestException;
import com.example.linuxlearning.common.NotFoundException;
import com.example.linuxlearning.domain.Lab;
import com.example.linuxlearning.domain.LabSession;
import com.example.linuxlearning.domain.LabSessionStatus;
import com.example.linuxlearning.domain.LabTask;
import com.example.linuxlearning.domain.LearningPath;
import com.example.linuxlearning.domain.PublishStatus;
import com.example.linuxlearning.domain.Submission;
import com.example.linuxlearning.domain.UserAccount;
import com.example.linuxlearning.dto.CheckTaskRequest;
import com.example.linuxlearning.dto.CheckTaskResponse;
import com.example.linuxlearning.dto.LabSessionResponse;
import com.example.linuxlearning.dto.StartDefaultLabSessionRequest;
import com.example.linuxlearning.repository.LabRepository;
import com.example.linuxlearning.repository.LabSessionRepository;
import com.example.linuxlearning.repository.LabTaskRepository;
import com.example.linuxlearning.repository.SubmissionRepository;
import com.example.linuxlearning.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LabSessionService {

    private final UserAccountRepository userAccountRepository;
    private final LabRepository labRepository;
    private final LabTaskRepository labTaskRepository;
    private final LabSessionRepository labSessionRepository;
    private final SubmissionRepository submissionRepository;
    private final VmSandboxClient vmSandboxClient;
    private final TaskCheckerService taskCheckerService;
    private final TerminalTranscriptService transcriptService;

    public LabSessionService(UserAccountRepository userAccountRepository,
                             LabRepository labRepository,
                             LabTaskRepository labTaskRepository,
                             LabSessionRepository labSessionRepository,
                             SubmissionRepository submissionRepository,
                             VmSandboxClient vmSandboxClient,
                             TaskCheckerService taskCheckerService,
                             TerminalTranscriptService transcriptService) {
        this.userAccountRepository = userAccountRepository;
        this.labRepository = labRepository;
        this.labTaskRepository = labTaskRepository;
        this.labSessionRepository = labSessionRepository;
        this.submissionRepository = submissionRepository;
        this.vmSandboxClient = vmSandboxClient;
        this.taskCheckerService = taskCheckerService;
        this.transcriptService = transcriptService;
    }

    @Transactional
    public LabSessionResponse startDefault(StartDefaultLabSessionRequest request) {
        LearningPath learningPath = request == null || request.learningPath() == null
                ? LearningPath.BEGINNER
                : request.learningPath();
        UserAccount user = resolveUser(learningPath);
        Lab lab = resolveDefaultLab(learningPath);
        return startSession(user, lab);
    }

    private LabSessionResponse startSession(UserAccount user, Lab lab) {
        SandboxAllocation allocation = vmSandboxClient.allocate(lab);
        OffsetDateTime now = OffsetDateTime.now();
        LabSession session = new LabSession(
                user,
                lab,
                allocation.vmId(),
                allocation.vmHost(),
                allocation.snapshotRef(),
                now,
                now.plusMinutes(lab.getTimeoutMinutes())
        );
        LabSession savedSession = labSessionRepository.save(session);
        transcriptService.clear(savedSession.getId());
        return toResponse(savedSession);
    }

    @Transactional(readOnly = true)
    public LabSessionResponse get(Long sessionId) {
        return toResponse(findSession(sessionId));
    }

    @Transactional
    public CheckTaskResponse check(Long sessionId, CheckTaskRequest request) {
        LabSession session = findSession(sessionId);
        if (session.getStatus() != LabSessionStatus.RUNNING) {
            throw new BadRequestException("当前实验会话不可判题");
        }
        LabTask task = labTaskRepository.findById(request.taskId())
                .orElseThrow(() -> new NotFoundException("任务不存在"));
        if (!task.getLab().getId().equals(session.getLab().getId())) {
            throw new BadRequestException("任务不属于当前实验");
        }
        CheckResult result = taskCheckerService.check(session, task, request.commandSummary());
        submissionRepository.save(new Submission(session, task, result.passed(), result.message(), nullToBlank(request.commandSummary())));
        int progressPercent = calculateProgress(session);
        session.markProgress(progressPercent);
        if (session.getStatus() == LabSessionStatus.PASSED) {
            vmSandboxClient.release(session.getVmId());
        }
        return new CheckTaskResponse(task.getId(), task.getCheckerType().name(), result.passed(), result.message(), progressPercent);
    }

    @Transactional
    public LabSessionResponse reset(Long sessionId) {
        LabSession session = findSession(sessionId);
        SandboxAllocation allocation = vmSandboxClient.reset(session.getVmId(), session.getLab());
        OffsetDateTime now = OffsetDateTime.now();
        session.reset(
                allocation.vmId(),
                allocation.vmHost(),
                allocation.snapshotRef(),
                now,
                now.plusMinutes(session.getLab().getTimeoutMinutes())
        );
        submissionRepository.deleteBySessionId(session.getId());
        transcriptService.clear(session.getId());
        return toResponse(session);
    }

    private UserAccount resolveUser(LearningPath learningPath) {
        return userAccountRepository.findFirstByLearningPathOrderByIdAsc(learningPath)
                .or(userAccountRepository::findFirstByOrderByIdAsc)
                .orElseThrow(() -> new NotFoundException("缺少默认学员"));
    }

    private Lab resolveDefaultLab(LearningPath learningPath) {
        return labRepository.findFirstByStatusAndLearningPathOrderByIdAsc(PublishStatus.PUBLISHED, learningPath)
                .orElseThrow(() -> new NotFoundException("缺少默认学习实验"));
    }

    private LabSession findSession(Long sessionId) {
        return labSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("实验会话不存在"));
    }

    private int calculateProgress(LabSession session) {
        List<LabTask> tasks = labTaskRepository.findByLabIdOrderBySortOrderAsc(session.getLab().getId());
        if (tasks.isEmpty()) {
            return 100;
        }
        Map<Long, Submission> latestSubmissionByTask = latestSubmissionByTask(session.getId());
        long passedCount = tasks.stream()
                .filter(task -> {
                    Submission submission = latestSubmissionByTask.get(task.getId());
                    return submission != null && submission.isPassed();
                })
                .count();
        return (int) Math.round(passedCount * 100.0 / tasks.size());
    }

    private LabSessionResponse toResponse(LabSession session) {
        Map<Long, Submission> latestSubmissionByTask = latestSubmissionByTask(session.getId());
        List<LabSessionResponse.TaskProgressView> taskViews = labTaskRepository.findByLabIdOrderBySortOrderAsc(session.getLab().getId())
                .stream()
                .map(task -> {
                    Submission submission = latestSubmissionByTask.get(task.getId());
                    return new LabSessionResponse.TaskProgressView(
                            task.getId(),
                            task.getTitle(),
                            task.getInstruction(),
                            task.getCheckerType().name(),
                            task.getHint(),
                            task.getScore(),
                            submission != null && submission.isPassed(),
                            submission == null ? "尚未检查" : submission.getMessage(),
                            submission == null ? null : submission.getCheckedAt()
                    );
                })
                .toList();
        return new LabSessionResponse(
                session.getId(),
                session.getLab().getId(),
                session.getLab().getTitle(),
                session.getStatus().name(),
                session.getVmId(),
                session.getVmHost(),
                session.getSnapshotRef(),
                session.getStartedAt(),
                session.getEndedAt(),
                session.getExpiresAt(),
                session.getProgressPercent(),
                taskViews
        );
    }

    private Map<Long, Submission> latestSubmissionByTask(Long sessionId) {
        Map<Long, Submission> latestSubmissionByTask = new HashMap<>();
        submissionRepository.findBySessionIdOrderByCheckedAtDesc(sessionId)
                .forEach(submission -> latestSubmissionByTask.putIfAbsent(submission.getTask().getId(), submission));
        return latestSubmissionByTask;
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }
}
