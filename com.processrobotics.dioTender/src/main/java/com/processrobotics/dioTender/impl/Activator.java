package com.processrobotics.dioTender.impl;

import com.processrobotics.dioTender.installation.DioTenderInstallationNodeService;
import com.processrobotics.dioTender.program.door.DoorProgramNodeService;
import com.processrobotics.dioTender.program.vise.ViseProgramNodeService;
import com.processrobotics.dioTender.program.pickRaw.PickRawProgramNodeService;
import com.processrobotics.dioTender.program.placeFinished.PlaceFinishedProgramNodeService;
import com.processrobotics.dioTender.program.root.DioTenderRootProgramNodeService;
import com.processrobotics.dioTender.program.tendSwap.TendSwapProgramNodeService;
import com.processrobotics.dioTender.program.waitM21.WaitM21ProgramNodeService;
import com.processrobotics.dioTender.program.unload.UnloadProgramNodeService;
import com.processrobotics.dioTender.program.load.LoadProgramNodeService;
import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeService;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	@Override
	public void start(final BundleContext context) throws Exception {
		context.registerService(SwingInstallationNodeService.class, new DioTenderInstallationNodeService(), null);
		context.registerService(SwingProgramNodeService.class, new DioTenderRootProgramNodeService(), null);
		context.registerService(SwingProgramNodeService.class, new PickRawProgramNodeService(), null);
		context.registerService(SwingProgramNodeService.class, new WaitM21ProgramNodeService(), null);
		context.registerService(SwingProgramNodeService.class, new TendSwapProgramNodeService(), null);
		context.registerService(SwingProgramNodeService.class, new UnloadProgramNodeService(), null);
		context.registerService(SwingProgramNodeService.class, new LoadProgramNodeService(), null);
		context.registerService(SwingProgramNodeService.class, new PlaceFinishedProgramNodeService(), null);
		context.registerService(SwingProgramNodeService.class, new DoorProgramNodeService(), null);
		context.registerService(SwingProgramNodeService.class, new ViseProgramNodeService(), null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}
}
