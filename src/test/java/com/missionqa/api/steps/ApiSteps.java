package com.missionqa.api.steps;

import com.missionqa.api.client.ReqResClient;
import com.missionqa.core.TestContext;
import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.restassured.response.Response;

import java.util.*;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

public class ApiSteps {

        private final ReqResClient client = new ReqResClient();
        private final TestContext ctx = new TestContext();

        // Keys for context storage
        private static final String LAST_RESPONSE = "lastResponse";
        private static final String ALL_USER_IDS = "allUserIds";

        @Given("^I get the default list of users for on 1st page$")
        public void iGetTheDefaultListofusers() {
                Response resp = client.listUsers(1);
                ctx.set(LAST_RESPONSE, resp);
                assertEquals(resp.getStatusCode(), 200, "Expected 200 for list users page 1");
        }

        @When("I get the list of all users within every page")
        public void iGetTheListOfAllUsers() {
                // ReqRes typically returns: { page, per_page, total, total_pages, data: [...] }
                // We'll fetch all pages and collect user IDs.
                Response first = client.listUsers(1);
                assertEquals(first.getStatusCode(), 200, "Expected 200 for list users");

                Integer totalPages = first.jsonPath().getInt("total_pages");
                if (totalPages == null || totalPages < 1) totalPages = 1;

                Set<Integer> ids = new HashSet<>();

                for (int page = 1; page <= totalPages; page++) {
                        Response r = client.listUsers(page);
                        assertEquals(r.getStatusCode(), 200, "Expected 200 for list users page " + page);

                        List<Integer> pageIds = r.jsonPath().getList("data.id");
                        if (pageIds != null) ids.addAll(pageIds);

                        // keep last response handy for the next Then if needed
                        ctx.set(LAST_RESPONSE, r);
                }

                ctx.set(ALL_USER_IDS, ids);
        }

        @Then("I should see total users count equals the number of user ids")
        public void iShouldMatchTotalCount() {
                // Compare "total" from API vs size of collected IDs
                // We'll read total from the last response or re-fetch page 1 (safe).
                Response r = (Response) ctx.get(LAST_RESPONSE);
                if (r == null) r = client.listUsers(1);

                Integer total = r.jsonPath().getInt("total");
                assertNotNull(total, "API did not return 'total'");

                @SuppressWarnings("unchecked")
                Set<Integer> ids = (Set<Integer>) ctx.get(ALL_USER_IDS);
                assertNotNull(ids, "User IDs were not collected. Did the When step run?");
                assertEquals(ids.size(), total.intValue(), "Total users count should match unique user ids count");
        }

        @Given("I make a search for user (.*)")
        public void iMakeASearchForUser(String sUserID) {
                int userId = Integer.parseInt(sUserID.trim());
                Response resp = client.getUser(userId);
                ctx.set(LAST_RESPONSE, resp);
        }

        @Then("I should see the following user data")
        public void IShouldSeeFollowingUserData(DataTable dt) {
                Response resp = (Response) ctx.get(LAST_RESPONSE);
                assertNotNull(resp, "No response found in context");
                assertEquals(resp.getStatusCode(), 200, "Expected 200 for single user");

                Map<String, String> expected = dataTableToMap(dt);

                // Example keys you might have in the feature table:
                // id, email, first_name, last_name
                expected.forEach((k, v) -> {
                        String actual = String.valueOf(resp.jsonPath().get("data." + k));
                        assertEquals(actual, v, "Mismatch for data." + k);
                });
        }

        @Then("I receive error code (.*) in response")
        public void iReceiveErrorCodeInResponse(int responseCode) {
                Response resp = (Response) ctx.get(LAST_RESPONSE);
                assertNotNull(resp, "No response found in context");
                assertEquals(resp.getStatusCode(), responseCode, "Unexpected status code");
        }

        @Given("I create a user with following (.*) (.*)")
        public void iCreateUserWithFollowing(String sUsername, String sJob) {
                Response resp = client.createUser(sUsername, sJob);
                ctx.set(LAST_RESPONSE, resp);
                assertEquals(resp.getStatusCode(), 201, "Expected 201 for create user");
        }

