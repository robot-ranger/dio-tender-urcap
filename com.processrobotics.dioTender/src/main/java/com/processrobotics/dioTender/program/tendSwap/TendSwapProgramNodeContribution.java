package com.processrobotics.dioTender.program.tendSwap;

import com.processrobotics.dioTender.program.load.LoadProgramNodeService;
import com.processrobotics.dioTender.program.unload.UnloadProgramNodeService;
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

public class TendSwapProgramNodeContribution implements ProgramNodeContribution {

	private final ProgramAPI programAPI;
	private final DataModel model;

	public TendSwapProgramNodeContribution(ProgramAPIProvider apiProvider, DataModel model, CreationContext context) {
		this.programAPI = apiProvider.getProgramAPI();
		this.model = model;
		if (context.getNodeCreationType().equals(NodeCreationType.NEW)) {
			buildTemplateTree();
		}
	}

	/**
	 * Children are fixed to Unload then Load. The sequence is locked so the user can't add, remove or reorder
	 * nodes directly under Tend Swap; Unload and Load each take their own children.
	 */
	private void buildTemplateTree() {
		final ProgramModel programModel = programAPI.getProgramModel();
		final ProgramNodeFactory nf = programModel.getProgramNodeFactory();
		final TreeNode root = programModel.getRootTreeNode(this);
		programAPI.getUndoRedoManager().recordChanges(new UndoableChanges() {
			@Override
			public void executeChanges() {
				try {
					root.addChild(nf.createURCapProgramNode(UnloadProgramNodeService.class));
					root.addChild(nf.createURCapProgramNode(LoadProgramNodeService.class));
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
		return "Tend Swap";
	}

	@Override
	public boolean isDefined() {
		return true;
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		// tend request already latched (Wait for M21 node): open the door
		writer.writeChildren(); // Unload (sets tend_req = "load"), Load
		// door closed + mfin pulse, tend_req = ""
		writer.appendLine("release_machine()");
		writer.appendLine("global tend_req = \"idle\"");
	}
}
