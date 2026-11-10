package com.processrobotics.dioTender.program.placeFinished;

import com.processrobotics.dioTender.program.TemplateComments;
import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.CreationContext.NodeCreationType;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;

public class PlaceFinishedProgramNodeContribution implements ProgramNodeContribution {

	private final DataModel model;

	public PlaceFinishedProgramNodeContribution(ProgramAPIProvider apiProvider, DataModel model, CreationContext context) {
		this.model = model;
		if (context.getNodeCreationType().equals(NodeCreationType.NEW)) {
			TemplateComments.add(apiProvider.getProgramAPI(), this,
					"Approach outfeed",
					"Open G1 to release FINISHED part",
					"Retreat from outfeed");
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
		return "Place Finished";
	}

	@Override
	public boolean isDefined() {
		return true;
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		writer.appendLine("textmsg(\"DIO Tender: place finished\")");
		writer.writeChildren();
	}
}
