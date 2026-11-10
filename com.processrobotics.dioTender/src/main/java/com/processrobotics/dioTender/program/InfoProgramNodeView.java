package com.processrobotics.dioTender.program;

import com.processrobotics.dioTender.style.LogoRow;
import com.processrobotics.dioTender.style.Style;
import com.ur.urcap.api.contribution.ContributionProvider;
import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeView;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Component;

/** Read-only view shared by the DIO Tender program nodes: a description and the logo. */
public class InfoProgramNodeView<C extends ProgramNodeContribution> implements SwingProgramNodeView<C> {

	private final Style style;
	private final String html;

	public InfoProgramNodeView(Style style, String html) {
		this.style = style;
		this.html = html;
	}

	@Override
	public void buildUI(JPanel jPanel, ContributionProvider<C> provider) {
		jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
		JLabel info = new JLabel("<html>" + html + "</html>");
		info.setAlignmentX(Component.LEFT_ALIGNMENT);
		jPanel.add(info);
		jPanel.add(Style.createVerticalSpacing(Style.VERTICAL_SPACING * 3));
		jPanel.add(Box.createVerticalGlue());
		Box logo = LogoRow.create(75);
		logo.setAlignmentX(Component.LEFT_ALIGNMENT);
		jPanel.add(logo);
	}
}
