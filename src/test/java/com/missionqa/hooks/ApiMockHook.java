package com.missionqa.hooks;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.After;
import io.cucumber.java.Before;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class ApiMockHook {

    private static WireMockServer wm;

    @Before("@api")
    public void startMock() {
        // Start WireMock on a random free port (avoids conflicts)
        wm = new WireMockServer(options().dynamicPort());
        wm.start();

        // Point WireMock client (stubFor) at the actual running port
        configureFor("localhost", wm.port());

        // Point your API client base URL to WireMock
        // Requires TestConfig.getProperty() to respect System properties (we updated that).
        System.setProperty("api.baseUrl", "http://localhost:" + wm.port());

        // ----------------------------
        // STUBS
        // ----------------------------

        // LIST USERS page 1
        stubFor(get(urlPathEqualTo("/api/users"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "page": 1,
                                  "per_page": 6,
                                  "total": 12,
                                  "total_pages": 2,
                                  "data": [
                                    { "id": 1, "email": "george.bluth@reqres.in", "first_name": "George", "last_name": "Bluth" },
                                    { "id": 2, "email": "janet.weaver@reqres.in", "first_name": "Janet", "last_name": "Weaver" },
                                    { "id": 3, "email": "emma.wong@reqres.in", "first_name": "Emma", "last_name": "Wong" }
                                  ]
                                }
                                """)));

        // LIST USERS page 2
        stubFor(get(urlPathEqualTo("/api/users"))
                .withQueryParam("page", equalTo("2"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "page": 2,
                                  "per_page": 6,
                                  "total": 12,
                                  "total_pages": 2,
                                  "data": [
                                    { "id": 4, "email": "eve.holt@reqres.in", "first_name": "Eve", "last_name": "Holt" },
                                    { "id": 5, "email": "charles.morris@reqres.in", "first_name": "Charles", "last_name": "Morris" },
                                    { "id": 6, "email": "tracey.ramos@reqres.in", "first_name": "Tracey", "last_name": "Ramos" },
                                    { "id": 7, "email": "michael.lawson@reqres.in", "first_name": "Michael", "last_name": "Lawson" },
                                    { "id": 8, "email": "lindsay.ferguson@reqres.in", "first_name": "Lindsay", "last_name": "Ferguson" },
                                    { "id": 9, "email": "tobias.funke@reqres.in", "first_name": "Tobias", "last_name": "Funke" },
                                    { "id": 10, "email": "byron.fields@reqres.in", "first_name": "Byron", "last_name": "Fields" },
                                    { "id": 11, "email": "george.edwards@reqres.in", "first_name": "George", "last_name": "Edwards" },
                                    { "id": 12, "email": "rachel.howell@reqres.in", "first_name": "Rachel", "last_name": "Howell" }
                                  ]
                                }
                                """)));

        // SINGLE USER 3
        stubFor(get(urlPathEqualTo("/api/users/3"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "data": {
                                    "id": 3,
                                    "email": "emma.wong@reqres.in",
                                    "first_name": "Emma",
                                    "last_name": "Wong"
                                  }
                                }
                                """)));

        // SINGLE USER 55 (NOT FOUND)
        stubFor(get(urlPathEqualTo("/api/users/55"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        // CREATE USER
        stubFor(post(urlPathEqualTo("/api/users"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "name": "DYNAMIC",
                                  "job": "DYNAMIC",
                                  "id": "123",
                                  "createdAt": "2026-01-26T10:00:00.000Z"
                                }
                                """)));

        // LOGIN SUCCESS (email + password present)
        stubFor(post(urlPathEqualTo("/api/login"))
                .withRequestBody(matchingJsonPath("$.email"))
                .withRequestBody(matchingJsonPath("$.password"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                { "token": "fake-token-123" }
                                """)));

        // LOGIN MISSING PASSWORD (email present, password NOT present)
        stubFor(post(urlPathEqualTo("/api/login"))
                .withRequestBody(matchingJsonPath("$.email"))
                .withRequestBody(notMatching("(?s).*\"password\"\\s*:.*"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                { "error": "Missing password" }
                                """)));

        // DELAYED USERS (GET /api/users?delay=3)
        stubFor(get(urlPathEqualTo("/api/users"))
                .withQueryParam("delay", equalTo("3"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "page": 1,
                                  "per_page": 6,
                                  "total": 6,
                                  "total_pages": 1,
                                  "data": [
                                    { "id": 1 }, { "id": 2 }, { "id": 3 }, { "id": 4 }, { "id": 5 }, { "id": 6 }
                                  ]
                                }
                                """)));
    }

    @After("@api")
    public void stopMock() {
        if (wm != null) {
            wm.stop();
            wm = null;
        }
        System.clearProperty("api.baseUrl");
    }
}
