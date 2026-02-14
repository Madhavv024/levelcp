package com.levelUpZone.levelUpZone_backend.DTO;

public class ProblemDTO {
    private Integer contestId;
    private String problemInd;

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
}
