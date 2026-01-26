package com.missionqa.api.steps;

import com.missionqa.api.client.ReqResClient;
import com.missionqa.core.TestContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import io.restassured.response.Response;

import java.util.*;

import static org.testng.Assert.*;

public class ApiSteps {

        private final ReqResClient client = new ReqResClient();
        private final TestContext ctx = new TestContext();

        private static final String LAST_RESPONSE = "lastResponse";
        private static final String ALL_USER_IDS = "allUserIds";

        // -------------------------
        // LIST USERS
        // -------------------------

        @Given("^I get the default list of users for on 1st page$")
        public void iGetTheDefaultListofusers() {
                Response resp = client.listUsers(1);
                ctx.set(LAST_RESPONSE, resp);

                assertEquals(
                        resp.getStatusCode(),
                        200,
                        "Expected 200 for list users page 1. Body: " + resp.getBody().asString()
                );
        }

        @When("I get the list of all users within every page")
        public void iGetTheListOfAllUsers() {
                Response first = client.listUsers(1);
                assertEquals(first.getStatusCode(), 200, "Expected 200 for list users. Body: " + first.getBody().asString());

                Integer totalPages = first.jsonPath().getInt("total_pages");
                if (totalPages == null || totalPages < 1) totalPages = 1;

                Set<Integer> ids = new HashSet<>();

                for (int page = 1; page <= totalPages; page++) {
                        Response r = client.listUsers(page);
                        assertEquals(r.getStatusCode(), 200, "Expected 200 for list users page " + page + ". Body: " + r.getBody().asString());

                        List<Integer> pageIds = r.jsonPath().getList("data.id");
                        if (pageIds != null) ids.addAll(pageIds);

                        ctx.set(LAST_RESPONSE, r);
                }

                ctx.set(ALL_USER_IDS, ids);
        }

        @Then("I should see total users count equals the number of user ids")
        public void iShouldMatchTotalCount() {
                Response r = (Response) ctx.get(LAST_RESPONSE);
                if (r == null) r = client.listUsers(1);

                Integer total = r.jsonPath().getInt("total");
                assertNotNull(total, "API did not return 'total'");

                @SuppressWarnings("unchecked")
                Set<Integer> ids = (Set<Integer>) ctx.get(ALL_USER_IDS);
                assertNotNull(ids, "User IDs were not collected. Did the When step run?");

                assertEquals(ids.size(), total.intValue(), "Total users count should match unique user ids count");
        }

        // -------------------------
        // SINGLE USER
        // -------------------------

        @Given("I make a search for user {int}")
        public void iMakeASearchForUser(int userId) {
                Response resp = client.getUser(userId);
                ctx.set(LAST_RESPONSE, resp);
        }

        @Then("I should see the following user data")
        public void IShouldSeeFollowingUserData(DataTable dt) {
                Response resp = (Response) ctx.get(LAST_RESPONSE);
                assertNotNull(resp, "No response found in context");

                assertEquals(
                        resp.getStatusCode(),
                        200,
                        "Expected 200 for single user. Body: " + resp.getBody().asString()
                );

                // Feature format:
                // | first_name | email |
                // | Emma       | emma... |
                Map<String, String> expected = singleRowTableToMap(dt);

                expected.forEach((k, v) -> {
                        String actual = resp.jsonPath().getString("data." + k);
                        assertEquals(actual, v, "Mismatch for data." + k);
                });
        }

        @Then("I receive error code {int} in response")
        public void iReceiveErrorCodeInResponse(int responseCode) {
                Response resp = (Response) ctx.get(LAST_RESPONSE);
                assertNotNull(resp, "No response found in context");

                assertEquals(
                        resp.getStatusCode(),
                        responseCode,
                        "Unexpected status code. Body: " + resp.getBody().asString()
                );
        }

        // -------------------------
        // CREATE USER
        // -------------------------

        @Given("I create a user with following {word} {word}")
        public void iCreateUserWithFollowingWords(String name, String job) {
                iCreateUserWithFollowing(name, job);
        }

        @Given("I create a user with following {string} {string}")
        public void iCreateUserWithFollowing(String sUsername, String sJob) {
                Response resp = client.createUser(sUsername, sJob);
                ctx.set(LAST_RESPONSE, resp);

                assertEquals(
                        resp.getStatusCode(),
                        201,
                        "Expected 201 for create user. Body: " + resp.getBody().asString()
                );
        }

        @Then("response should contain the following data")
        public void responseShouldContainTheFollowingData(DataTable dt) {
                Response resp = (Response) ctx.get(LAST_RESPONSE);
                assertNotNull(resp, "No response found in context");

                // Feature format is header-only:
                // | name | job | id | createdAt |
                List<String> fields = dt.row(0);

                for (String field : fields) {
                        String key = field.trim();
                        Object raw = resp.jsonPath().get(key);
                        assertNotNull(raw, "Missing field in response: " + key + " Body: " + resp.getBody().asString());
                }
        }

