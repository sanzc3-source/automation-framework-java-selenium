# Bugs & Issues Found – MissionQA Assessment

This document tracks issues discovered during setup, execution, and refactoring,
including root cause analysis, impact, and verification steps.

---

## Pre-flight Observations (Before Test Execution)

These items were identified during initial project inspection and environment setup.
They were documented prior to running the test suite and then confirmed or dismissed
based on execution results.

---

### Observation 1 – Build Output Directory Present (`target/`)
**Type:** Build Hygiene  
**Risk:** Low

**Evidence:**  
The `target/` directory was present in the repository.

**Impact:**  
Generated build artifacts should not be committed to source control, as they can:
- Cause unnecessary repository bloat
- Lead to inconsistent local build states

**Fix:**
- Added `target/` to the root `.gitignore`
- Removed tracked build artifacts in a separate cleanup commit

**Verification:**
- `target/` is no longer tracked by Git
- Fresh builds regenerate artifacts locally only

---

## Execution & Refactor Findings

These issues were discovered during test execution, dependency updates,
and framework refactoring.

---

### Issue 2 – Duplicate and Conflicting REST Assured Dependencies
**Type:** Dependency Management  
**Risk:** Medium

**Evidence:**  
`pom.xml` declared multiple versions of REST Assured:
- `io.rest-assured:rest-assured:5.5.1` (test scope)
- `io.rest-assured:rest-assured:4.1.2` (compile scope)

**Impact:**
- Maven may resolve different versions unpredictably
- Risk of runtime classpath conflicts
- Unstable or hard-to-debug test behavior

**Fix:**
- Removed the older REST Assured dependency
- Standardized on a single REST Assured version
- Scoped REST Assured correctly to `test`

**Verification:**
- `mvn dependency:tree` shows a single REST Assured version
- Project builds and runs successfully

---

### Issue 3 – Outdated and Unnecessary Maven Dependencies
**Type:** Dependency Hygiene  
**Risk:** Low

**Evidence:**  
The project included Maven plugin internals as dependencies:
- `maven-core`
- `maven-project`
- `maven-artifact`
- `maven-plugin-api`

**Impact:**
- Bloated classpath
- Increased dependency complexity
- Not relevant to a test automation framework

**Fix:**
- Removed all Maven plugin-related dependencies
- Limited dependencies to test automation needs only

**Verification:**
- Maven build succeeds without these dependencies
- Dependency graph is significantly cleaner

---

### Issue 4 – Multiple and Obsolete Selenium Dependencies
**Type:** Dependency Management  
**Risk:** Medium

**Evidence:**  
The project declared multiple Selenium artifacts, including obsolete ones:
- `selenium-java`
- `selenium-server`
- `selenium-api`
- `selenium-htmlunit-driver`
- Legacy Selenium POM (`2.0rc2`)

**Impact:**
- High risk of dependency conflicts
- Use of unsupported or deprecated components
- Inconsistent WebDriver behavior

**Fix:**
- Retained a single canonical Selenium dependency (`selenium-java`)
- Removed redundant and obsolete Selenium artifacts
- Prepared project for Selenium 4.x upgrade

**Verification:**
- Selenium resolves cleanly
- No duplicate Selenium artifacts in dependency tree

---

### Issue 5 – Incorrect Dependency Scopes for Test Libraries
**Type:** Build Configuration  
**Risk:** Low

**Evidence:**  
Several test libraries were declared with `compile` scope (e.g., TestNG, REST Assured).

**Impact:**
- Pollutes main classpath unnecessarily
- Breaks separation between application code and test code

**Fix:**
- Updated all test-only libraries to use `test` scope

**Verification:**
- Maven build succeeds
- Dependencies are available only during test execution

---

### Issue 6 – Mixed UI and API Step Definitions in a Single Class
**Type:** Test Architecture  
**Risk:** Medium

**Evidence:**  
UI and API step definitions were implemented together in a single
`StepDefinition` class.

**Impact:**
- Poor separation of concerns
- Reduced readability and maintainability
- Harder to scale or extend tests

**Fix:**
- Split step definitions by domain:
  - `ApiSteps` for API scenarios
  - `UiSteps` for UI scenarios
- Updated Cucumber glue configuration accordingly

**Verification:**
- Cucumber discovers steps correctly
- Test structure aligns with BDD and POM best practices

