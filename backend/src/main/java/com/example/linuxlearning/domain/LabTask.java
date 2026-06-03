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

@Entity
@Table(name = "lab_tasks")
public class LabTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 1200)
    private String instruction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CheckerType checkerType;

    @Column(nullable = false, length = 300)
    private String expected;

    @Column(nullable = false, length = 600)
    private String hint;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private int sortOrder;

    protected LabTask() {
    }

    public LabTask(String title, String instruction, CheckerType checkerType, String expected,
                   String hint, int score, int sortOrder) {
        this.title = title;
        this.instruction = instruction;
        this.checkerType = checkerType;
        this.expected = expected;
        this.hint = hint;
        this.score = score;
        this.sortOrder = sortOrder;
    }

    void setLab(Lab lab) {
        this.lab = lab;
    }

    public Long getId() {
        return id;
    }

    public Lab getLab() {
        return lab;
    }

    public String getTitle() {
        return title;
    }

    public String getInstruction() {
        return instruction;
    }

    public CheckerType getCheckerType() {
        return checkerType;
    }

    public String getExpected() {
        return expected;
    }

    public String getHint() {
        return hint;
    }

    public int getScore() {
        return score;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
