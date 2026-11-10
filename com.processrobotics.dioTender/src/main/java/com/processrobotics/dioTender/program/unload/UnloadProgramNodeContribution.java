package com.processrobotics.dioTender.program.unload;

import com.processrobotics.dioTender.program.TemplateComments;
import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.CreationContext.NodeCreationType;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;

public class UnloadProgramNodeContribution implements ProgramNodeContribution {

	private final DataModel model;

	public UnloadProgramNodeContribution(ProgramAPIProvider apiProvider, DataModel model, CreationContext context) {
		this.model = model;
		if (context.getNodeCreationType().equals(NodeCreationType.NEW)) {
			TemplateComments.add(apiProvider.getProgramAPI(), this,
					"Enter machine (door is open)",
					"Unload part(s): grip, vise(SIG_VISE1 / SIG_VISE2, True), retreat");
		}
	}

	@Override
	public void openView() {
	}

	@Override
	public void closeView() {
	}

	@Override
	public String getTitle() {
		return "Unload (OP1)";
	}

	@Override
	public boolean isDefined() {
		return true;
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		writer.appendLine("textmsg(\"DIO Tender: unload\")");
		writer.writeChildren();
		// unload complete: next step is load
		writer.appendLine("global tend_req = \"load\"");
	}
}
