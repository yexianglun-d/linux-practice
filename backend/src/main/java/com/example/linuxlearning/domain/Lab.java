package com.example.linuxlearning.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "labs")
public class Lab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false, unique = true)
    private Lesson lesson;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 1200)
    private String description;

    @Column(nullable = false, length = 120)
    private String imageRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private LearningPath learningPath;

    @Column(nullable = false)
    private int cpuCores;

    @Column(nullable = false)
    private int memoryMb;

    @Column(nullable = false)
    private int timeoutMinutes;

    @Column(nullable = false, length = 500)
    private String networkWhitelist;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private SandboxTier sandboxTier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private PublishStatus status;

    @OneToMany(mappedBy = "lab", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<LabTask> tasks = new ArrayList<>();

    protected Lab() {
    }

    public Lab(String title, String description, String imageRef, LearningPath learningPath, int cpuCores, int memoryMb,
               int timeoutMinutes, String networkWhitelist, SandboxTier sandboxTier, PublishStatus status) {
        this.title = title;
        this.description = description;
        this.imageRef = imageRef;
        this.learningPath = learningPath;
        this.cpuCores = cpuCores;
        this.memoryMb = memoryMb;
        this.timeoutMinutes = timeoutMinutes;
        this.networkWhitelist = networkWhitelist;
        this.sandboxTier = sandboxTier;
        this.status = status;
    }

    public void addTask(LabTask task) {
        tasks.add(task);
        task.setLab(this);
    }

    void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageRef() {
        return imageRef;
    }

    public LearningPath getLearningPath() {
        return learningPath;
    }

    public int getCpuCores() {
        return cpuCores;
    }

    public int getMemoryMb() {
        return memoryMb;
    }

    public int getTimeoutMinutes() {
        return timeoutMinutes;
    }

    public String getNetworkWhitelist() {
        return networkWhitelist;
    }

    public SandboxTier getSandboxTier() {
        return sandboxTier;
    }

    public PublishStatus getStatus() {
        return status;
    }

    public List<LabTask> getTasks() {
        return tasks;
    }
}
