package com.processrobotics.dioTender.program.waitM21;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;

/** Blocks until m21_watch latches a tend request, so PolyScope highlights this node while the robot waits. */
public class WaitM21ProgramNodeContribution implements ProgramNodeContribution {

	private final DataModel model;

	public WaitM21ProgramNodeContribution(ProgramAPIProvider apiProvider, DataModel model, CreationContext context) {
		this.model = model;
	}

	@Override
	public void openView() {
	}

	@Override
	public void closeView() {
	}

	@Override
	public String getTitle() {
		return "Wait for M21";
	}

	@Override
	public boolean isDefined() {
		return true;
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		// wait for m21 (latched by m21_watch -> set_req() -> tend_req = "unload")
		writer.appendLine("while tend_req == \"idle\":");
		writer.appendLine("sync()");
		writer.appendLine("end");
	}
}
