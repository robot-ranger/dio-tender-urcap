# DIO Tender URScript Manual

This manual describes the URScript functions, constants and variables that the **DIO Tender** URCap adds to your robot program. You can call them from any **Script** node (or any other place that accepts URScript) in the same way you call built-in URScript functions such as `sleep()` or `set_digital_out()`.

Contents

1. [Overview](#1-overview)
2. [Types and conventions](#2-types-and-conventions)
3. [Constants](#3-constants)
4. [Variables](#4-variables)
5. [Functions](#5-functions)
   - [mb_on](#mb_onsig) · [mb_set](#mb_setsig-on) · [wait_mb](#wait_mbsig-timeout) · [vise](#visesig-open) · [door](#dooropen) · [release_machine](#release_machine) · [fail](#failmsg)
6. [Complete example](#6-complete-example)

---

## 1. Overview

DIO Tender maps seven logical signals to the physical I/O you choose in **Installation > URCaps > DIO Tender**. Each signal can be a standard, configurable or tool digital I/O, or a Modbus signal. The functions in this manual let your own script logic use those signals **by logical name**, without caring which kind of I/O is behind them. Inversion set on the installation page is applied automatically.

The program nodes (Pick Raw, Tend Swap, Unload, Load, Door, Vise, …) are built on these same functions, so script code and nodes behave identically.

**Availability.** The functions are only defined when DIO Tender is **enabled** and all seven signals are **mapped** in the installation. If the signals are not mapped, none of the functions or constants below exist and calling them is a script error.

---

## 2. Types and conventions

| Name | Type | Meaning |
|------|------|---------|
| `sig` | string | A logical signal constant: one of the `SIG_*` constants in [Constants](#3-constants). Never pass a raw Modbus name or pin number. |
| `on`, `open` | bool | `True` / `False`. For a vise or door, `open = True` opens it and `False` closes it. |
| `timeout` | number | Seconds. |

- All time values are in **seconds**.
- A signal is *on* when the physical level is active after inversion. For Modbus signals, a value of 0 is off and any value above 0 is on.
- Functions that fail (timeouts) show a popup and **halt the program**; they do not return an error code.

---

## 3. Constants

### Signal names

Pass these to `mb_on`, `mb_set`, `wait_mb` and `vise`. Each holds the name of the signal you mapped, so always use the constant rather than a literal string.

| Constant | Direction | Description |
|----------|-----------|-------------|
| `SIG_M21` | input | Cycle Complete: the machine has finished and may be tended. |
| `SIG_MFIN` | output | Cycle Start: pulse telling the machine to begin a cycle. |
| `SIG_VISE1` | output | Vise 1. |
| `SIG_VISE2` | output | Vise 2. |
| `SIG_DOOR` | output | Door output (open / close command). |
| `SIG_DOOR_OPENED` | input | Door fully open feedback. |
| `SIG_DOOR_CLOSED` | input | Door fully closed feedback. |

Use input constants only with `mb_on` and `wait_mb`, and output constants only with `mb_set` and `vise`.

### Timing and gripper constants

| Constant | Value | Unit | Description |
|----------|-------|------|-------------|
| `VISE_TIME` | 1.0 | s | Settle time `vise()` waits after switching a vise (there is no vise feedback signal). |
| `MFIN_PULSE` | 0.5 | s | Width of the Cycle Start pulse sent by `release_machine()`. |
| `DOOR_TIMEOUT` | 10.0 | s | Maximum time `door()` waits for the door feedback signal. |
| `G1` | 0 | | Tool digital output index for gripper 1. High = gripper closed. |
| `G2` | 1 | | Tool digital output index for gripper 2. High = gripper closed. |

Example: close gripper 1, then open it.

```python
set_tool_digital_out(G1, True)
sleep(0.5)
set_tool_digital_out(G1, False)
```

---

## 4. Variables

### tend_req

```python
global tend_req = "idle"
```

Global string holding the current state of the tending handshake. Read it to branch on the machine state; the program nodes update it for you.

| Value | Meaning |
|-------|---------|
| `"idle"` | Nothing requested. The initial value, and the value after a Tend Swap completes. |
| `"unload"` | Cycle Complete was received (latched on its rising edge, so a short pulse is never missed). The machine is ready to be unloaded. |
| `"load"` | The Unload node has finished; the machine is ready to be loaded. |

Writing to `tend_req` from your own script changes the cycle sequence. Do this only if you are replacing the standard nodes.

---

## 5. Functions

### mb_on(sig)

**Description**

Reads a logical input signal and returns whether it is on. Digital and Modbus mappings are handled transparently, and inversion is applied.

**Parameters**

| Name | Type | Description |
|------|------|-------------|
| `sig` | string | An input signal: `SIG_M21`, `SIG_DOOR_OPENED` or `SIG_DOOR_CLOSED`. |

**Return value**

`True` if the signal is on, otherwise `False`.

**Example**

```python
if mb_on(SIG_DOOR_OPENED):
  textmsg("Door is open")
end
```

---

### mb_set(sig, on)

**Description**

Switches a logical output signal on or off. Digital and Modbus mappings are handled transparently, and inversion is applied. The call returns immediately. It does not wait or check feedback; use `vise()` or `door()` if you need the settle or feedback wait.

**Parameters**

| Name | Type | Description |
|------|------|-------------|
| `sig` | string | An output signal: `SIG_MFIN`, `SIG_VISE1`, `SIG_VISE2` or `SIG_DOOR`. |
| `on` | bool | `True` to switch on, `False` to switch off. |

**Return value**

None.

**Example**

```python
mb_set(SIG_MFIN, True)
sleep(0.2)
mb_set(SIG_MFIN, False)
```

---

### wait_mb(sig, timeout)

**Description**

Blocks until an input signal becomes on. Checks once per controller step. If the signal is not on after `timeout` seconds, a popup `Timeout waiting for <signal name>` is shown and the program halts (see [fail](#failmsg)).

**Parameters**

| Name | Type | Description |
|------|------|-------------|
| `sig` | string | An input signal: `SIG_M21`, `SIG_DOOR_OPENED` or `SIG_DOOR_CLOSED`. |
| `timeout` | number | Maximum wait in seconds. |

**Return value**

None. Returns only when the signal is on; otherwise the program halts.

**Example**

```python
wait_mb(SIG_M21, 600)   # wait up to 10 minutes for the machine
```

---

### vise(sig, open)

**Description**

Opens or closes a vise, then waits `VISE_TIME` seconds for it to settle.

**Parameters**

| Name | Type | Description |
|------|------|-------------|
| `sig` | string | `SIG_VISE1` or `SIG_VISE2`. |
| `open` | bool | `True` to open the vise, `False` to close it. |

**Return value**

None.

**Example**

```python
vise(SIG_VISE1, True)    # open vise 1, wait VISE_TIME
vise(SIG_VISE1, False)   # close vise 1, wait VISE_TIME
```

---

### door(open)

**Description**

Opens or closes the machine door and waits for the matching feedback signal (`SIG_DOOR_OPENED` or `SIG_DOOR_CLOSED`). If the feedback does not arrive within `DOOR_TIMEOUT` seconds, a popup is shown and the program halts.

**Parameters**

| Name | Type | Description |
|------|------|-------------|
| `open` | bool | `True` to open the door, `False` to close it. |

**Return value**

None. Returns only once the door has reached the requested position.

**Example**

```python
door(True)     # open and wait for Door Opened
# ... robot enters the machine ...
```

---

### release_machine()

**Description**

Hands the machine back after tending: closes the door (waiting for Door Closed), then pulses Cycle Start high for `MFIN_PULSE` seconds and sets it low again. The robot must be clear of the door before calling this function.

**Parameters**

None.

**Return value**

None.

**Example**

```python
release_machine()
```

---

### fail(msg)

**Description**

Shows an error popup titled **Machine tending** with the given message and halts the program. This is the same routine the other functions use when a timeout occurs; call it from your own script to stop the program with a consistent message.

**Parameters**

| Name | Type | Description |
|------|------|-------------|
| `msg` | string | Message to show in the popup. |

**Return value**

None. The program halts.

**Example**

```python
if not mb_on(SIG_DOOR_CLOSED):
  fail("Door not closed before cycle start")
end
```

---

## 6. Complete example

A hand-written tending step that does what the Unload, Load and Tend Swap nodes do, for use in a Script node:

```python
wait_mb(SIG_M21, 600)       # machine finished
door(True)                  # open the door, wait for feedback

vise(SIG_VISE1, True)       # release finished part
# ... move: remove finished part ...
# ... move: place raw part ...
vise(SIG_VISE1, False)      # clamp raw part

release_machine()           # close door, pulse Cycle Start
```
