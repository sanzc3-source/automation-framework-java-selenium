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
        String cfg;
        try {
            cfg = System.getProperty("api.baseUrl", TestConfig.getProperty("api.baseUrl"));

        } catch (Exception e) {
            cfg = "http://localhost:8080";
        }
        this.baseUrl = cfg;
    }

    // Centralized request defaults (helps avoid Cloudflare blocks)
    private io.restassured.specification.RequestSpecification request() {
        return RestAssured
                .given()
                .baseUri(baseUrl)
                .header("Accept", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36")
                .header("Accept-Language", "en-US,en;q=0.9");
    }

    public Response listUsers(int page) {
        return request()
                .when()
                .get("/api/users?page=" + page)
                .andReturn();
    }

    public Response listUsersDelayed(int delaySeconds) {
        return request()
                .when()
                .get("/api/users?delay=" + delaySeconds)
                .andReturn();
    }

    public Response getUser(int userId) {
        return request()
                .when()
                .get("/api/users/" + userId)
                .andReturn();
    }

    public Response createUser(String name, String job) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("job", job);

        return request()
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

        return request()
                .contentType(JSON)
                .body(body)
                .when()
                .post("/api/login")
                .andReturn();
    }
}
