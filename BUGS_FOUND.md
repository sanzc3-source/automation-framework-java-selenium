# Bugs & Issues Found – MissionQA Assessment

This document tracks issues discovered during setup, execution, and refactoring,
including root cause analysis and verification steps.

---

## Pre-flight Observations (Before Test Execution)

These items were identified during initial project inspection and environment setup.
They were documented prior to running the test suite and then confirmed or dismissed
based on execution results.

### Observation 1 – Build Output Directory Present (`target/`)
**Type:** Build Hygiene  
**Risk:** Low

The `target/` directory exists locally as a result of Maven compilation.
This directory should not be committed to source control and should be ignored via `.gitignore`.

**Result (Confirmed):** After running `mvn clean test`, `target/` is generated and appears as untracked.  
**Next action:** Keep `target/` ignored to prevent accidental commits.

---

### Observation 2 – Package Mismatch Between Source and Compiled Tests
**Type:** Test Configuration / Cucumber  
**Risk:** High

During initial inspection, compiled output appeared under:
`target/test-classes/qumu` while test source lives under `src/test/java/mission`.

**Result (Not Confirmed as Blocking):** `mvn clean test` successfully ran `mission.RunnerTest` and executed the feature files.  
**Note:** Earlier `qumu` artifacts may have been stale build output; revisit only if glue discovery issues appear later.

---

## Confirmed Issues (Based on `mvn clean test` Execution)

### Confirmed Issue 1 – All Cucumber steps are undefined / pending
**Type:** Test Implementation / Cucumber  
**Severity:** Blocking (prevents real validation)

#### Evidence
Cucumber execution reports:
- `10 Scenarios (10 undefined)`
- `34 Steps (34 undefined)`
  and prints step definition snippets that currently throw `PendingException()`.

#### Impact
The test run completes with `BUILD SUCCESS` but performs no real automation or assertions.

#### Next Action
Implement step definitions for:
- API scenarios (ReqRes / reqres.in)
- UI scenario (SauceDemo / saucedemo.com)

Refactor into reusable layers:
- API client layer for requests/responses + validation
- Page Object Model for UI actions and assertions
- Keep step definitions thin (orchestrate only)

---

### Confirmed Issue 2 – Reporting/Execution output folder is generated and untracked (`test-output/`)
**Type:** Build / Reporting Output  
**Severity:** Low

#### Evidence
After running `mvn clean test`, `test-output/` appears as untracked.

#### Impact
Can accidentally be committed and create noise in PRs/history.

#### Next Action
Ignore `test-output/` via `.gitignore` (recommended), or formalize it as a build artifact only (CI).
