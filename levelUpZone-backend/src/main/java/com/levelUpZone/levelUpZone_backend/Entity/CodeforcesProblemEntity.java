package com.levelUpZone.levelUpZone_backend.Entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.OffsetDateTime;


@Entity
@Table(name = "codeforces_problems")
public class CodeforcesProblemEntity implements Serializable {

    private static final long serialVersionUID = -5677854825959523175L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cf_contest_id")
    private Integer cfContestId;

    @Column(name = "cf_problem_id")
    private String cfProblemId;

    @Column(name = "problem_name")
    private String problemName;

    @Column(name = "problem_rating")
    private Integer problemRating;

    @Column(name = "problem_tags", columnDefinition = "text")
    private String problemTags;

    @Column(name = "cf_problem_solve_count")
    private Integer cfProblemSolvedCount;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "version")
    private Integer version;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCfContestId() {
        return cfContestId;
    }

    public void setCfContestId(Integer cfContestId) {
        this.cfContestId = cfContestId;
    }

    public String getCfProblemId() {
        return cfProblemId;
    }

    public void setCfProblemId(String cfProblemId) {
        this.cfProblemId = cfProblemId;
    }

    public String getProblemName() {
        return problemName;
    }

    public void setProblemName(String problemName) {
        this.problemName = problemName;
    }

    public Integer getProblemRating() {
        return problemRating;
    }

    public void setProblemRating(Integer problemRating) {
        this.problemRating = problemRating;
    }

    public String getProblemTags() {
        return problemTags;
    }

    public void setProblemTags(String problemTags) {
        this.problemTags = problemTags;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getCfProblemSolvedCount() {
        return cfProblemSolvedCount;
    }

    public void setCfProblemSolvedCount(Integer cfProblemSolvedCount) {
        this.cfProblemSolvedCount = cfProblemSolvedCount;
    }
}
