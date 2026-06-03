package com.example.linuxlearning.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule module;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 600)
    private String objective;

    @Column(nullable = false)
    private int durationMinutes;

    @Column(nullable = false)
    private int sortOrder;

    @OneToOne(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private Lab lab;

    protected Lesson() {
    }

    public Lesson(String title, String objective, int durationMinutes, int sortOrder) {
        this.title = title;
        this.objective = objective;
        this.durationMinutes = durationMinutes;
        this.sortOrder = sortOrder;
    }

    public void assignLab(Lab lab) {
        this.lab = lab;
        lab.setLesson(this);
    }

    void setModule(CourseModule module) {
        this.module = module;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getObjective() {
        return objective;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public Lab getLab() {
        return lab;
    }
}
