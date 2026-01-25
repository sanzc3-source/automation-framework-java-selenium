package com.missionqa.api.models;

public class CreateUserRequest {
    public String name;
    public String job;

    public CreateUserRequest(String name, String job) {
        this.name = name;
        this.job = job;
    }
}
