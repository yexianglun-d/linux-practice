package com.example.linuxlearning.service;

import com.example.linuxlearning.domain.CheckerType;
import com.example.linuxlearning.domain.LabSession;
import com.example.linuxlearning.domain.LabTask;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class TaskCheckerService {

    private final TerminalTranscriptService transcriptService;

    public TaskCheckerService(TerminalTranscriptService transcriptService) {
        this.transcriptService = transcriptService;
    }

    public CheckResult check(LabSession session, LabTask task, String commandSummary) {
        String combinedEvidence = (transcriptService.summarize(session.getId()) + "\n" + nullToBlank(commandSummary))
                .toLowerCase(Locale.ROOT);
        String expected = task.getExpected().toLowerCase(Locale.ROOT);

        // 本地默认实现基于终端 transcript 做可运行判题；真实 VM 适配器应改为在沙箱内执行检查脚本。
        boolean passed = switch (task.getCheckerType()) {
            case OUTPUT_CONTAINS -> combinedEvidence.contains(expected);
            case FILE_EXISTS -> combinedEvidence.contains(expected) || combinedEvidence.contains("created " + expected);
            case SERVICE_ACTIVE -> combinedEvidence.contains(expected) || serviceLooksActive(task, combinedEvidence);
        };

        if (passed) {
            return new CheckResult(true, "检查通过：" + task.getTitle());
        }
        return new CheckResult(false, failureMessage(task));
    }

    private boolean serviceLooksActive(LabTask task, String evidence) {
        return task.getCheckerType() == CheckerType.SERVICE_ACTIVE
                && evidence.contains("active (running)")
                && evidence.contains("systemctl status");
    }

    private String failureMessage(LabTask task) {
        return "未检测到预期结果 `" + task.getExpected() + "`，可查看提示后重新执行命令。";
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }
}
