# Plan: DIO Tender Door and DIO Tender Vise program nodes

## Context
The DIO Tender URCap (PolyScope 5, URCap API 1.12.0, e-Series only) has fixed template nodes: Pick Raw, Wait M21, Tend Swap (with Unload and Load under it) and Place Finished. To work the door or a vise inside Unload/Load, the user currently types `vise(SIG_VISE1, True)` and similar calls into Script nodes. This change adds two insertable program nodes so these actions can be taught from the UI:

- **DIO Tender Door**: an Open / Close radio pair. It sets the Door Output signal, then waits for Door Opened or Door Closed, failing after DOOR_TIMEOUT.
- **DIO Tender Vise**: an Open / Close radio pair plus a Vise 1 / Vise 2 radio pair. It sets that vise's output signal, then sleeps VISE_TIME.

In Installation > DIO Tender, each signal can be mapped to a standard, configurable or tool digital I/O, or to a Modbus signal. The script each node generates must use the URScript call that matches the mapped I/O type, per the PS5 URScript manual (below). Both nodes use the existing Vise 1 / Vise 2 / door signals, so no new signals are added.

### URScript reference (PS5 e-Series Script Directory)
Source: `/workspaces/universal_robots_sdk/.claude/skills/polyscope5-urcap/references/script_directory_Poly5.pdf`, section 18 "Module interfaces"

| Mapped I/O | Write output | Read input | n range |
|---|---|---|---|
| Standard digital | `set_standard_digital_out(n, b)` (18.48) | `get_standard_digital_in(n)` (18.13) | [0:7] |
| Configurable digital | `set_configurable_digital_out(n, b)` | `get_configurable_digital_in(n)` | [0:7] |
| Tool digital | `set_tool_digital_out(n, b)` (18.49) | `get_tool_digital_in(n)` (18.16) | [0:1] |
| Modbus | `modbus_set_output_register(name, value, False)` (current behavior) | `modbus_get_signal_status(name, False)` | n/a |

`set_digital_out(n, b)` / `get_digital_in(n)` (18.45) are **deprecated**. Don't use them.

## Step 0
Before writing any code, copy this plan to `/workspaces/universal_robots_sdk/my_urcaps/dio-tender/DOOR_VISE_NODES_PLAN.md` (next to `dual_grip_dual_vise_dio.urscript`).

## Handoff notes (for the implementing agent)
- Platform: PolyScope 5, URCap API `1.12.0` (see `pom.xml`), Java 1.8. Use anonymous inner classes, not lambdas, to match the existing code.
- Read these files before writing code. Each one shows a pattern to copy or code to reuse:
  - `program/waitM21/WaitM21ProgramNodeService.java`: service skeleton and Style selection.
  - `program/root/DioTenderRootProgramNodeContribution.java`: `programAPI.getInstallationNode(DioTenderInstallationNodeContribution.class)`.
  - `installation/DioTenderInstallationNodeView.java`: `updating` guard flag, `Style.buildLabel`.
  - `program/InfoProgramNodeView.java`: view layout and logo row.
  - `installation/DioTenderInstallationNodeContribution.java`:
    - `getSelection(Signal)` returns the mapped `IoOption`: source STANDARD / CONFIGURABLE / TOOL / MODBUS, plus pin and id.
    - `writeHelpers()` defines `fail(msg)`, `wait_mb(sig, timeout)` and the constants `VISE_TIME` and `DOOR_TIMEOUT`.
    - `mb_on()` / `mb_set()` dispatch digital-mapped signals by name. The new nodes must not rely on that dispatch. They emit the I/O-specific call directly.
  - `installation/IoOption.java`: `readExpression()` and `writeStatement(value)` already produce the correct standard / configurable / tool calls from the table above. Reuse them.
  - `installation/Signal.java`: `VISE1`, `VISE2`, `DOOR_OUTPUT`, `DOOR_OPENED`, `DOOR_CLOSED`, and `getScriptVar()`.
- Undo/redo: import `com.ur.urcap.api.domain.undoredo.UndoableChanges` and call `programAPI.getUndoRedoManager().recordChanges(...)`.
- View interface: `com.ur.urcap.api.contribution.program.swing.SwingProgramNodeView<C>`, with `buildUI(JPanel, ContributionProvider<C>)`. Inside listeners, get the contribution with `provider.get()`.
- The service passes the view into `createNode(...)`. Pass it on to the contribution constructor so `openView()` can call `view.update(this)`.
- Don't edit anything under `/workspaces/universal_robots_sdk/samples/`. It is read-only reference.

## Files

Base: `/workspaces/universal_robots_sdk/my_urcaps/dio-tender/com.processrobotics.dioTender/src/main/java/com/processrobotics/dioTender/`

New:
- `program/door/DoorProgramNodeService.java`
- `program/door/DoorProgramNodeContribution.java`
- `program/door/DoorProgramNodeView.java`
- `program/vise/ViseProgramNodeService.java`
- `program/vise/ViseProgramNodeContribution.java`
- `program/vise/ViseProgramNodeView.java`
- `program/IoScript.java`: shared helper that turns a mapped signal into URScript (see below).

Modified:
- `impl/Activator.java`: register both services.
- `installation/IoOption.java`: make `readExpression()` and `writeStatement(String)` `public`, and make `Source.isDigital()` `public`. The program package needs them. Behavior is unchanged.

## Design

### `program/IoScript.java` (shared by both nodes)
- `static String write(IoOption io, String scriptVar, boolean on)`:
  - Digital source: `io.writeStatement(on ? "True" : "False")`, which gives e.g. `set_standard_digital_out(3, True)`.
  - MODBUS source: `modbus_set_output_register(<scriptVar>, 1|0, False)`. This matches the existing `mb_set` Modbus branch, and `scriptVar` is the `SIG_*` constant holding the Modbus name.
