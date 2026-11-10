package com.processrobotics.dioTender.program.root;

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

public class DioTenderRootProgramNodeService implements SwingProgramNodeService<DioTenderRootProgramNodeContribution, InfoProgramNodeView<DioTenderRootProgramNodeContribution>> {

	@Override
	public String getId() {
		return "DioTenderRoot";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		configuration.setChildrenAllowed(true);
	}

	@Override
	public String getTitle(Locale locale) {
		return "DIO Tender";
	}

	@Override
	public InfoProgramNodeView<DioTenderRootProgramNodeContribution> createView(ViewAPIProvider apiProvider) {
		SystemAPI systemAPI = apiProvider.getSystemAPI();
		Style style = systemAPI.getSoftwareVersion().getMajorVersion() >= 5 ? new V5Style() : new V3Style();
		return new InfoProgramNodeView<DioTenderRootProgramNodeContribution>(style,
				"Dual-gripper, dual-vise machine tender over Modbus. Runs forever: "
				+ "Pick Raw, Wait for M21, Tend Swap (opens door, sends MFin), Place Finished.<br><br>"
				+ "Enable DIO Tender and map all seven signals in Installation &gt; DIO Tender first; this node stays undefined until you do.<br><br>"
				+ "Both grippers must be empty at program start.");
	}

	@Override
	public DioTenderRootProgramNodeContribution createNode(ProgramAPIProvider apiProvider,
			InfoProgramNodeView<DioTenderRootProgramNodeContribution> view, DataModel model, CreationContext context) {
		return new DioTenderRootProgramNodeContribution(apiProvider, model, context);
	}
}
