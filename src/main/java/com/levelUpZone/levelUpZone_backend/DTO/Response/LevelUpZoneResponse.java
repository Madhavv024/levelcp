package com.levelUpZone.levelUpZone_backend.DTO.Response;

import java.time.OffsetDateTime;

public class LevelUpZoneResponse {

    private Integer id;
    private String username;
    private String email;
    private String codeforcesHandle;
    private Integer currentLevelId;
    private Integer maxRating;
    private Boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Private constructor to enforce builder usage
    private LevelUpZoneResponse(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.email = builder.email;
        this.codeforcesHandle = builder.codeforcesHandle;
        this.currentLevelId = builder.currentLevelId;
        this.maxRating = builder.maxRating;
        this.active = builder.active;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    // Getters only (optional: remove setters for immutability)

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getCodeforcesHandle() {
        return codeforcesHandle;
    }

    public Integer getCurrentLevelId() {
        return currentLevelId;
    }

    public Integer getMaxRating() {
        return maxRating;
    }

    public Boolean getActive() {
        return active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Static Builder class
    public static class Builder {

        private Integer id;
        private String username;
        private String email;
        private String codeforcesHandle;
        private Integer currentLevelId;
        private Integer maxRating;
        private Boolean active;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder codeforcesHandle(String codeforcesHandle) {
            this.codeforcesHandle = codeforcesHandle;
            return this;
        }

        public Builder currentLevelId(Integer currentLevelId) {
            this.currentLevelId = currentLevelId;
            return this;
        }

        public Builder maxRating(Integer maxRating) {
            this.maxRating = maxRating;
            return this;
        }

        public Builder active(Boolean active) {
            this.active = active;
            return this;
        }

        public Builder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public LevelUpZoneResponse build() {
            return new LevelUpZoneResponse(this);
        }
    }
}

