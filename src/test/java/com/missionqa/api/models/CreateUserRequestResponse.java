package com.missionqa.api.models;

/**
 * Model for POST /api/users response (ReqRes).
 *
 * Example response:
 * {
 *   "name": "morpheus",
 *   "job": "leader",
 *   "id": "123",
 *   "createdAt": "2024-01-01T00:00:00.000Z"
 * }
 */
public class CreateUserRequestResponse {

    private String name;
    private String job;
    private String id;
    private String createdAt;

    public String getName() {
        return name;
    }

    public String getJob() {
        return job;
    }

    public String getId() {
        return id;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
