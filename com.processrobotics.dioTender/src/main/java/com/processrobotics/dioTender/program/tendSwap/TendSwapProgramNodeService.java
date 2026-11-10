package com.processrobotics.dioTender.program.tendSwap;

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

public class TendSwapProgramNodeService implements SwingProgramNodeService<TendSwapProgramNodeContribution, InfoProgramNodeView<TendSwapProgramNodeContribution>> {

	@Override
	public String getId() {
		return "DioTenderTendSwap";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		configuration.setChildrenAllowed(true);
		configuration.setUserInsertable(false);
	}

	@Override
	public String getTitle(Locale locale) {
		return "Tend Swap";
	}

	@Override
	public InfoProgramNodeView<TendSwapProgramNodeContribution> createView(ViewAPIProvider apiProvider) {
		SystemAPI systemAPI = apiProvider.getSystemAPI();
		Style style = systemAPI.getSoftwareVersion().getMajorVersion() >= 5 ? new V5Style() : new V3Style();
		return new InfoProgramNodeView<TendSwapProgramNodeContribution>(style,
				"Opens the door, runs Unload then Load, then closes the door and pulses MFin.<br><br>Unload and Load are fixed: teach the moves as their children. The robot must be clear of the door when Load finishes.");
	}

	@Override
	public TendSwapProgramNodeContribution createNode(ProgramAPIProvider apiProvider,
			InfoProgramNodeView<TendSwapProgramNodeContribution> view, DataModel model, CreationContext context) {
		return new TendSwapProgramNodeContribution(apiProvider, model, context);
	}
}
