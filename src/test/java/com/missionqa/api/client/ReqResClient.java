package com.missionqa.api.client;

import com.missionqa.config.TestConfig;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.http.ContentType.JSON;

public class ReqResClient {

    private final String baseUrl;

    public ReqResClient() {
        // Prefer config key "api.baseUrl". If not present, default to ReqRes.
        String cfg;
        try {
            cfg = TestConfig.getProperty("api.baseUrl");
        } catch (Exception e) {
            cfg = "https://reqres.in";
        }
        this.baseUrl = cfg;

        // RestAssured global defaults (safe for this small project)
        RestAssured.baseURI = this.baseUrl;
    }

    public Response listUsers(int page) {
        return RestAssured
                .given()
                .contentType(JSON)
                .when()
                .get("/api/users?page=" + page)
                .andReturn();
    }

    public Response listUsersDelayed(int delaySeconds) {
        return RestAssured
                .given()
                .contentType(JSON)
                .when()
                .get("/api/users?delay=" + delaySeconds)
                .andReturn();
    }

    public Response getUser(int userId) {
        return RestAssured
                .given()
                .contentType(JSON)
                .when()
                .get("/api/users/" + userId)
                .andReturn();
    }

    public Response createUser(String name, String job) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("job", job);

        return RestAssured
                .given()
                .contentType(JSON)
                .body(body)
                .when()
                .post("/api/users")
                .andReturn();
    }

    public Response login(String email, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        if (password != null && !password.trim().isEmpty()) {
            body.put("password", password);
        }

        return RestAssured
                .given()
                .contentType(JSON)
                .body(body)
                .when()
                .post("/api/login")
                .andReturn();
    }
}