        // -------------------------
        // LOGIN
        // -------------------------

        @Given("I login unsuccessfully with the following data")
        public void iLoginWithFollowingData(DataTable dt) {
                // Feature format:
                // | Email | Password |
                // | ...   | ...      |
                Map<String, String> creds = singleRowTableToMap(dt);

                String email = pickIgnoreCase(creds, "email", "Email");
                String password = pickIgnoreCase(creds, "password", "Password"); // may be "" (missing)

                // If empty string, send null to trigger missing-password stub
                if (password != null && password.trim().isEmpty()) {
                        password = null;
                }

                Response resp = client.login(email, password);
                ctx.set(LAST_RESPONSE, resp);
        }

        @Then("^I should get a response code of (\\d+)$")
        public void iShouldGetAResponseCodeOf(int responseCode) {
                Response resp = (Response) ctx.get(LAST_RESPONSE);
                assertNotNull(resp, "No response found in context");

                assertEquals(
                        resp.getStatusCode(),
                        responseCode,
                        "Unexpected status code. Body: " + resp.getBody().asString()
                );
        }

        @And("^I should see the following response message:$")
        public void iShouldSeeTheFollowingResponseMessage(DataTable dt) {
                Response resp = (Response) ctx.get(LAST_RESPONSE);
                assertNotNull(resp, "No response found in context");

                // Your feature currently uses:
                // | "error": "Missing password" |
                // That is a 1-cell row. We'll just assert the body contains that snippet.
                List<List<String>> rows = dt.asLists(String.class);
                assertFalse(rows.isEmpty(), "Expected at least one row in response message table");

                String expectedSnippet = rows.get(0).get(0).trim();
                String body = resp.getBody().asString();

                assertTrue(body.contains(expectedSnippet.replace("\"", "")) || body.contains(expectedSnippet),
                        "Expected response body to contain: " + expectedSnippet + " Body: " + body);
        }

        // -------------------------
        // DELAYED USERS
        // -------------------------

        @Given("^I wait for the user list to load$")
        public void iWaitForUserListToLoad() {
                Response resp = client.listUsersDelayed(3);
                ctx.set(LAST_RESPONSE, resp);

                assertEquals(
                        resp.getStatusCode(),
                        200,
                        "Expected 200 for delayed users. Body: " + resp.getBody().asString()
                );
        }

        @Then("I should see that every user has a unique id")
        public void iShouldSeeThatEveryUserHasAUniqueID() {
                Response resp = (Response) ctx.get(LAST_RESPONSE);
                assertNotNull(resp, "No response found in context");

                assertEquals(resp.getStatusCode(), 200, "Expected 200 response. Body: " + resp.getBody().asString());

                List<Integer> ids = resp.jsonPath().getList("data.id");
                assertNotNull(ids, "No user ids found in response");

                Set<Integer> unique = new HashSet<>(ids);
                assertEquals(unique.size(), ids.size(), "User IDs are not unique");
        }

        // -------------------------
        // Helpers
        // -------------------------

        private static Map<String, String> singleRowTableToMap(DataTable dt) {
                // Supports BOTH:
                // 1) header + one row:  | col1 | col2 | ... |
                //                       | v1   | v2   | ... |
                // 2) key/value rows:    | key | value |
                //                       | ... | ...   |
                List<List<String>> raw = dt.asLists(String.class);
                if (raw.isEmpty()) return Collections.emptyMap();

                // header + one row
                if (raw.size() >= 2 && raw.get(0).size() >= 2 && raw.get(1).size() == raw.get(0).size()) {
                        Map<String, String> map = new LinkedHashMap<>();
                        List<String> headers = raw.get(0);
                        List<String> values = raw.get(1);
                        for (int i = 0; i < headers.size(); i++) {
                                map.put(headers.get(i).trim(), values.get(i) == null ? null : values.get(i).trim());
                        }
                        return map;
                }

                // key/value rows
                Map<String, String> map = new LinkedHashMap<>();
                for (List<String> row : raw) {
                        if (row.size() >= 2) {
                                String k = row.get(0) == null ? null : row.get(0).trim();
                                String v = row.get(1) == null ? null : row.get(1).trim();
                                if (k != null && !k.isEmpty()) map.put(k, v);
                        }
                }
                return map;
        }

        private static String pickIgnoreCase(Map<String, String> map, String... keys) {
                for (String k : keys) {
                        if (map.containsKey(k)) return map.get(k);
                        for (String actualKey : map.keySet()) {
                                if (actualKey != null && actualKey.equalsIgnoreCase(k)) {
                                        return map.get(actualKey);
                                }
                        }
                }
                return null;
        }
}