---

### Issue 7 – Duplicate Cucumber Step Definitions
**Type:** Automation / BDD Configuration  
**Risk:** Medium

**Evidence:**  
Cucumber failed at runtime with a `DuplicateStepDefinitionException` due to
multiple step annotations mapped to the same step text:
- `"I add these items to the cart {string}"`

**Impact:**
- Entire UI test suite failed before execution
- Blocked scenario execution and CI feedback
- Produced misleading “undefined step” errors

**Fix:**
- Standardized the step definition to a single annotation
- Updated feature files to use consistent step wording
- Removed redundant step annotations

**Verification:**
- `mvn clean test` executes successfully
- UI scenarios pass consistently
- No duplicate or undefined step warnings

---

## API-Specific Findings & Stabilization Work

---

### Issue 8 – External API (ReqRes) Blocking Automated Requests
**Type:** Test Stability / External Dependency  
**Risk:** High

**Evidence:**  
All API tests failed with HTTP `403` responses. Responses returned HTML login
pages instead of JSON payloads, indicating upstream request blocking.

**Impact:**
- All API tests failed consistently
- API suite was non-deterministic
- CI execution would be unreliable or completely blocked
- Test failures were unrelated to application logic

**Root Cause:**
- ReqRes API introduced request filtering / protection (e.g., bot detection or rate limiting)
- Automated test requests were rejected

**Fix:**
- Introduced **WireMock** to simulate ReqRes API behavior
- Implemented `ApiMockHook` to:
  - Start WireMock on a dynamic port before API scenarios
  - Stub all required ReqRes endpoints
  - Override `api.baseUrl` at runtime
- Ensured API tests run against mocked responses only

**Verification:**
- API tests run consistently without external dependencies
- All API scenarios pass locally and are CI-safe
- No outbound network dependency required

---

### Issue 9 – Jackson Dependency Mismatch Causing Runtime Failures
**Type:** Dependency Resolution  
**Risk:** High

**Evidence:**  
API tests failed with:
NoClassDefFoundError: com.fasterxml.jackson.annotation.JsonSerializeAs


**Impact:**
- WireMock server failed to start
- Entire API test suite failed at runtime
- Errors were non-obvious and misleading

**Root Cause:**
- Incorrect Jackson version specified (`2.21.0`)
- Version did not exist in Maven Central
- Dependency resolution pulled incompatible Jackson artifacts

**Fix:**
- Corrected Jackson dependency to a valid version (`2.21`)
- Removed duplicate / conflicting Jackson declarations
- Cleared local Maven cache to force clean resolution

**Verification:**
- WireMock starts successfully
- API tests execute without Jackson-related errors

---

### Issue 10 – Cucumber DataTable Misuse in API Assertions
**Type:** Test Implementation  
**Risk:** Medium

**Evidence:**  
API assertions failed with:
UndefinedDataTableTypeException
ClassCastException: java.lang.String cannot be cast to char[]


**Impact:**
- API tests partially passed but failed during validation
- Reduced confidence in API assertions

**Root Cause:**
- DataTables were incorrectly converted to `List<String>`
- Table width required `Map<String, String>` semantics

**Fix:**
- Updated API step definitions to use `dataTable.asMap(String.class, String.class)`
- Validated response payloads using key-based assertions

**Verification:**
- All API assertions pass correctly
- Step definitions are clearer and more maintainable

---

### Issue 11 – Undefined Cucumber Steps Due to Over-Specific Step Text
**Type:** BDD Design  
**Risk:** Medium

**Evidence:**  
Cucumber reported undefined steps for:
- `"I create a user with following Peter Manager"`
- `"I create a user with following Liza Sales"`

**Impact:**
- Scenario Outline steps were not reusable
- Required redundant step definitions

**Fix:**
- Replaced hard-coded step text with parameterized step:
  - `I create a user with following {string} {string}`
- Consolidated logic into a single reusable step definition

**Verification:**
- Scenario Outlines execute correctly
- No undefined or duplicate steps
- API tests are fully data-driven

---

## Final State Summary

- UI and API tests pass consistently
- External dependencies eliminated from API execution
- Dependency graph cleaned and stabilized
- Framework structure aligned with best practices
- Project is CI-ready and deterministic

---
