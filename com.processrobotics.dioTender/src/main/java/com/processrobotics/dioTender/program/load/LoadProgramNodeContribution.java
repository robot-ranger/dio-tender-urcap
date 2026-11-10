package com.processrobotics.dioTender.program.load;

import com.processrobotics.dioTender.program.TemplateComments;
import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.CreationContext.NodeCreationType;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;

public class LoadProgramNodeContribution implements ProgramNodeContribution {

	private final DataModel model;

	public LoadProgramNodeContribution(ProgramAPIProvider apiProvider, DataModel model, CreationContext context) {
		this.model = model;
		if (context.getNodeCreationType().equals(NodeCreationType.NEW)) {
			TemplateComments.add(apiProvider.getProgramAPI(), this,
					"Load part(s): place, vise(SIG_VISE1 / SIG_VISE2, False), release, retreat",
					"Exit machine: robot clear of door");
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
		return "Load (OP2)";
	}

	@Override
	public boolean isDefined() {
		return true;
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		writer.appendLine("textmsg(\"DIO Tender: load\")");
		writer.writeChildren();

	}
}
