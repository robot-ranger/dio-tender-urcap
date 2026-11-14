<p align="center">
  <img src="images/toolbar_logo.png" alt="Process Robotics" height="50">
</p>

# DIO Tender

A URCap for Universal Robots e-Series that gives you a ready-made program skeleton for **machine tending** with a dual-gripper, dual-vise setup. You map your machine's signals once, then teach only the robot moves.

![DIO Tender program tree in PolyScope](images/program_diotender_root.png)

DIO Tender is developed and owned by [Process Robotics](https://processrobotics.com) and is part of the [MXInterface](https://mxinterface.com) ecosystem.

## Start here, scale with MXInterface

DIO Tender is the quickest way to put a Universal Robots cobot to work on a machine: map your signals, teach your moves and run. It is the on-ramp to **MXInterface**, the orchestration platform that turns that same cobot into a multi-machine production asset.

| | DIO Tender | MXInterface |
|---|---|---|
| Best for | Getting your first cell running fast | Scaling to a full production floor |
| Machines per robot | One machine, dual-gripper and dual-vise cycle | Three or more CNC machines tended by one cobot |
| Coordination | Cycle Complete handshake and a ready-made sequence | Dispatches work across machines, so the robot is never waiting on one |
| Machine interface | Digital I/O or Modbus signals | MTConnect-native (Part 5 interfaces), with agent and device adapters included |
| Work definition | Pick, Wait, Swap, Place template | Abstract task model: any repeatable robot operation becomes a dispatchable task |
| Data and visibility | Handshake signals | REST/HTTP data layer that plugs into dashboards, historians and MES |
| Adding a machine | New program and wiring | Add it to the hub. Each added machine costs a fraction of the first |
| Planning | Commission on the real cell | Free NVIDIA Isaac Sim digital twin of your workcell before you buy |

### Why move up

A cobot tending a single machine typically sits idle most of the time, since industry figures put 1:1 utilization below 35%. MXInterface is built to push that above 85% by orchestrating several machines from one robot. For example, three Haas VF-2 machines with 12-minute cycles and one UR10e at an $85/hour burden rate yield over 320 extra machine-hours a year, worth more than $27,000, against a $5,500 per-robot software license.

MXInterface runs on open standards (MTConnect and REST/HTTP). It needs no proprietary hardware, no custom PLC per machine pairing, and no systems integrator on retainer. It deploys in days. Schweitzer Engineering runs a TRAK VMC2 with a UR12 unattended on the MXInterface M21/Indexer handshake, with IO for door and chuck control, at $10,675 all in.

**Ready to put your cobot to work on more than one machine?** Request a free simulation of your workcell at [mxinterface.com](https://mxinterface.com).

## Features

- **Ready-made cycle**: Pick Raw → Wait for M21 → Tend Swap (Unload, Load) → Place Finished, repeated forever.
- **Signal mapping**: connect the machine, vises and door to digital I/O or Modbus signals, with optional inversion.
- **Door and Vise nodes**: open or close the door (with feedback and timeout) or a vise (with a settle dwell).
- **Automatic handshake**: a short Cycle Complete pulse is never missed, and Cycle Start is pulsed once the robot is clear and the door is closed.

## Requirements

- Universal Robots e-Series.
- A machine, vises and door controllable through digital I/O or Modbus signals.

## Setup

### 1. Install the URCap

Copy the `.urcap` file to the robot and restart PolyScope, following the usual UR procedure for installing a URCap.

### 2. (Modbus only) Define your signals

Add any Modbus signals in **Installation > Fieldbus > MODBUS** first and give each a clear name. 0 means off, above 0 means on.

![Modbus signals defined in the Fieldbus page](images/installation_fieldbus_modbus.png)

### 3. Map the seven signals

Open **Installation > URCaps > DIO Tender**, tick **Enable**, and choose an I/O for every signal.

| Signal | Direction | Purpose |
|--------|-----------|---------|
| Cycle Complete | machine → robot | Machine finished; robot may tend it |
| Door Opened | door → robot | Door fully open |
| Door Closed | door → robot | Door fully closed |
| Cycle Start | robot → machine | Pulse telling the machine to begin a cycle |
| Vise 1 | robot → vise | Open / close vise 1 |
| Vise 2 | robot → vise | Open / close vise 2 |
| Door Output | robot → door | Open / close the door |

![DIO Tender installation page](images/installation_diotender.png)

Tick **Invert** if a signal's logic is reversed. The buttons next to each output let you test it immediately.

> [!NOTE]
> DIO Tender program nodes stay undefined until it is enabled and all seven signals are mapped.

## Using it in a program

1. In the **Program** tab, open **URCaps** and insert **DIO Tender**. It creates the template tree with comments describing what to teach at each step.
2. Teach your moves inside **Pick Raw**, **Unload (OP1)**, **Load (OP2)** and **Place Finished**.
3. Add **DIO Tender Door** and **DIO Tender Vise** nodes inside Unload and Load where needed.

### The cycle

| Node | What it does |
|------|--------------|
| **Pick Raw** | Your moves to pick a raw part from the infeed. |
| **Wait for M21** | Pauses until the machine signals Cycle Complete. |
| **Tend Swap** | Fixed container holding **Unload (OP1)** and **Load (OP2)**. When both finish, Cycle Start is pulsed. |
| **Place Finished** | Your moves to place the finished part at the outfeed. |

![Wait for Machine node](images/program_diotender_wait.png)

### Door node

Choose **Open** or **Close**. The node sets the Door Output, waits for the matching feedback, and stops with an error popup if the door doesn't respond within 10 seconds.

![Door node](images/program_diotender_door.png)

### Vise node

Choose the vise, **Open** or **Close**, and a **Dwell** time (default 1.0 s, max 60 s). Vises have no feedback signal, so the dwell lets them settle.

![Vise node](images/program_diotender_vise.png)

> [!IMPORTANT]
> Both grippers must be empty when the program starts.

## Safety I/O integration

DIO Tender only uses **standard (non-safety) digital I/O and Modbus signals**. The Door Opened / Door Closed feedback, Door Output and Cycle handshake are **not safety functions** and must not be relied on to protect people. Wire machine and cell safety separately through the robot's safety I/O.

References: [UR5e User Manual (SW 5.26)](https://www.universal-robots.com/manuals/EN/PDF/SW5_26/user-manual-UR5e-PDF_online/710-965-00_UR5e_User_Manual_en_Global.pdf) section 8.6 *Safety I/O*, and [PolyScope e-Series Software Handbook (SW 5.26)](https://www.universal-robots.com/manuals/EN/PDF/SW5_26/Poly5_handbook_online/e-Series_Software_Handbook_en_Global.pdf) section 2.7 *Safety I/O* (the handbook's 2.6 *Software Safety Limits* and 2.8 *Software Safety Restrictions* cover limits and safety planes).

### Key points

- **Redundant, dual-channel.** All safety I/O are paired (Category 3, PLd). Keep the two channels as separate branches.
- **Permanent inputs** (yellow/red terminals): *Robot Emergency Stop* (E-stop equipment only) and *Safeguard Stop* (protective devices such as door switches, light curtains, mats and scanners).
- **Configurable I/O** (CI/CO, yellow/black terminals) can be assigned safety functions in **Installation > Safety > I/O**. These are separate from the general-purpose digital I/O that DIO Tender maps.
- **Safety configuration is locked by a checksum.** Changing it powers the robot off and requires applying and confirming the new configuration. Test all safety functions after any change and regularly afterwards.

### Typical machine-tending connections

| Need | Use | Notes |
|------|-----|-------|
| Machine door interlock | **Safeguard Stop** (SI0/SI1) | Stop Category 2, protective stop. Drive power stays on, program pauses and resumes. |
| Reset after guard is cleared | **Safeguard Reset** (CI pair) | Two-channel reset button placed outside the cell. Required for light curtains. |
| Share E-stop with the machine | **External Emergency Stop** (input pair) and **System Stop** (output pair) | Do not use the Robot Emergency Stop input for sharing. Use a safety PLC if more than two machines are linked. |
| Slower motion while operator is near | **Reduced** input | Low signal switches to the Reduced configuration. Limits are met in under 0.5 s. |
| Tell the machine the robot is stopped or moving | Safety outputs such as **Robot Moving**, **Robot Not Stopping**, **Not Reduced**, **Safe Home** | All safety outputs go low on a fault. |
| Manual / Automatic switching | **Operational Mode** input | Key switch with redundant layout or safety PLC. Low is Automatic, high is Manual. |

### Notes for this URCap

- Keep the **Door Output** (open/close) on general I/O, and the **door lock/interlock switch** on Safeguard Stop. The door being closed in software does not make the cell safe.
- If the Safeguard Stop trips mid-cycle, the program pauses and the Door and Vise nodes resume where they left off. Check that an interrupted door or vise move is safe to resume, since vises have no feedback signal.
- Do not use automatic Safeguard Reset where a person can re-trigger the signal from inside the perimeter. Use an external reset outside the cell.
- Safety outputs may be set to emit **OSSD test pulses** (1 ms low pulse every 32 ms). Make sure the machine input accepts them, or leave OSSD disabled.
- Inputs are filtered to tolerate OSSD pulses under 3 ms.
- Any machine receiving the E-stop state from the robot's **System Stop** output must comply with ISO 13850. It needs a manual action to resume once the E-stop is released.
- Use **Safe Home** or **Not Reduced** outputs only for signalling to the machine. DIO Tender's Wait for M21 and Cycle Start do not replace them.

> [!WARNING]
> Perform a risk assessment for the complete cell. Verify and test every safety function before production use.
