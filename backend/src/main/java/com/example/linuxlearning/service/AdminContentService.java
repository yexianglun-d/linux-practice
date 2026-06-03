package com.example.linuxlearning.service;

import com.example.linuxlearning.common.BadRequestException;
import com.example.linuxlearning.common.NotFoundException;
import com.example.linuxlearning.domain.Course;
import com.example.linuxlearning.domain.CourseModule;
import com.example.linuxlearning.domain.Lab;
import com.example.linuxlearning.domain.LabTask;
import com.example.linuxlearning.domain.Lesson;
import com.example.linuxlearning.domain.PublishStatus;
import com.example.linuxlearning.domain.SandboxTier;
import com.example.linuxlearning.dto.CreateCourseRequest;
import com.example.linuxlearning.dto.CreateLabRequest;
import com.example.linuxlearning.dto.CreateTaskRequest;
import com.example.linuxlearning.dto.LearningProgressResponse;
import com.example.linuxlearning.repository.CourseRepository;
import com.example.linuxlearning.repository.LabRepository;
import com.example.linuxlearning.repository.LabSessionRepository;
import com.example.linuxlearning.repository.LabTaskRepository;
import com.example.linuxlearning.repository.LessonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminContentService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final LabRepository labRepository;
    private final LabTaskRepository labTaskRepository;
    private final LabSessionRepository labSessionRepository;

    public AdminContentService(CourseRepository courseRepository,
                               LessonRepository lessonRepository,
                               LabRepository labRepository,
                               LabTaskRepository labTaskRepository,
                               LabSessionRepository labSessionRepository) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.labRepository = labRepository;
        this.labTaskRepository = labTaskRepository;
        this.labSessionRepository = labSessionRepository;
    }

    @Transactional
    public Long createCourse(CreateCourseRequest request) {
        Course course = new Course(request.title(), request.summary(), PublishStatus.PUBLISHED, nextCourseSortOrder());
        CourseModule module = new CourseModule(
                blankToDefault(request.initialModuleTitle(), "新建阶段"),
                "通过后台创建的课程阶段",
                1
        );
        Lesson lesson = new Lesson(
                blankToDefault(request.initialLessonTitle(), "新建实验课时"),
                "等待补充实验目标",
                30,
                1
        );
        module.addLesson(lesson);
        course.addModule(module);
        return courseRepository.save(course).getId();
    }

    @Transactional
    public Long createLab(CreateLabRequest request) {
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new NotFoundException("课时不存在"));
        if (lesson.getLab() != null) {
            throw new BadRequestException("该课时已经配置实验");
        }
        SandboxTier sandboxTier = request.sandboxTier() == null ? SandboxTier.VM : request.sandboxTier();
        Lab lab = new Lab(
                request.title(),
                request.description(),
                request.imageRef(),
                request.cpuCores(),
                request.memoryMb(),
                request.timeoutMinutes(),
                request.networkWhitelist(),
                sandboxTier,
                PublishStatus.PUBLISHED
        );
        lesson.assignLab(lab);
        return lessonRepository.save(lesson).getLab().getId();
    }

    @Transactional
    public Long createTask(CreateTaskRequest request) {
        Lab lab = labRepository.findById(request.labId())
                .orElseThrow(() -> new NotFoundException("实验不存在"));
        int sortOrder = labTaskRepository.findByLabIdOrderBySortOrderAsc(lab.getId()).size() + 1;
        LabTask task = new LabTask(
                request.title(),
                request.instruction(),
                request.checkerType(),
                request.expected(),
                request.hint(),
                request.score(),
                sortOrder
        );
        lab.addTask(task);
        return labRepository.save(lab).getTasks().stream()
                .filter(savedTask -> savedTask.getSortOrder() == sortOrder)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("任务创建失败"))
                .getId();
    }

    @Transactional(readOnly = true)
    public LearningProgressResponse learningProgress() {
        return new LearningProgressResponse(
                labSessionRepository.findTop30ByOrderByStartedAtDesc()
                        .stream()
                        .map(session -> new LearningProgressResponse.ProgressRow(
                                session.getId(),
                                session.getUser().getUsername(),
                                session.getUser().getDisplayName(),
                                session.getLab().getTitle(),
                                session.getStatus().name(),
                                session.getProgressPercent(),
                                session.getStartedAt(),
                                session.getExpiresAt()
                        ))
                        .toList()
        );
    }

    private int nextCourseSortOrder() {
        return (int) courseRepository.count() + 1;
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
