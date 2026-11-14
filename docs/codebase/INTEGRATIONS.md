# External Integrations

## Core Sections (Required)

### 1) Integration Inventory

| System | Type | Purpose | Auth | Evidence |
|--------|------|---------|------|----------|
| UR PolyScope URCap API 1.12.0 | Host platform API | Nodes, DataModel, `IOModel`, `ScriptWriter`, keyboard input, undo/redo | n/a | `pom.xml`, `SRC/**` imports `com.ur.urcap.api.*` |
| Machine (CNC mill/indexer) via digital I/O or Modbus | Field I/O, configured by the user in the Installation node | 7 logical signals (below) | none | `installation/Signal.java`, `IoOption.java` |
| UR controller (robot) | Deploy target | `scp` jar to `/root/.urcaps/` and `pkill java` | root password in `pom.xml` properties | `pom.xml` profile `remote` |
| UR Sim VM | Deploy target | `scp` to `/home/ur/ursim-current/.urcaps/` | ur password in `pom.xml` | `pom.xml` profile `ursimvm` |

### 2) Signal Map (`Signal` enum)

| Signal | Direction (robot) | Script var |
|--------|-------------------|-----------|
| Cycle Complete (enum `M21`) | input | `SIG_M21` |
| Cycle Start (enum `MFIN`) | output | `SIG_MFIN` |
| Vise 1 / Vise 2 | output | `SIG_VISE1` / `SIG_VISE2` |
| Door Output | output | `SIG_DOOR` |
| Door Opened / Door Closed | input | `SIG_DOOR_OPENED` / `SIG_DOOR_CLOSED` |

Each may map to: standard / configurable / tool digital I/O (pin parsed from default name like `digital_in[2]`) or a Modbus signal defined in PolyScope Installation > Fieldbus > MODBUS (`IoOption.java`). Modbus output is written with `modbus_set_output_register`, input read with `modbus_get_signal_status(...) > 0` (`IoScript.java`). Grippers use tool digital outputs 0/1, hard-coded (`G1 = 0`, `G2 = 1`).

### 3) Data Stores / Auth / Monitoring

- No database, network client, auth or telemetry in the code. A previous `ControllerClient.java` was deleted in commit `8e1a0d1` in favour of the URCap `ModbusIO` API.
- Logging: `textmsg(...)` in generated URScript only.

### 4) Secrets

- `pom.xml` contains plaintext default credentials for the robot (`root`/`easybot`) and the sim VM (`ur`/`easybot`) and a fixed IP `192.168.8.10`. These are UR factory defaults/placeholders but are committed. See CONCERNS.md.

### 5) Evidence

- `pom.xml`, `SRC/installation/*`, `SRC/program/IoScript.java`
