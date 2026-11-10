package com.processrobotics.dioTender.program.unload;

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

public class UnloadProgramNodeService implements SwingProgramNodeService<UnloadProgramNodeContribution, InfoProgramNodeView<UnloadProgramNodeContribution>> {

	@Override
	public String getId() {
		return "DioTenderUnload";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		configuration.setChildrenAllowed(true);
		configuration.setUserInsertable(false);
	}

	@Override
	public String getTitle(Locale locale) {
		return "Unload (OP1)";
	}

	@Override
	public InfoProgramNodeView<UnloadProgramNodeContribution> createView(ViewAPIProvider apiProvider) {
		SystemAPI systemAPI = apiProvider.getSystemAPI();
		Style style = systemAPI.getSoftwareVersion().getMajorVersion() >= 5 ? new V5Style() : new V3Style();
		return new InfoProgramNodeView<UnloadProgramNodeContribution>(style,
				"Unload part(s) from the vises. The door is already open.<br><br>Teach the moves as children of this node. Use vise(SIG_VISE1 / SIG_VISE2, True) in Script nodes to open a vise. When this node finishes, tend_req is set to \"load\".");
	}

	@Override
	public UnloadProgramNodeContribution createNode(ProgramAPIProvider apiProvider,
			InfoProgramNodeView<UnloadProgramNodeContribution> view, DataModel model, CreationContext context) {
		return new UnloadProgramNodeContribution(apiProvider, model, context);
	}
}
