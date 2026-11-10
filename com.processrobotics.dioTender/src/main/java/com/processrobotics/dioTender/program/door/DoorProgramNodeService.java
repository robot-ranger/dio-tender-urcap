package com.processrobotics.dioTender.program.door;

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

public class DoorProgramNodeService implements SwingProgramNodeService<DoorProgramNodeContribution, DoorProgramNodeView> {

	@Override
	public String getId() {
		return "DioTenderDoor";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		configuration.setChildrenAllowed(false);
	}

	@Override
	public String getTitle(Locale locale) {
		return "DIO Tender Door";
	}

	@Override
	public DoorProgramNodeView createView(ViewAPIProvider apiProvider) {
		SystemAPI systemAPI = apiProvider.getSystemAPI();
		Style style = systemAPI.getSoftwareVersion().getMajorVersion() >= 5 ? new V5Style() : new V3Style();
		return new DoorProgramNodeView(style);
	}

	@Override
	public DoorProgramNodeContribution createNode(ProgramAPIProvider apiProvider, DoorProgramNodeView view,
			DataModel model, CreationContext context) {
		return new DoorProgramNodeContribution(apiProvider, view, model);
	}
}
