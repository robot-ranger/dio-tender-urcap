package com.processrobotics.dioTender.program.pickRaw;

import com.processrobotics.dioTender.program.TemplateComments;
import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.CreationContext.NodeCreationType;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;

public class PickRawProgramNodeContribution implements ProgramNodeContribution {

	private final DataModel model;

	public PickRawProgramNodeContribution(ProgramAPIProvider apiProvider, DataModel model, CreationContext context) {
		this.model = model;
		if (context.getNodeCreationType().equals(NodeCreationType.NEW)) {
			TemplateComments.add(apiProvider.getProgramAPI(), this,
					"Approach infeed",
					"Close G1 on RAW part",
					"Retreat from infeed");
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
		return "Pick Raw";
	}

	@Override
	public boolean isDefined() {
		return true;
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		writer.appendLine("textmsg(\"DIO Tender: pick raw\")");
		writer.writeChildren();
	}
}
