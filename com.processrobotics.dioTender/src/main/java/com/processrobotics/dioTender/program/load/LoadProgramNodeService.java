package com.processrobotics.dioTender.program.load;

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

public class LoadProgramNodeService implements SwingProgramNodeService<LoadProgramNodeContribution, InfoProgramNodeView<LoadProgramNodeContribution>> {

	@Override
	public String getId() {
		return "DioTenderLoad";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		configuration.setChildrenAllowed(true);
		configuration.setUserInsertable(false);
	}

	@Override
	public String getTitle(Locale locale) {
		return "Load (OP2)";
	}

	@Override
	public InfoProgramNodeView<LoadProgramNodeContribution> createView(ViewAPIProvider apiProvider) {
		SystemAPI systemAPI = apiProvider.getSystemAPI();
		Style style = systemAPI.getSoftwareVersion().getMajorVersion() >= 5 ? new V5Style() : new V3Style();
		return new InfoProgramNodeView<LoadProgramNodeContribution>(style,
				"Load part(s) into the vises, then exit the machine.<br><br>Teach the moves as children of this node. Use vise(SIG_VISE1 / SIG_VISE2, False) in Script nodes to close a vise. The robot must be clear of the door when this node finishes: the door closes and MFin pulses next.");
	}

	@Override
	public LoadProgramNodeContribution createNode(ProgramAPIProvider apiProvider,
			InfoProgramNodeView<LoadProgramNodeContribution> view, DataModel model, CreationContext context) {
		return new LoadProgramNodeContribution(apiProvider, model, context);
	}
}
