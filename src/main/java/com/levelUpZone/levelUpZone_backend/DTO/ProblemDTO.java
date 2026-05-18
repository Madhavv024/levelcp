package com.levelUpZone.levelUpZone_backend.DTO;

import java.time.LocalDateTime;

public class ProblemDTO {
    private Integer contestId;
    private String problemInd;
    private LocalDateTime solvedTime;

    public Integer getContestId() {
        return contestId;
    }

    public void setContestId(Integer contestId) {
        this.contestId = contestId;
    }

    public String getProblemInd() {
        return problemInd;
    }

    public void setProblemInd(String problemInd) {
        this.problemInd = problemInd;
    }

    public LocalDateTime getSolvedTime() {
        return solvedTime;
    }

    public void setSolvedTime(LocalDateTime solvedTime) {
        this.solvedTime = solvedTime;
    }
}
