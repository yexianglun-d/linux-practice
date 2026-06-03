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
@Table(name = "lab_sessions")
public class LabSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab;

    @Column(nullable = false, length = 80)
    private String vmId;

    @Column(nullable = false, length = 160)
    private String vmHost;

    @Column(nullable = false, length = 80)
    private String snapshotRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private LabSessionStatus status;

    @Column(nullable = false)
    private OffsetDateTime startedAt;

    private OffsetDateTime endedAt;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    @Column(nullable = false)
    private int progressPercent;

    protected LabSession() {
    }

    public LabSession(UserAccount user, Lab lab, String vmId, String vmHost, String snapshotRef,
                      OffsetDateTime startedAt, OffsetDateTime expiresAt) {
        this.user = user;
        this.lab = lab;
        this.vmId = vmId;
        this.vmHost = vmHost;
        this.snapshotRef = snapshotRef;
        this.status = LabSessionStatus.RUNNING;
        this.startedAt = startedAt;
        this.expiresAt = expiresAt;
        this.progressPercent = 0;
    }

    public void markProgress(int progressPercent) {
        this.progressPercent = progressPercent;
        if (progressPercent >= 100) {
            this.status = LabSessionStatus.PASSED;
            this.endedAt = OffsetDateTime.now();
        }
    }

    public void reset(String vmId, String vmHost, String snapshotRef, OffsetDateTime startedAt, OffsetDateTime expiresAt) {
        this.vmId = vmId;
        this.vmHost = vmHost;
        this.snapshotRef = snapshotRef;
        this.startedAt = startedAt;
        this.expiresAt = expiresAt;
        this.endedAt = null;
        this.status = LabSessionStatus.RUNNING;
        this.progressPercent = 0;
    }

    public Long getId() {
        return id;
    }

    public UserAccount getUser() {
        return user;
    }

    public Lab getLab() {
        return lab;
    }

    public String getVmId() {
        return vmId;
    }

    public String getVmHost() {
        return vmHost;
    }

    public String getSnapshotRef() {
        return snapshotRef;
    }

    public LabSessionStatus getStatus() {
        return status;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public OffsetDateTime getEndedAt() {
        return endedAt;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public int getProgressPercent() {
        return progressPercent;
    }
}
