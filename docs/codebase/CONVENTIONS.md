# Coding Conventions

## Core Sections (Required)

### 1) Naming and Structure

- Package root `com.processrobotics.dioTender`; feature packages are lowerCamelCase (`pickRaw`, `waitForMachine`, `tendSwap`, `placeFinished`).
- Program node classes: `<Name>ProgramNodeService`, `<Name>ProgramNodeContribution`, optional `<Name>ProgramNodeView`. Installation: `DioTenderInstallationNode*`.
- Service IDs are `"DioTender<Name>"` (e.g. `"DioTenderRoot"`, `"DioTenderLoad"`); node titles are user-facing strings from `getTitle`.
- Generated URScript names: constants `SIG_*`, `G1/G2`, `*_TIME/TIMEOUT`; helpers snake_case; Modbus helper names kept `mb_*` for backward compatibility with existing Script nodes (comment in `DioTenderInstallationNodeContribution.java`).
- DataModel keys are short lowerCamel strings held in `private static final String *_KEY`; signal keys derive from `Signal.getModelKey()`.

### 2) Formatting and Style

- Tabs for indentation; Javadoc `/** */` on non-obvious methods; Java 1.8 syntax with explicit generics and anonymous inner classes (no lambdas seen).
- No formatter/linter config in repo. [TODO] confirm IDE formatter settings.

### 3) Patterns Seen

- Template nodes build their child tree only when `context.getNodeCreationType() == NEW` inside an `UndoableChanges` block (`DioTenderRootProgramNodeContribution`, `TendSwapProgramNodeContribution`).
- Model writes from program nodes go through `programAPI.getUndoRedoManager().recordChanges(...)` (Door, Vise). Installation node writes `model.set` directly.
- Child sequences locked with `setChildSequenceLocked(true)`; template-only nodes use `configuration.setUserInsertable(false)` (`LoadProgramNodeService`).
- Style chosen per PolyScope major version in every `createView`; the same 3-line snippet is repeated in each service.
- `isDefined()` gates nodes on installation mapping (`installation.isMapped()` or `isDefined()`).
- Errors: `TreeStructureException` is caught and `printStackTrace()`d; generated scripts report errors via `fail(msg)` (popup + `halt`).
- Backward compatibility: stored selections without a `_source` key default to Modbus (`getSelection`).

### 4) Evidence

- `SRC/program/root/DioTenderRootProgramNodeContribution.java`, `SRC/program/door/DoorProgramNodeContribution.java`, `SRC/installation/DioTenderInstallationNodeContribution.java`, `SRC/program/load/LoadProgramNodeService.java`
