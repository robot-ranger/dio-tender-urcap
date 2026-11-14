# Architecture

`SRC` = `com.processrobotics.dioTender/src/main/java/com/processrobotics/dioTender`.

## Core Sections (Required)

### 1) Architectural Style

- Style: a UR PolyScope **URCap plugin** that is a **code generator**. Java never moves the robot or talks Modbus at run time; it emits URScript through `ScriptWriter` that the controller executes. (Exception: the installation page polls/sets I/O live for diagnostics.)
- Pattern per node: OSGi `*Service` (factory) → `*Contribution` (model + `generateScript`) → optional Swing `*View`.
- Evidence: `SRC/impl/Activator.java`, `SRC/installation/DioTenderInstallationNodeContribution.java#generateScript`.

### 2) Layers / Components

| Component | Responsibility | Evidence |
|-----------|----------------|----------|
| Installation node (`DioTender`) | User maps 7 `Signal`s to digital or Modbus I/O, per-signal invert flag, enable toggle; persisted in installation `DataModel`; emits the program preamble (constants + helper functions + watcher thread) | `installation/*` |
| `Signal` / `IoOption` | Logical signal definitions; one concrete I/O selection (source, id, pin) and the URScript read/write expression for it | `installation/Signal.java`, `IoOption.java` |
| `IoScript` | Turns an `IoOption` + invert flag into URScript read/write text | `program/IoScript.java` |
| Root node `DioTender` | Container; on NEW creation builds template tree Pick Raw → Wait for M21 → Tend Swap → Place Finished; `isDefined` requires installation enabled and mapped | `program/root/*` |
| Tend Swap | Children locked to Unload (OP1) then Load (OP2); after children emits `release_machine()` | `program/tendSwap/*` |
| Door / Vise nodes | Emit direct I/O writes (+ feedback wait/dwell) using the installation mapping | `program/door/*`, `program/vise/*` |
| Pick Raw / Place Finished / Unload / Load | Mostly empty containers: `textmsg`, `writeChildren()`; `TemplateComments` seeds teaching hints | `program/*/…Contribution.java` |
| Style | PolyScope 3 vs 5 sizing chosen from `getSoftwareVersion().getMajorVersion() >= 5` | `style/*`, services |

### 3) Run-time Flow (generated URScript)

1. Preamble (installation `generateScript`): if all 7 signals mapped, emits `SIG_*` name constants, `G1=0`/`G2=1` (tool outputs, HIGH = closed), timing constants (`VISE_TIME` 1.0, `MFIN_PULSE` 0.5, `DOOR_TIMEOUT` 10.0), `tend_req = "idle"`, helpers `set_req, fail, mb_on, mb_set, wait_mb, m21_watch (thread), vise, door, release_machine`. If enabled+mapped, starts `m21_watch_thrd = run m21_watch()`; otherwise emits a `textmsg` saying scripts were omitted.
2. `m21_watch` thread latches the rising edge of the *Cycle Complete* input (`SIG_M21`) → `tend_req = "unload"`.
3. Program loop (user's root node wraps in `if m21_watch_thrd != "":`): Pick Raw → Wait for M21 (`while tend_req == "idle": sync()`) → Tend Swap { Unload (sets `tend_req="load"`), Load, `release_machine()` (door close, pulse *Cycle Start* `SIG_MFIN` for `MFIN_PULSE`), `tend_req="idle"` } → Place Finished.
4. Signal inversion and I/O type dispatch happen at generation time (`mb_on`/`mb_set` are generated per-mapping; node scripts use `IoScript` directly).

### 4) State

- Installation `DataModel` keys: `enabled`, `signal_<key>`, `signal_<key>_source`, `signal_<key>_pin`, `signal_<key>_invert`.
- Program node `DataModel` keys: Door `open`; Vise `open`, `vise`, `dwell`.
- Evidence: `DioTenderInstallationNodeContribution.java`, `DoorProgramNodeContribution.java`, `ViseProgramNodeContribution.java`.

### 5) Evidence

- `installation/DioTenderInstallationNodeContribution.java`, `program/root/*`, `program/tendSwap/*`, `program/waitForMachine/*`, `program/unload/*`
- Design reference: `com.processrobotics.dioTender/urscript_reference/dual_grip_dual_vise_dio.urscript` (describes the intended part flow: 2 grippers, 2 vises, mill indexer, M21/MFIN handshake).
