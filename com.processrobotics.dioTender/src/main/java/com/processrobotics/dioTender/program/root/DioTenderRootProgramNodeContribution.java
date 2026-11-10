package com.processrobotics.dioTender.program.root;

import com.processrobotics.dioTender.installation.DioTenderInstallationNodeContribution;
import com.processrobotics.dioTender.program.pickRaw.PickRawProgramNodeService;
import com.processrobotics.dioTender.program.placeFinished.PlaceFinishedProgramNodeService;
import com.processrobotics.dioTender.program.tendSwap.TendSwapProgramNodeService;
import com.processrobotics.dioTender.program.waitM21.WaitM21ProgramNodeService;
import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.CreationContext.NodeCreationType;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.ProgramAPI;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.program.ProgramModel;
import com.ur.urcap.api.domain.program.nodes.ProgramNodeFactory;
import com.ur.urcap.api.domain.program.structure.TreeNode;
import com.ur.urcap.api.domain.program.structure.TreeStructureException;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.undoredo.UndoableChanges;

public class DioTenderRootProgramNodeContribution implements ProgramNodeContribution {

	private final ProgramAPI programAPI;
	private final DataModel model;

	public DioTenderRootProgramNodeContribution(ProgramAPIProvider apiProvider, DataModel model, CreationContext context) {
		this.programAPI = apiProvider.getProgramAPI();
		this.model = model;
		if (context.getNodeCreationType().equals(NodeCreationType.NEW)) {
			buildTemplateTree();
		}
	}

	private void buildTemplateTree() {
		final ProgramModel programModel = programAPI.getProgramModel();
		final ProgramNodeFactory nf = programModel.getProgramNodeFactory();
		final TreeNode root = programModel.getRootTreeNode(this);
		programAPI.getUndoRedoManager().recordChanges(new UndoableChanges() {
			@Override
			public void executeChanges() {
				try {
					root.addChild(nf.createURCapProgramNode(PickRawProgramNodeService.class));
					root.addChild(nf.createURCapProgramNode(WaitM21ProgramNodeService.class));
					root.addChild(nf.createURCapProgramNode(TendSwapProgramNodeService.class));
					root.addChild(nf.createURCapProgramNode(PlaceFinishedProgramNodeService.class));
					// handshake order must not change
					root.setChildSequenceLocked(true);
				} catch (TreeStructureException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void openView() {
	}

	@Override
	public void closeView() {
	}

	@Override
	public String getTitle() {
		return "DIO Tender";
	}

	@Override
	public boolean isDefined() {
		DioTenderInstallationNodeContribution installation = getInstallation();
		return installation != null && installation.isDefined();
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		writer.ifCondition(DioTenderInstallationNodeContribution.M21_WATCH_THREAD + " != \"\"");
		writer.writeChildren();
		writer.end();
	}

	private DioTenderInstallationNodeContribution getInstallation() {
		return programAPI.getInstallationNode(DioTenderInstallationNodeContribution.class);
	}
}
