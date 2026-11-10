package com.processrobotics.dioTender.program.vise;

import com.processrobotics.dioTender.installation.DioTenderInstallationNodeContribution;
import com.processrobotics.dioTender.installation.Signal;
import com.processrobotics.dioTender.program.IoScript;
import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.ProgramAPI;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.undoredo.UndoableChanges;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputCallback;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardNumberInput;

/** Opens or closes vise 1 or 2 with the I/O mapped in the installation, then dwells for the configured time. */
public class ViseProgramNodeContribution implements ProgramNodeContribution {

	private static final String OPEN_KEY = "open";
	private static final String VISE_KEY = "vise";
	private static final String DWELL_KEY = "dwell";
	private static final double DEFAULT_DWELL = 1.0; // s, same as the installation VISE_TIME
	private static final double MAX_DWELL = 60.0;    // s

	private final ProgramAPIProvider apiProvider;
	private final ProgramAPI programAPI;
	private final ViseProgramNodeView view;
	private final DataModel model;

	public ViseProgramNodeContribution(ProgramAPIProvider apiProvider, ViseProgramNodeView view, DataModel model) {
		this.apiProvider = apiProvider;
		this.programAPI = apiProvider.getProgramAPI();
		this.view = view;
		this.model = model;
	}

	public boolean isOpen() {
		return model.get(OPEN_KEY, true);
	}

	public int getVise() {
		return model.get(VISE_KEY, 1) == 2 ? 2 : 1;
	}

	/** Dwell after the vise action, in seconds. */
	public double getDwell() {
		return model.get(DWELL_KEY, DEFAULT_DWELL);
	}

	public void setDwell(final double seconds) {
		programAPI.getUndoRedoManager().recordChanges(new UndoableChanges() {
			@Override
			public void executeChanges() {
				model.set(DWELL_KEY, seconds);
			}
		});
	}

	public KeyboardNumberInput<Double> getDwellKeyboard() {
		KeyboardNumberInput<Double> keyboard = apiProvider.getUserInterfaceAPI().getUserInteraction()
				.getKeyboardInputFactory().createDoubleKeypadInput();
		keyboard.setErrorValidator(apiProvider.getUserInterfaceAPI().getUserInteraction()
				.getInputValidationFactory().createDoubleRangeValidator(0.0, MAX_DWELL));
		keyboard.setInitialValue(getDwell());
		return keyboard;
	}

	public KeyboardInputCallback<Double> getDwellCallback() {
		return new KeyboardInputCallback<Double>() {
			@Override
			public void onOk(Double value) {
				setDwell(value);
				view.update(ViseProgramNodeContribution.this);
			}
		};
	}

	public void setOpen(final boolean open) {
		programAPI.getUndoRedoManager().recordChanges(new UndoableChanges() {
			@Override
			public void executeChanges() {
				model.set(OPEN_KEY, open);
			}
		});
	}

	public void setVise(final int vise) {
		programAPI.getUndoRedoManager().recordChanges(new UndoableChanges() {
			@Override
			public void executeChanges() {
				model.set(VISE_KEY, vise == 2 ? 2 : 1);
			}
		});
	}

	private Signal getSignal() {
		return getVise() == 2 ? Signal.VISE2 : Signal.VISE1;
	}

	@Override
	public void openView() {
		view.update(this);
	}

	@Override
	public void closeView() {
	}

	@Override
	public String getTitle() {
		return "Vise " + getVise() + (isOpen() ? " Open" : " Close");
	}

	@Override
	public boolean isDefined() {
		DioTenderInstallationNodeContribution installation = getInstallation();
		return installation != null && installation.isMapped();
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		DioTenderInstallationNodeContribution installation = getInstallation();
		Signal signal = getSignal();
		writer.appendLine(IoScript.write(installation.getSelection(signal), signal.getScriptVar(), isOpen()));
		writer.appendLine("sleep(" + getDwell() + ")");
	}

	private DioTenderInstallationNodeContribution getInstallation() {
		return programAPI.getInstallationNode(DioTenderInstallationNodeContribution.class);
	}
}
