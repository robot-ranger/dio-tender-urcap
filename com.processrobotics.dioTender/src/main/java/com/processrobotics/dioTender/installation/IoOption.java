package com.processrobotics.dioTender.installation;

import com.ur.urcap.api.domain.io.DigitalIO;
import com.ur.urcap.api.domain.io.IO;
import com.ur.urcap.api.domain.io.ModbusIO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One entry in a signal dropdown: a digital I/O, a Modbus signal, the "-- select --" placeholder,
 * or a non-selectable group header.
 */
public final class IoOption {

	public enum Source {
		NONE, HEADER, MODBUS, STANDARD, CONFIGURABLE, TOOL;

		public boolean isDigital() {
			return this == STANDARD || this == CONFIGURABLE || this == TOOL;
		}
	}

	// default names look like digital_in[2], config_out[0], tool_out[1]
	private static final Pattern PIN = Pattern.compile("\\[(\\d+)\\]$");

	public static final IoOption NOT_SELECTED = new IoOption(Source.NONE, "", -1, "-- select --");

	private final Source source;
	private final String id;    // Modbus: signal name. Digital: robot default name (stable if the user renames the I/O)
	private final int pin;      // digital only
	private final String label;

	IoOption(Source source, String id, int pin, String label) {
		this.source = source;
		this.id = id;
		this.pin = pin;
		this.label = label;
	}

	static IoOption header(String label) {
		return new IoOption(Source.HEADER, label, -1, label);
	}

	static IoOption modbus(ModbusIO io) {
		return new IoOption(Source.MODBUS, io.getName(), -1, io.getName());
	}

	/** null if the I/O isn't a standard / configurable / tool digital I/O or its default name has no pin index. */
	static IoOption digital(DigitalIO io) {
		Source source;
		switch (io.getInterfaceType()) {
		case UR_STANDARD:
			source = Source.STANDARD;
			break;
		case UR_CONFIGURABLE:
			source = Source.CONFIGURABLE;
			break;
		case UR_TOOL:
			source = Source.TOOL;
			break;
		default:
			return null;
		}
		Matcher m = PIN.matcher(io.getDefaultName());
		if (!m.find()) {
			return null;
		}
		String label = io.getName().equals(io.getDefaultName())
				? io.getDefaultName()
				: io.getName() + " (" + io.getDefaultName() + ")";
		return new IoOption(source, io.getDefaultName(), Integer.parseInt(m.group(1)), label);
	}

	/** Rebuild a stored selection (from the DataModel) so it can be matched against, or shown as missing. */
	static IoOption stored(Source source, String id, int pin) {
		return new IoOption(source, id, pin, id);
	}

	IoOption asMissing() {
		return new IoOption(source, id, pin, label + " (missing)");
	}

	public Source getSource() {
		return source;
	}

	public String getId() {
		return id;
	}

	public int getPin() {
		return pin;
	}

	public boolean isSelectable() {
		return source != Source.HEADER;
	}

	/** URScript expression reading this digital input as a bool. */
	public String readExpression() {
		switch (source) {
		case STANDARD:
			return "get_standard_digital_in(" + pin + ")";
		case CONFIGURABLE:
			return "get_configurable_digital_in(" + pin + ")";
		case TOOL:
			return "get_tool_digital_in(" + pin + ")";
		default:
			throw new IllegalStateException("not a digital input: " + id);
		}
	}

	/** URScript statement writing bool expression {@code value} to this digital output. */
	public String writeStatement(String value) {
		switch (source) {
		case STANDARD:
			return "set_standard_digital_out(" + pin + ", " + value + ")";
		case CONFIGURABLE:
			return "set_configurable_digital_out(" + pin + ", " + value + ")";
		case TOOL:
			return "set_tool_digital_out(" + pin + ", " + value + ")";
		default:
			throw new IllegalStateException("not a digital output: " + id);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IoOption)) {
			return false;
		}
		IoOption other = (IoOption) o;
		return source == other.source && id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return source.hashCode() * 31 + id.hashCode();
	}

	@Override
	public String toString() {
		return label;
	}
}
