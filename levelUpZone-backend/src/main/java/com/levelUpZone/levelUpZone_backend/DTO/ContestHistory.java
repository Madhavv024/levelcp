package com.levelUpZone.levelUpZone_backend.DTO;

import java.time.OffsetDateTime;
import java.util.List;

public class ContestHistory {

    private Long id;
    private OffsetDateTime  date;
    private String topic;
    private Integer level;
    private List<ProblemDTO> problems;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public List<ProblemDTO> getProblems() {
        return problems;
    }

    public void setProblems(List<ProblemDTO> problems) {
        this.problems = problems;
    }
}
