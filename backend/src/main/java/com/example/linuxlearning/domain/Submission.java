package com.example.linuxlearning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private LabSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private LabTask task;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CheckerType checkerType;

    @Column(nullable = false)
    private boolean passed;

    @Column(nullable = false, length = 600)
    private String message;

    @Column(nullable = false, length = 1000)
    private String commandSummary;

    @Column(nullable = false)
    private OffsetDateTime checkedAt;

    protected Submission() {
    }

    public Submission(LabSession session, LabTask task, boolean passed, String message, String commandSummary) {
        this.session = session;
        this.task = task;
        this.checkerType = task.getCheckerType();
        this.passed = passed;
        this.message = message;
        this.commandSummary = commandSummary;
        this.checkedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public LabTask getTask() {
        return task;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getMessage() {
        return message;
    }

    public OffsetDateTime getCheckedAt() {
        return checkedAt;
    }
}
