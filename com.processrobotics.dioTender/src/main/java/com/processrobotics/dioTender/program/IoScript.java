package com.processrobotics.dioTender.program;

import com.processrobotics.dioTender.installation.IoOption;

/** Turns a mapped signal into the URScript call that matches its I/O type. */
public final class IoScript {

	private IoScript() {
	}

	/** Statement that switches the mapped output on or off. With {@code invert} the physical level is the opposite. {@code scriptVar} is the SIG_* constant holding the Modbus name. */
	public static String write(IoOption io, String scriptVar, boolean on, boolean invert) {
		on = on != invert;
		if (io.getSource().isDigital()) {
			return io.writeStatement(on ? "True" : "False");
		}
		return "modbus_set_output_register(" + scriptVar + ", " + (on ? 1 : 0) + ", False)";
	}

	/** Bool expression that reads the mapped input. */
	public static String read(IoOption io, String scriptVar, boolean invert) {
		String expr = io.getSource().isDigital()
				? io.readExpression()
				: "modbus_get_signal_status(" + scriptVar + ", False) > 0";
		return invert ? "not (" + expr + ")" : expr;
	}

	/** Short description of the mapped I/O for display, e.g. "digital_out[2]" or the Modbus signal name. */
	public static String describe(IoOption io) {
		return io.getSource() == IoOption.Source.NONE ? "not mapped" : io.getId();
	}
}