        @Then("response should contain the following data")
        public void responseShouldContainTheFollowingData(DataTable dt) {
                Response resp = (Response) ctx.get(LAST_RESPONSE);
                assertNotNull(resp, "No response found in context");

                Map<String, String> expected = dataTableToMap(dt);

                // This checks top-level fields in create user response (name, job, id, createdAt, etc.)
                expected.forEach((k, v) -> {
                        Object raw = resp.jsonPath().get(k);
                        assertNotNull(raw, "Missing field: " + k);

                        // If expected is "*", treat as "exists"
                        if (!"*".equals(v)) {
                                assertEquals(String.valueOf(raw), v, "Mismatch for field: " + k);
                        }
                });
        }

        @Given("I login unsuccessfully with the following data")
        public void iLoginSuccesfullyWithFollowingData(DataTable dt) {
                Map<String, String> creds = dataTableToMap(dt);

                // ReqRes login expects: email + password (password optional for unsuccessful scenario)
                String email = creds.getOrDefault("email", creds.getOrDefault("Email", ""));
                String password = creds.getOrDefault("password", creds.getOrDefault("Password", null));

                Response resp = client.login(email, password);
                ctx.set(LAST_RESPONSE, resp);
        }

        @Given("^I wait for the user list to load$")
        public void iWaitForUserListToLoad() {
                // ReqRes delayed endpoint: /api/users?delay=3
                Response resp = client.listUsersDelayed(3);
                ctx.set(LAST_RESPONSE, resp);
                assertEquals(resp.getStatusCode(), 200, "Expected 200 for delayed users");
        }

        @Then("I should see that every user has a unique id")
        public void iShouldSeeThatEveryUserHasAUniqueID() {
                Response resp = (Response) ctx.get(LAST_RESPONSE);
                assertNotNull(resp, "No response found in context");
                assertEquals(resp.getStatusCode(), 200, "Expected 200 response");

                List<Integer> ids = resp.jsonPath().getList("data.id");
                assertNotNull(ids, "No user ids found in response");

                Set<Integer> unique = new HashSet<>(ids);
                assertEquals(unique.size(), ids.size(), "User IDs are not unique");
        }

        @Then("^I should get a response code of (\\d+)$")
        public void iShouldGetAResponseCodeOf(int responseCode) {
                Response resp = (Response) ctx.get(LAST_RESPONSE);
                assertNotNull(resp, "No response found in context");
                assertEquals(resp.getStatusCode(), responseCode, "Unexpected status code");
        }

        @And("^I should see the following response message:$")
        public void iShouldSeeTheFollowingResponseMessage(DataTable dt) {
                Response resp = (Response) ctx.get(LAST_RESPONSE);
                assertNotNull(resp, "No response found in context");

                Map<String, String> expected = dataTableToMap(dt);

                // Most common ReqRes error field is "error"
                expected.forEach((k, v) -> {
                        String actual = String.valueOf(resp.jsonPath().get(k));
                        assertEquals(actual, v, "Mismatch for response field: " + k);
                });
        }

        // -------------------------
        // Helpers
        // -------------------------
        private Map<String, String> dataTableToMap(DataTable dt) {
                // Supports either:
                // | key | value |
                // or:
                // | key | value | (as header)
                List<List<String>> raw = dt.raw();
                if (raw.isEmpty()) return Collections.emptyMap();

                // If first row looks like headers: ["key","value"]
                boolean hasHeader = raw.get(0).size() >= 2 &&
                        raw.get(0).get(0).toLowerCase().contains("key");

                int start = hasHeader ? 1 : 0;

                Map<String, String> map = new LinkedHashMap<>();
                for (int i = start; i < raw.size(); i++) {
                        List<String> row = raw.get(i);
                        if (row.size() >= 2) {
                                map.put(row.get(0).trim(), row.get(1).trim());
                        }
                }
                return map;
        }
}
