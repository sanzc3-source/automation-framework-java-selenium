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

Type: Automation / BDD Configuration
**Risk:** Medium

**Evidence:**
Cucumber failed at runtime with a DuplicateStepDefinitionException due to multiple step annotations (@Given and @When) mapped to the same step text:
"I add these items to the cart {string}"

**Impact:**
- Entire UI test suite failed before execution
- Blocked scenario execution and CI feedback
- Produced misleading “undefined step” errors
- Increased debugging time and reduced framework reliability

**Fix:**
- Standardized the step definition to use a single keyword (@When)
- Updated all feature files to consistently use the same step wording
- Removed redundant step annotations to enforce one-to-one step mapping

**Verification:**
- mvn clean test executes successfully
- All UI scenarios pass consistently
- No duplicate or undefined step warnings
- Step definitions are deterministic and easy to maintain

---
