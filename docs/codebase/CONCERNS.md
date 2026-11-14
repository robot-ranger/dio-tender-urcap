# Codebase Concerns

All items verified from source; severity is my judgment. `SRC` = `com.processrobotics.dioTender/src/main/java/com/processrobotics/dioTender`.

## Core Sections (Required)

### 1) Top Risks

| # | Severity | Concern | Evidence | Suggested action |
|---|----------|---------|----------|------------------|
| 1 | High | No automated tests for script generation, where a bad string becomes a robot-controller syntax error (the code already works around one: `runThread` emits invalid `run m21_watch`). | `DioTenderInstallationNodeContribution.java:~501`; no `src/test` | Add unit tests for `IoOption`, `IoScript`, `generateScript` |
| 2 | High | Root node guard `if m21_watch_thrd != "":` compares a thread handle to a string, and `m21_watch_thrd` is only assigned when enabled+mapped. Semantics in URScript are unverified. | `DioTenderRootProgramNodeContribution.java:~783`, installation `generateScript` | [ASK USER] / verify on URSim |
| 3 | Medium | Door and Vise nodes report `isDefined()` on `isMapped()` only, not `isEnabled()`; when disabled, helpers (`fail`, `SIG_*`, `DOOR_TIMEOUT`) are still emitted only if mapped, but the watcher is not started. Door/Vise nodes use `fail()`/`DOOR_TIMEOUT` that exist only when mapped. | `DoorProgramNodeContribution.java`, `ViseProgramNodeContribution.java` | Confirm intended behavior |
| 4 | Medium | Hard-coded gripper outputs `G1=0`, `G2=1` and timings (`VISE_TIME`, `MFIN_PULSE`, `DOOR_TIMEOUT`) with no UI to change; Vise node has its own dwell default 1.0 duplicating `VISE_TIME`. | installation `writeConstants`; `ViseProgramNodeContribution.java` | [ASK USER] |
| 5 | Medium | Committed plaintext credentials/IP for robot and sim VM. | `pom.xml:33-44` | Move to `~/.m2/settings.xml` profile or CLI `-D` |
| 6 | Medium | Build uses `cp` and `sshpass`, not available natively on Windows (dev machine is Windows). | `pom.xml` exec plugin | Document or switch to `maven-antrun`/`maven-resources` |
| 7 | Low | Stale naming after rename (M21→Cycle Complete, MFin→Cycle Start): enum names, `SIG_M21/SIG_MFIN`, node title "Wait for M21", info text, thread `m21_watch`. Javadoc "all seven signals are mapped to a Modbus signal" is stale since digital I/O is supported. | `Signal.java`, `WaitForMachineProgramNodeContribution.java`, `DioTenderRootProgramNodeService.java` | [ASK USER] whether script var names must stay (existing Script nodes use them) |
| 8 | Low | Placeholder metadata: `urcap.copyright`="Copyright notice (C)", `licenseType`="License type", `META-INF/LICENSE` is template text; version `1.0-SNAPSHOT`. | `pom.xml`, `META-INF/LICENSE` | Fill before shipping |
| 9 | Low | `V3Style` path selected for PolyScope major < 5, but `urcap.compatibility.CB3=false`. | `DioTenderInstallationNodeService.java:268`, `pom.xml` | [ASK USER] keep or remove |
| 10 | Low | `printStackTrace()` for tree errors; repeated style-selection boilerplate in every service. | `program/*/…Service.java` | Optional cleanup |
| 11 | Low | The two `urscript_reference` files use different signal names/model than the URCap (e.g. `"m21"`, `"openVise1"`); `processrobotics.script` is a 93 KB export. | `urscript_reference/` | [ASK USER] still needed? |

### 2) High-Churn Files (90 days, from git)

`installation/DioTenderInstallationNodeContribution.java` (4), `installation/DioTenderInstallationNodeView.java` (4), `installation/Signal.java` (3), `program/root/DioTenderRootProgramNodeContribution.java` (3). The history is only 5 commits, so churn is weak evidence; the installation node is also the largest logic file (~370 lines) and holds mapping, I/O lookup, option building and script generation together.

### 3) TODO/FIXME in Production Code

None found in project source (only matches were inside the skill's own `scan.py`).

### 4) Test-code debt

N/A — no tests exist.

### 5) Evidence

- Files cited per row; `docs/codebase/.codebase-scan.txt`; git log (`e18b3b2`, `8e1a0d1`)
