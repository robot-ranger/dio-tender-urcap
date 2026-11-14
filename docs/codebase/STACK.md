# Technology Stack

Paths below are relative to the repo root. `SRC` = `com.processrobotics.dioTender/src/main/java/com/processrobotics/dioTender`.

## Core Sections (Required)

### 1) Runtime Summary

| Area | Value | Evidence |
|------|-------|----------|
| Primary language | Java (source/target 1.8) | `com.processrobotics.dioTender/pom.xml` (maven-compiler-plugin) |
| Secondary language | URScript, emitted as strings by the URCap at program generation time | `SRC/installation/DioTenderInstallationNodeContribution.java` |
| Runtime | OSGi bundle (`packaging=bundle`) loaded by Universal Robots PolyScope; URCap API `com.ur.urcap:api:1.12.0` | `pom.xml`, `SRC/impl/Activator.java` |
| UI toolkit | Java Swing (`SwingInstallationNodeView`, `SwingProgramNodeService`) | `SRC/installation/DioTenderInstallationNodeView.java` |
| Package/build | Maven (no wrapper found in repo) | `pom.xml` |
| Target robots | e-Series only (`urcap.compatibility.eSeries=true`, `CB3=false`) | `pom.xml` |

### 2) Production Dependencies

| Dependency | Version | Role | Evidence |
|------------|---------|------|----------|
| `com.ur.urcap:api` | 1.12.0 (scope `provided`) | PolyScope URCap API: nodes, DataModel, IOModel, ScriptWriter | `pom.xml` |
| `org.osgi:org.osgi.core` | 4.3.0 | `BundleActivator`/service registration | `pom.xml`, `SRC/impl/Activator.java` |

No other runtime libraries are declared.

### 3) Development Toolchain

| Tool | Purpose | Evidence |
|------|---------|----------|
| maven-bundle-plugin 2.4.0 | Generates OSGi MANIFEST (Bundle-Activator, URCap headers) | `pom.xml` |
| maven-compiler 3.6.0, jar 3.0.2, resources 3.0.2, install 2.5.2, surefire 2.22.1 | Standard build | `pom.xml` |
| exec-maven-plugin 1.1 | Copies the jar to `.urcap`; install profiles (see below) | `pom.xml` |
| JUnit 4.12 (test scope) | Declared, but no tests exist | `pom.xml`; no `src/test` directory |
| Linter/formatter | None configured | scan output (`docs/codebase/.codebase-scan.txt`) |
| CI/CD | None detected | scan output |

### 4) Key Commands

Derived from `pom.xml` (not executed during this analysis):

```bash
cd com.processrobotics.dioTender
mvn clean package            # builds target/dioTender-1.0-SNAPSHOT.jar and copies it to .urcap via `cp`
mvn install -Plocal          # cp jar to ${user.home}/.urcaps/
mvn install -Pursim          # cp jar to ${ursim.home}/.urcaps/ (default /ursim)
mvn install -Pursimvm        # sshpass+scp to the UR Sim VM
mvn install -Premote         # sshpass+scp to the robot, then `pkill java` to restart the UI
```

The `cp` and `sshpass` executables imply a POSIX shell environment; on Windows they need Git Bash/WSL equivalents. [TODO: confirm how the build is actually run on this Windows machine.]

### 5) Environment and Config

- Config sources: `pom.xml` properties only (`urcap.*`, `urcap.install.*`, `ursim.*`, `ursimvm.*`). No `.env` files.
- Required env vars: none found.
- Deployment target: PolyScope on a UR e-Series controller (or URSim).
- Runtime state is stored in the PolyScope installation/program `DataModel`, not files.

### 6) Evidence

- `com.processrobotics.dioTender/pom.xml`
- `com.processrobotics.dioTender/src/main/java/.../impl/Activator.java`
- `.gitignore`

## Extended Sections

- Reference-only artifacts (not built): `com.processrobotics.dioTender/urscript_reference/dual_grip_dual_vise_dio.urscript`, `processrobotics.script` (a PolyScope-exported program, 93 KB).
