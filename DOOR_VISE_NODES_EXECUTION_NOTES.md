# Execution notes: DIO Tender Door / Vise nodes

Plan: `DOOR_VISE_NODES_PLAN.md`

## Done
- Step 0: the plan file was already at the target path, so nothing to copy.
- New: `program/IoScript.java`, `program/door/*` (Service, Contribution, View), `program/vise/*` (Service, Contribution, View).
- Modified: `impl/Activator.java` (registers both services); `installation/IoOption.java` (`readExpression`, `writeStatement`, `Source.isDigital` made public).
- `mvn -o install` compiles cleanly and produces `target/dioTender-1.0-SNAPSHOT.urcap`.
- Checked `IoScript` output with a throwaway harness (in the scratchpad, not in the repo) for all four source types. Output matches the plan's table:
  standard / configurable / tool use `set_*_digital_out` / `get_*_digital_in`; Modbus uses `modbus_set_output_register(SIG_*, 1|0, False)` and `modbus_get_signal_status(SIG_*, False) > 0`. No deprecated calls.

## Deviations
1. **Build command: `mvn -o install` instead of `mvn install -P ursim`.** The plan's profile deploys the jar to URSim; I only wanted to confirm it compiles, and URSim isn't running in this session. Offline mode worked because dependencies were already cached.
2. **Added `IoScript.describe(IoOption)`** (not in the plan). It supports the plan's *optional* "mapped I/O under the controls" label, which I implemented in both views and via `getMappingText()` in both contributions. It shows the stored id (e.g. `digital_out[2]` or the Modbus name), not the user-renamed label.
3. **Views take a `Style` field that is currently unused.** Kept it to match how the other views are constructed. Harmless, can be removed.
4. **Door wait loop uses a fixed local name `dt_t`.** The plan said to check this. It is reset to 0 at the start of each door node, so repeated nodes don't interfere, and loops run sequentially in the main program. Not verified inside the real generated script (see below).

## Not done (needs URSim / PolyScope UI, not available in this session)
- Verification steps 2-5: inserting the nodes, undo/redo, save/reload persistence, undefined (yellow) state when unmapped, inspecting `/ursim/programs/<name>.script`, and running in URSim with I/O toggled.
- Not verified: how `ScriptWriter.whileNot` formats its condition in the generated file (I assumed `while not (...)`) and whether a `dt_t` variable at program scope conflicts with anything.
- Not tested on hardware: real door and vise actuation, timing, Modbus signals.

## Next
Run `mvn install -P ursim`, then `./startURSim.sh` and follow plan Verification steps 2-5.
