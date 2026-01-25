package com.missionqa.api.client;

import io.restassured.response.Response;

/**
 * Generic API response wrapper.
 * Keeps raw RestAssured response and status code together.
 */
public class ApiResponse {

    private final Response response;
    private final int statusCode;

    public ApiResponse(Response response) {
        this.response = response;
        this.statusCode = response.getStatusCode();
    }

    public Response getResponse() {
        return response;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBodyAsString() {
        return response.getBody().asString();
    }
}
