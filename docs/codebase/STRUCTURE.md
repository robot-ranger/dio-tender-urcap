# Codebase Structure

`SRC` = `com.processrobotics.dioTender/src/main/java/com/processrobotics/dioTender`.

## Core Sections (Required)

### 1) Top-Level Map

| Path | Purpose | Evidence |
|------|---------|----------|
| `com.processrobotics.dioTender/` | The single Maven module (URCap bundle) | `pom.xml` |
| `com.processrobotics.dioTender/src/main/java/` | All Java source (34 files, ~2.6k lines) | file listing |
| `com.processrobotics.dioTender/src/main/resources/` | `img/process-robotics_transparent_bg_50px.png` (logo), `META-INF/LICENSE` (template placeholder text) | file listing |
| `com.processrobotics.dioTender/urscript_reference/` | Hand-written/exported URScript used as design reference; not packaged | file listing |
| `com.processrobotics.dioTender/target/` | Build output (git-ignored); do not document | `.gitignore` |
| `docs/codebase/` | This documentation | — |
| `.claude/skills/acquire-codebase-knowledge/` | Tooling used to produce these docs | — |

Not a monorepo (scan: "No monorepo signals detected"). No README, PRD or spec exists in the working tree; two plan files (`DOOR_VISE_NODES_PLAN.md`, `DOOR_VISE_NODES_EXECUTION_NOTES.md`) were deleted in commit `e18b3b2` ("old plans") and are recoverable from git history.

### 2) Package Layout (under `SRC`)

| Package | Contents |
|---------|----------|
| `impl/` | `Activator` – registers every service with OSGi |
| `installation/` | `DioTenderInstallationNode{Service,Contribution,View}`, `Signal` (enum of 7 logical signals), `IoOption` (one dropdown entry / I/O source) |
| `program/` | Shared helpers `IoScript`, `TemplateComments`, `InfoProgramNodeView` |
| `program/root`, `pickRaw`, `waitForMachine`, `tendSwap`, `unload`, `load`, `placeFinished` | Template program nodes forming the tending cycle (each = `*Service` + `*Contribution`) |
| `program/door`, `program/vise` | User-insertable Door Open/Close and Vise 1/2 Open/Close nodes (`*Service`, `*Contribution`, `*View`) |
| `style/` | `Style` + `V3Style`/`V5Style` (UI sizing per PolyScope major version), `LogoRow` |
| (root) | `Constants` – logo resource path |

### 3) Entry Points

- OSGi: `Bundle-Activator` = `com.processrobotics.dioTender.impl.Activator` (`pom.xml`, `SRC/impl/Activator.java:26`). It registers 1 installation node service and 9 program node services.
- No `main()`.

### 4) Where To Add Things

| Change | Location |
|--------|----------|
| New logical signal | Add a constant to `installation/Signal.java`; view and script constants iterate `Signal.values()` |
| New program node | New package under `program/` with Service+Contribution; register in `impl/Activator.java` |
| Shared script helper (URScript) | `writeHelpers` in `installation/DioTenderInstallationNodeContribution.java` |
| UI sizing/colors | `style/` |

### 5) Evidence

- `SRC/impl/Activator.java`, `pom.xml`, `.gitignore`, git log (`e18b3b2`)
