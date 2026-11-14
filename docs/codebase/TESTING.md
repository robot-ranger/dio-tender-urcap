# Testing

## Core Sections (Required)

### 1) Test Stack

- Declared: JUnit 4.12 (test scope) and maven-surefire-plugin 2.22.1 (`pom.xml`).
- Actual tests: **none**. `com.processrobotics.dioTender/src/test` does not exist; scan reports no testing/perf configs.
- CI: none detected.

### 2) Commands

```bash
cd com.processrobotics.dioTender
mvn test      # runs nothing today
```

### 3) How Behavior Is Validated Today

- Manual, on URSim/robot: the pom ships install profiles (`local`, `ursim`, `ursimvm`, `remote`) for this purpose.
- `urscript_reference/dual_grip_dual_vise_dio.urscript` header says to "test in URSim first" and that motion is stubbed. [TODO] confirm the manual test procedure with the team.

### 4) Coverage Gaps (candidates for unit tests, no PolyScope needed)

- `IoOption` (pin regex, `readExpression`/`writeStatement`, `asMissing`) and `IoScript` (invert logic) are pure string logic.
- `DioTenderInstallationNodeContribution.generateScript` branches (mapped/unmapped, enabled, inverted, digital vs Modbus) can be tested with a fake `ScriptWriter`/`DataModel`.
- `Signal` key stability (DataModel compatibility across versions).

### 5) Evidence

- `com.processrobotics.dioTender/pom.xml`, directory listing, `docs/codebase/.codebase-scan.txt`