- `static String read(IoOption io, String scriptVar)`:
  - Digital source: `io.readExpression()`, which gives e.g. `get_standard_digital_in(2)`.
  - MODBUS source: `modbus_get_signal_status(<scriptVar>, False) > 0`.

### Services (copy the pattern from `program/waitM21/WaitM21ProgramNodeService.java`)
- `getId()`: `"DioTenderDoor"` / `"DioTenderVise"`.
- `getTitle(locale)`: `"DIO Tender Door"` / `"DIO Tender Vise"`.
- `configureContribution`: call `setChildrenAllowed(false)`. Leave the node user-insertable (the default).
- `createView`: choose V5Style or V3Style the same way the existing services do, and return the new view.

### Contributions (undo pattern from SDK sample `cyclecounterswing/.../CycleCounterProgramNodeContribution.java:123`)
- Hold `ProgramAPI`, `DataModel` and the view.
- DataModel keys: `"open"` (boolean, default `true`). The vise node also has `"vise"` (int 1 or 2, default `1`).
- Setters wrap `model.set(...)` in `recordChanges(new UndoableChanges(){...})`.
- `openView()`: call `view.update(this)`.
- `getTitle()`: build it from the selection, e.g. `"Door Open"` or `"Vise 2 Close"`.
- `isDefined()`: `installation != null && installation.isMapped()`. When signals aren't mapped, the helpers and `SIG_*` constants aren't written.
- `generateScript`: look up the mapped `IoOption` with `installation.getSelection(signal)` at generation time, then:
  - **Vise** (signal = `Signal.VISE1` or `Signal.VISE2`; on = open):
    ```
    set_standard_digital_out(2, True)   # or set_configurable_/set_tool_ / modbus_set_output_register
    sleep(VISE_TIME)
    ```
  - **Door** (output = `Signal.DOOR_OUTPUT`; feedback = `DOOR_OPENED` when opening, `DOOR_CLOSED` when closing):
    ```
    set_standard_digital_out(4, True)   # IoScript.write(...)
    dt_t = 0
    while not (get_standard_digital_in(5)):   # IoScript.read(feedback, ...)
      if dt_t > DOOR_TIMEOUT:
        fail("Timeout waiting for door opened")
      end
      dt_t = dt_t + get_steptime()
      sync()
    end
    ```
    Build this with the `ScriptWriter` methods `whileNot`, `ifCondition`, `sync()` and `end()`, the same calls `writeHelpers()` uses. This keeps the loop real-time-safe (`sync()` every tick). If the same variable name in more than one node is a problem, use a uniquely named local, or wrap the code in a generated `def`. Check this when you inspect the generated script.

### Views (`SwingProgramNodeView<C>`)
- Lay out like `program/InfoProgramNodeView.java`: Y-axis `BoxLayout`, a short description label, the controls, vertical glue, then `LogoRow.create(75)`.
- For each radio pair: one `ButtonGroup` with two `JRadioButton`s, placed side by side in a horizontal `Box` after a `Style.buildLabel(...)` caption ("Door:", "Action:", "Vise:"). Use DejaVu Sans at `FONT_VERY_LARGE`.
- Listeners call `provider.get().setOpen(...)` or `setVise(1|2)`, guarded by an `updating` flag.
- `update(contribution)`: select the radio buttons from the contribution's getters.
- Optional: show the mapped I/O under the controls (e.g. "Vise 1 → digital_out[2]") so the operator sees what will switch.

### Activator
Add two `context.registerService(SwingProgramNodeService.class, new ...Service(), null);` lines.

### Not changed
- Tend Swap's `door(True)` and `release_machine()` still go through the installation helpers. They already handle digital-mapped signals via `mb_set` / `mb_on`.
- The Unload/Load template comments stay as they are.

## Verification
1. In the `universal_robots_sdk` devcontainer, run `cd /workspaces/universal_robots_sdk/my_urcaps/dio-tender/com.processrobotics.dioTender && mvn install -P ursim`. The build must compile cleanly.
2. Run `./startURSim.sh` and open http://localhost:6080/vnc.html. In Installation > DIO Tender, enable DIO Tender and map all seven signals.
3. In Program > URCaps, confirm that "DIO Tender Door" and "DIO Tender Vise" appear. Insert each one under Unload/Load and check:
   - The radio buttons toggle, the node title updates, and Undo/Redo restores the selection.
   - Save the program, reload it, and confirm the selections persisted.
   - Unmap a signal and confirm the nodes become undefined (yellow).
4. Check the generated script (`/ursim/programs/<name>.script`) with each mapping type in turn:
   - Vise 1 on standard DO 2 → `set_standard_digital_out(2, True)`
   - Vise 2 on configurable DO 1 → `set_configurable_digital_out(1, False)`
   - tool DO → `set_tool_digital_out(n, b)`
   - Modbus → `modbus_set_output_register(SIG_VISE1, 1, False)`
   - Door on standard I/O → `set_standard_digital_out(...)`, then a wait loop on `get_standard_digital_in(...)`
   - No deprecated `set_digital_out` / `get_digital_in` anywhere.
5. Run the program in URSim with standard digital I/O mapped. Confirm the vise output toggles in the I/O tab. Then toggle the Door Opened/Closed input by hand and confirm the door node continues, or times out with the popup if the input is never set.
6. Not tested on hardware: real door and vise actuation, timing, and Modbus signals.
