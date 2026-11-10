package com.processrobotics.dioTender.installation;

import com.processrobotics.dioTender.style.Style;
import com.processrobotics.dioTender.style.V3Style;
import com.processrobotics.dioTender.style.V5Style;
import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.installation.ContributionConfiguration;
import com.ur.urcap.api.contribution.installation.CreationContext;
import com.ur.urcap.api.contribution.installation.InstallationAPIProvider;
import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeService;
import com.ur.urcap.api.domain.SystemAPI;
import com.ur.urcap.api.domain.data.DataModel;

import java.util.Locale;

public class DioTenderInstallationNodeService implements SwingInstallationNodeService<DioTenderInstallationNodeContribution, DioTenderInstallationNodeView> {

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
	}

	@Override
	public String getTitle(Locale locale) {
		return "DIO Tender";
	}

	@Override
	public DioTenderInstallationNodeView createView(ViewAPIProvider apiProvider) {
		SystemAPI systemAPI = apiProvider.getSystemAPI();
		Style style = systemAPI.getSoftwareVersion().getMajorVersion() >= 5 ? new V5Style() : new V3Style();
		return new DioTenderInstallationNodeView(style);
	}

	@Override
	public DioTenderInstallationNodeContribution createInstallationNode(InstallationAPIProvider apiProvider, DioTenderInstallationNodeView view, DataModel model, CreationContext context) {
		return new DioTenderInstallationNodeContribution(apiProvider, model, view);
	}
}
