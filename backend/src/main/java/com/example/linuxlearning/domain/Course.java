package com.example.linuxlearning.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 600)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private PublishStatus status;

    @Column(nullable = false)
    private int sortOrder;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<CourseModule> modules = new ArrayList<>();

    protected Course() {
    }

    public Course(String title, String summary, PublishStatus status, int sortOrder) {
        this.title = title;
        this.summary = summary;
        this.status = status;
        this.sortOrder = sortOrder;
    }

    public void addModule(CourseModule module) {
        modules.add(module);
        module.setCourse(this);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public PublishStatus getStatus() {
        return status;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public List<CourseModule> getModules() {
        return modules;
    }
}
