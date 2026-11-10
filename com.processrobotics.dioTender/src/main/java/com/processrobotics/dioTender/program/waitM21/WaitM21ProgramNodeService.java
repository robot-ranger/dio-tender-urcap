package com.processrobotics.dioTender.program.waitM21;

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

public class WaitM21ProgramNodeService implements SwingProgramNodeService<WaitM21ProgramNodeContribution, InfoProgramNodeView<WaitM21ProgramNodeContribution>> {

	@Override
	public String getId() {
		return "DioTenderWaitM21";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		configuration.setChildrenAllowed(false);
		configuration.setUserInsertable(false);
	}

	@Override
	public String getTitle(Locale locale) {
		return "Wait for M21";
	}

	@Override
	public InfoProgramNodeView<WaitM21ProgramNodeContribution> createView(ViewAPIProvider apiProvider) {
		SystemAPI systemAPI = apiProvider.getSystemAPI();
		Style style = systemAPI.getSoftwareVersion().getMajorVersion() >= 5 ? new V5Style() : new V3Style();
		return new InfoProgramNodeView<WaitM21ProgramNodeContribution>(style,
				"Waits here until the machine requests a tend (M21 pulse, latched by the m21_watch thread).<br><br>The robot is holding a RAW part in G1 and is outside the machine.");
	}

	@Override
	public WaitM21ProgramNodeContribution createNode(ProgramAPIProvider apiProvider,
			InfoProgramNodeView<WaitM21ProgramNodeContribution> view, DataModel model, CreationContext context) {
		return new WaitM21ProgramNodeContribution(apiProvider, model, context);
	}
}
