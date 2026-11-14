package com.processrobotics.dioTender.installation;

import com.processrobotics.dioTender.program.IoScript;
import com.ur.urcap.api.contribution.InstallationNodeContribution;
import com.ur.urcap.api.contribution.installation.InstallationAPIProvider;
import com.ur.urcap.api.domain.InstallationAPI;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.io.DigitalIO;
import com.ur.urcap.api.domain.io.ModbusIO;
import com.ur.urcap.api.domain.script.ScriptWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DioTenderInstallationNodeContribution implements InstallationNodeContribution {

	// timing constants written to script
	private static final String VISE_TIME = "1.0";     // s, vise settle (no vise feedback signals)
	private static final String MFIN_PULSE = "0.5";    // s, mfin pulse width
	private static final String DOOR_TIMEOUT = "10.0"; // s
	private static final String ENABLED_KEY = "enabled";
	public static final String M21_WATCH_THREAD = "m21_watch_thrd";

	private final InstallationAPI installationAPI;
	private final DioTenderInstallationNodeView view;
	private final DataModel model;

	public DioTenderInstallationNodeContribution(InstallationAPIProvider apiProvider, DataModel model,
			DioTenderInstallationNodeView view) {
		this.installationAPI = apiProvider.getInstallationAPI();
		this.model = model;
		this.view = view;
	}

	@Override
	public void openView() {
		view.update(this);
		view.startPolling();
	}

	@Override
	public void closeView() {
		view.stopPolling();
	}

	/**
	 * Logical state of a signal ({@code physical != inverted}), or null if unknown: unmapped, not found in the
	 * IOModel, or not resolvable. The IOModel is re-queried on every call so installation changes are picked up.
	 */
	Boolean readState(Signal signal) {
		IoOption io = getSelection(signal);
		Boolean physical = null;
		if (io.getSource().isDigital()) {
			for (DigitalIO dio : installationAPI.getIOModel().getIOs(DigitalIO.class)) {
				if (dio.getDefaultName().equals(io.getId())) {
					physical = dio.isResolvable() ? Boolean.valueOf(dio.getValue()) : null;
					break;
				}
			}
		} else if (io.getSource() == IoOption.Source.MODBUS) {
			for (ModbusIO mio : installationAPI.getIOModel().getIOs(ModbusIO.class)) {
				if (mio.getName().equals(io.getId())) {
					physical = mio.isResolvable() ? Boolean.valueOf(mio.getValue() > 0) : null;
					break;
				}
			}
		}
		if (physical == null) {
			return null;
		}
		return physical.booleanValue() != isInverted(signal);
	}

	/** One-line incognito program that drives an output signal to the logical state {@code on}. */
	String buildCommand(Signal signal, boolean on) {
		IoOption io = getSelection(signal);
		String scriptVar = "\"" + io.getId().replace("\"", "") + "\"";
		return "def inkognito():\n  " + IoScript.write(io, scriptVar, on, isInverted(signal)) + "\nend\n";
	}

	/** Enabled and all seven signals mapped: the DIO Tender program can run against this installation. */
	public boolean isDefined() {
		return isEnabled() && isMapped();
	}

	/** When disabled, the before-start block (questions + m21 watcher) is not written to the program preamble. */
	public boolean isEnabled() {
		return model.get(ENABLED_KEY, false);
	}

	public void setEnabled(boolean enabled) {
		model.set(ENABLED_KEY, enabled);
	}

	/** All seven signals are mapped to a Modbus signal. */
	public boolean isMapped() {
		for (Signal signal : Signal.values()) {
			if (getSignal(signal).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Dropdown entries for one direction: placeholder, then the standard / configurable / tool digital I/O,
	 * then the Modbus signals configured in Installation > Fieldbus > MODBUS, each group under a header.
	 */
	public List<IoOption> getOptions(boolean input) {
		List<IoOption> digital = new ArrayList<IoOption>();
		for (DigitalIO io : installationAPI.getIOModel().getIOs(DigitalIO.class)) {
			if (io.isInput() == input) {
				IoOption option = IoOption.digital(io);
				if (option != null) {
					digital.add(option);
				}
			}
		}
		List<IoOption> modbus = new ArrayList<IoOption>();
		for (ModbusIO io : installationAPI.getIOModel().getIOs(ModbusIO.class)) {
			if (io.isInput() == input) {
				modbus.add(IoOption.modbus(io));
			}
		}
		Collections.sort(modbus, new java.util.Comparator<IoOption>() {
			@Override
			public int compare(IoOption a, IoOption b) {
				return a.getId().compareTo(b.getId());
			}
		});

		List<IoOption> options = new ArrayList<IoOption>();
		options.add(IoOption.NOT_SELECTED);
		if (!digital.isEmpty()) {
			options.add(IoOption.header(input ? "--- Digital inputs ---" : "--- Digital outputs ---"));
			options.addAll(digital);
		}
		if (!modbus.isEmpty()) {
			options.add(IoOption.header(input ? "--- Modbus inputs ---" : "--- Modbus outputs ---"));
			options.addAll(modbus);
		}
		return options;
	}

	/** Stored id: Modbus signal name, or the digital I/O default name (e.g. digital_in[2]). Empty if unmapped. */
	public String getSignal(Signal signal) {
		return model.get(signal.getModelKey(), "");
	}

	public IoOption getSelection(Signal signal) {
		String id = getSignal(signal);
		if (id.isEmpty()) {
			return IoOption.NOT_SELECTED;
		}
		// selections saved before digital I/O support have no source key: they are Modbus
		IoOption.Source source = IoOption.Source.valueOf(model.get(sourceKey(signal), IoOption.Source.MODBUS.name()));
		return IoOption.stored(source, id, model.get(pinKey(signal), -1));
	}

	public void setSignal(Signal signal, IoOption option) {
		if (option == null || option.getSource() == IoOption.Source.NONE || !option.isSelectable()) {
			model.remove(signal.getModelKey());
			model.remove(sourceKey(signal));
			model.remove(pinKey(signal));
		} else {
			model.set(signal.getModelKey(), option.getId());
			model.set(sourceKey(signal), option.getSource().name());
			model.set(pinKey(signal), option.getPin());
		}
	}

	/** The signal's logic is inverted: outputs are written inverted, inputs are read inverted. */
	public boolean isInverted(Signal signal) {
		return model.get(invertKey(signal), false);
	}

	public void setInverted(Signal signal, boolean inverted) {
		if (inverted) {
			model.set(invertKey(signal), true);
		} else {
			model.remove(invertKey(signal));
		}
	}

	private static String invertKey(Signal signal) {
		return signal.getModelKey() + "_invert";
	}

	private static String sourceKey(Signal signal) {
		return signal.getModelKey() + "_source";
	}

	private static String pinKey(Signal signal) {
		return signal.getModelKey() + "_pin";
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		if (isMapped()) {
			writeConstants(writer);
			writeHelpers(writer);
		} else {
			writer.appendLine("# DIO Tender: Modbus signals not mapped (Installation > DIO Tender)");
		}
		// The DIO Tender program node is undefined unless enabled and mapped, so it never runs without this block.
		if (isEnabled() && isMapped()) {
			// not writer.runThread(): it emits "run m21_watch" without "()", a URScript syntax error
			writer.appendLine(M21_WATCH_THREAD + " = run m21_watch()");
		} else {
			writer.appendLine("textmsg(\"DIO Tender not enabled. Before start scripts omitted\")");
		}
	}

	private void writeConstants(ScriptWriter writer) {
		writer.appendLine("# ---- DIO Tender: Modbus signal map (Installation > DIO Tender) ----");
		for (Signal signal : Signal.values()) {
			writer.appendLine(signal.getScriptVar() + " = \"" + getSignal(signal).replace("\"", "") + "\"");
		}
		writer.appendLine("G1 = 0 # tool digital out, HIGH = gripper closed");
		writer.appendLine("G2 = 1");
		writer.appendLine("VISE_TIME = " + VISE_TIME);
		writer.appendLine("MFIN_PULSE = " + MFIN_PULSE);
		writer.appendLine("DOOR_TIMEOUT = " + DOOR_TIMEOUT);
		writer.appendLine("global tend_req = \"idle\" # \"idle\" = idle, \"unload\" = m21 latched, \"load\" = unload done");
	}

	/**
	 * mb_on(sig) / mb_set(sig, on) take a SIG_* name. Signals mapped to digital I/O are dispatched by name to the
	 * digital I/O call; everything else is Modbus (0 = off, >0 = on). Names kept as mb_* so existing Script nodes still work.
	 */
	private void writeSignalAccess(ScriptWriter writer) {
		writer.appendLine("def mb_on(sig):");
		boolean first = true;
		for (Signal signal : Signal.values()) {
			IoOption io = getSelection(signal);
			if (signal.isInput() && (io.getSource().isDigital() || isInverted(signal))) {
				branch(writer, first, "sig == " + signal.getScriptVar());
				writer.appendLine("return " + IoScript.read(io, signal.getScriptVar(), isInverted(signal)));
				first = false;
			}
		}
		if (!first) {
			writer.elseCondition();
		}
		writer.appendLine("return modbus_get_signal_status(sig, False) > 0");
		if (!first) {
			writer.end();
		}
		writer.end();

		writer.appendLine("def mb_set(sig, on):");
		first = true;
		for (Signal signal : Signal.values()) {
			IoOption io = getSelection(signal);
			if (!signal.isInput() && (io.getSource().isDigital() || isInverted(signal))) {
				branch(writer, first, "sig == " + signal.getScriptVar());
				if (io.getSource().isDigital()) {
					writer.appendLine(io.writeStatement(isInverted(signal) ? "not on" : "on"));
				} else {
					// inverted Modbus output
					writer.ifCondition("on");
					writer.appendLine("modbus_set_output_register(sig, 0, False)");
					writer.elseCondition();
					writer.appendLine("modbus_set_output_register(sig, 1, False)");
					writer.end();
				}
				first = false;
			}
		}
		if (!first) {
			writer.elseCondition();
		}
		writer.ifCondition("on");
		writer.appendLine("modbus_set_output_register(sig, 1, False)");
		writer.elseCondition();
		writer.appendLine("modbus_set_output_register(sig, 0, False)");
		writer.end();
		if (!first) {
			writer.end();
		}
		writer.end();
	}

	private static void branch(ScriptWriter writer, boolean first, String condition) {
		if (first) {
			writer.ifCondition(condition);
		} else {
			writer.elseIfCondition(condition);
		}
	}

	private void writeHelpers(ScriptWriter writer) {
		writer.appendLine("def set_req():");
		writer.appendLine("global tend_req = \"unload\"");
		writer.end();

		writer.appendLine("def fail(msg):");
		writer.appendLine("popup(msg, \"Machine tending\", error = True, blocking = True)");
		writer.appendLine("halt");
		writer.end();

		writeSignalAccess(writer);

		writer.appendLine("def wait_mb(sig, timeout):");
		writer.appendLine("t = 0");
		writer.whileNot("mb_on(sig)");
		writer.ifCondition("t > timeout");
		writer.appendLine("fail(str_cat(\"Timeout waiting for \", sig))");
		writer.end();
		writer.appendLine("t = t + get_steptime()");
		writer.sync();
		writer.end();
		writer.end();

		// m21 is a pulse: latch the rising edge so it's never missed
		writer.defineThread("m21_watch");
		writer.appendLine("last = False");
		writer.whileTrue();
		writer.appendLine("now = mb_on(SIG_M21)");
		writer.ifCondition("now and not last");
		writer.appendLine("set_req()");
		writer.appendLine("textmsg(\"m21 latched\")");
		writer.end();
		writer.appendLine("last = now");
		writer.sync();
		writer.end();
		writer.end();

		writer.appendLine("def vise(sig, open):");
		writer.appendLine("mb_set(sig, open)");
		writer.appendLine("sleep(VISE_TIME)");
		writer.end();

		writer.appendLine("def door(open):");
		writer.appendLine("mb_set(SIG_DOOR, open)");
		writer.ifCondition("open");
		writer.appendLine("wait_mb(SIG_DOOR_OPENED, DOOR_TIMEOUT)");
		writer.elseCondition();
		writer.appendLine("wait_mb(SIG_DOOR_CLOSED, DOOR_TIMEOUT)");
		writer.end();
		writer.end();

		writer.defineFunction("release_machine");
		writer.appendLine("door(False)");
		writer.appendLine("mb_set(SIG_MFIN, True)");
		writer.appendLine("sleep(MFIN_PULSE)");
		writer.appendLine("mb_set(SIG_MFIN, False)");
		writer.end();
	}
}
