package com.processrobotics.dioTender.program.door;

import com.processrobotics.dioTender.installation.DioTenderInstallationNodeContribution;
import com.processrobotics.dioTender.installation.IoOption;
import com.processrobotics.dioTender.installation.Signal;
import com.processrobotics.dioTender.program.IoScript;
import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.ProgramAPI;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.undoredo.UndoableChanges;

/** Opens or closes the door with the I/O mapped in the installation, then waits for the matching feedback. */
public class DoorProgramNodeContribution implements ProgramNodeContribution {

	private static final String OPEN_KEY = "open";

	private final ProgramAPI programAPI;
	private final DoorProgramNodeView view;
	private final DataModel model;

	public DoorProgramNodeContribution(ProgramAPIProvider apiProvider, DoorProgramNodeView view, DataModel model) {
		this.programAPI = apiProvider.getProgramAPI();
		this.view = view;
		this.model = model;
	}

	public boolean isOpen() {
		return model.get(OPEN_KEY, true);
	}

	public void setOpen(final boolean open) {
		programAPI.getUndoRedoManager().recordChanges(new UndoableChanges() {
			@Override
			public void executeChanges() {
				model.set(OPEN_KEY, open);
			}
		});
	}

	/** Mapped output, feedback input and their names for the view. Empty strings when unmapped. */
	public String getMappingText() {
		DioTenderInstallationNodeContribution installation = getInstallation();
		if (installation == null) {
			return "";
		}
		Signal feedback = isOpen() ? Signal.DOOR_OPENED : Signal.DOOR_CLOSED;
		return "Output: " + IoScript.describe(installation.getSelection(Signal.DOOR_OUTPUT))
				+ "   Feedback: " + IoScript.describe(installation.getSelection(feedback));
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
		return isOpen() ? "Door Open" : "Door Close";
	}

	@Override
	public boolean isDefined() {
		DioTenderInstallationNodeContribution installation = getInstallation();
		return installation != null && installation.isMapped();
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		DioTenderInstallationNodeContribution installation = getInstallation();
		boolean open = isOpen();
		Signal feedback = open ? Signal.DOOR_OPENED : Signal.DOOR_CLOSED;
		IoOption output = installation.getSelection(Signal.DOOR_OUTPUT);
		IoOption input = installation.getSelection(feedback);

		writer.appendLine(IoScript.write(output, Signal.DOOR_OUTPUT.getScriptVar(), open));
		writer.appendLine("dt_t = 0");
		writer.whileNot(IoScript.read(input, feedback.getScriptVar()));
		writer.ifCondition("dt_t > DOOR_TIMEOUT");
		writer.appendLine("fail(\"Timeout waiting for door " + (open ? "opened" : "closed") + "\")");
		writer.end();
		writer.appendLine("dt_t = dt_t + get_steptime()");
		writer.sync();
		writer.end();
	}

	private DioTenderInstallationNodeContribution getInstallation() {
		return programAPI.getInstallationNode(DioTenderInstallationNodeContribution.class);
	}
}
