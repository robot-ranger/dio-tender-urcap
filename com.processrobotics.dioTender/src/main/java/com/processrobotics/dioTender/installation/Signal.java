package com.processrobotics.dioTender.installation;

/**
 * Logical tender signals. Each one is mapped by the user to a Modbus signal configured in
 * Installation > Fieldbus > MODBUS. The selected Modbus signal name is stored in the
 * installation DataModel under {@link #getModelKey()} and written to script as {@link #getScriptVar()}.
 */
public enum Signal {
	M21("m21", "M21", true, "SIG_M21"),
	MFIN("mfin", "MFin", false, "SIG_MFIN"),
	VISE1("vise1", "Vise 1", false, "SIG_VISE1"),
	VISE2("vise2", "Vise 2", false, "SIG_VISE2"),
	DOOR_OUTPUT("doorOutput", "Door Output", false, "SIG_DOOR"),
	DOOR_OPENED("doorOpened", "Door Opened", true, "SIG_DOOR_OPENED"),
	DOOR_CLOSED("doorClosed", "Door Closed", true, "SIG_DOOR_CLOSED");

	private final String key;
	private final String label;
	private final boolean input;
	private final String scriptVar;

	Signal(String key, String label, boolean input, String scriptVar) {
		this.key = key;
		this.label = label;
		this.input = input;
		this.scriptVar = scriptVar;
	}

	public String getModelKey() {
		return "signal_" + key;
	}

	public String getLabel() {
		return label;
	}

	/** true = Modbus input (robot reads), false = Modbus output (robot writes) */
	public boolean isInput() {
		return input;
	}

	public String getScriptVar() {
		return scriptVar;
	}
}
