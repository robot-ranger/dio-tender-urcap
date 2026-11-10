package com.processrobotics.dioTender.program.pickRaw;

import com.processrobotics.dioTender.program.InfoProgramNodeView;
import com.processrobotics.dioTender.style.Style;
import com.processrobotics.dioTender.style.V3Style;
import com.processrobotics.dioTender.style.V5Style;
import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.ContributionConfiguration;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;
import com.ur.urcap.api.domain.SystemAPI;
import com.ur.urcap.api.domain.data.DataModel;

import java.util.Locale;

public class PickRawProgramNodeService implements SwingProgramNodeService<PickRawProgramNodeContribution, InfoProgramNodeView<PickRawProgramNodeContribution>> {

	@Override
	public String getId() {
		return "DioTenderPickRaw";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		configuration.setChildrenAllowed(true);
		configuration.setUserInsertable(false);
	}

	@Override
	public String getTitle(Locale locale) {
		return "Pick Raw";
	}

	@Override
	public InfoProgramNodeView<PickRawProgramNodeContribution> createView(ViewAPIProvider apiProvider) {
		SystemAPI systemAPI = apiProvider.getSystemAPI();
		Style style = systemAPI.getSoftwareVersion().getMajorVersion() >= 5 ? new V5Style() : new V3Style();
		return new InfoProgramNodeView<PickRawProgramNodeContribution>(style,
				"Pick a RAW part from the infeed with G1.<br><br>Teach the approach, gripper close and retreat as children of this node. Both grippers are empty when this node starts.");
	}

	@Override
	public PickRawProgramNodeContribution createNode(ProgramAPIProvider apiProvider,
			InfoProgramNodeView<PickRawProgramNodeContribution> view, DataModel model, CreationContext context) {
		return new PickRawProgramNodeContribution(apiProvider, model, context);
	}
}
