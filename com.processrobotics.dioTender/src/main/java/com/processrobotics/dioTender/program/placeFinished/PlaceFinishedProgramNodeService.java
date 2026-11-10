package com.processrobotics.dioTender.program.placeFinished;

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

public class PlaceFinishedProgramNodeService implements SwingProgramNodeService<PlaceFinishedProgramNodeContribution, InfoProgramNodeView<PlaceFinishedProgramNodeContribution>> {

	@Override
	public String getId() {
		return "DioTenderPlaceFinished";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		configuration.setChildrenAllowed(true);
		configuration.setUserInsertable(false);
	}

	@Override
	public String getTitle(Locale locale) {
		return "Place Finished";
	}

	@Override
	public InfoProgramNodeView<PlaceFinishedProgramNodeContribution> createView(ViewAPIProvider apiProvider) {
		SystemAPI systemAPI = apiProvider.getSystemAPI();
		Style style = systemAPI.getSoftwareVersion().getMajorVersion() >= 5 ? new V5Style() : new V3Style();
		return new InfoProgramNodeView<PlaceFinishedProgramNodeContribution>(style,
				"Place the FINISHED part held in G1 at the outfeed.<br><br>Teach the approach, gripper open and retreat as children of this node.");
	}

	@Override
	public PlaceFinishedProgramNodeContribution createNode(ProgramAPIProvider apiProvider,
			InfoProgramNodeView<PlaceFinishedProgramNodeContribution> view, DataModel model, CreationContext context) {
		return new PlaceFinishedProgramNodeContribution(apiProvider, model, context);
	}
}
