package com.processrobotics.dioTender.program;

import com.ur.urcap.api.domain.ProgramAPI;
import com.ur.urcap.api.domain.program.ProgramModel;
import com.ur.urcap.api.domain.program.nodes.ProgramNodeFactory;
import com.ur.urcap.api.domain.program.structure.TreeNode;
import com.ur.urcap.api.domain.program.structure.TreeStructureException;
import com.ur.urcap.api.domain.undoredo.UndoableChanges;
import com.ur.urcap.api.contribution.ProgramNodeContribution;

/** Adds comment nodes as teaching hints under a newly created template child node. */
public class TemplateComments {
	public static void add(final ProgramAPI programAPI, ProgramNodeContribution node, final String... comments) {
		final ProgramModel programModel = programAPI.getProgramModel();
		final ProgramNodeFactory nf = programModel.getProgramNodeFactory();
		final TreeNode root = programModel.getRootTreeNode(node);
		programAPI.getUndoRedoManager().recordChanges(new UndoableChanges() {
			@Override
			public void executeChanges() {
				try {
					for (String comment : comments) {
						root.addChild(nf.createCommentNode().setComment(comment));
					}
				} catch (TreeStructureException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
