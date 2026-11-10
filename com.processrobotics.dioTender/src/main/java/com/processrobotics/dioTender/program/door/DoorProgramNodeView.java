package com.processrobotics.dioTender.program.door;

import com.processrobotics.dioTender.style.LogoRow;
import com.processrobotics.dioTender.style.Style;
import com.ur.urcap.api.contribution.ContributionProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeView;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DoorProgramNodeView implements SwingProgramNodeView<DoorProgramNodeContribution> {

	private final Style style;
	private final JRadioButton openButton = new JRadioButton("Open");
	private final JRadioButton closeButton = new JRadioButton("Close");
	private final JLabel mapping = new JLabel(" ");
	private boolean updating = false;

	public DoorProgramNodeView(Style style) {
		this.style = style;
	}

	@Override
	public void buildUI(JPanel jPanel, final ContributionProvider<DoorProgramNodeContribution> provider) {
		jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
		JLabel info = new JLabel("<html>Sets the Door Output, then waits for Door Opened / Door Closed. Fails if the door does not respond in time.</html>");
		info.setAlignmentX(Component.LEFT_ALIGNMENT);
		jPanel.add(info);
		jPanel.add(Style.createVerticalSpacing(Style.VERTICAL_SPACING * 2));

		Font font = new Font("DejaVu Sans", Font.PLAIN, Style.FONT_VERY_LARGE);
		openButton.setFont(font);
		closeButton.setFont(font);
		ButtonGroup group = new ButtonGroup();
		group.add(openButton);
		group.add(closeButton);
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!updating) {
					provider.get().setOpen(true);
				}
			}
		});
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!updating) {
					provider.get().setOpen(false);
				}
			}
		});

		Box row = Box.createHorizontalBox();
		row.setAlignmentX(Component.LEFT_ALIGNMENT);
		row.add(Style.buildLabel("Door:"));
		row.add(Style.createHorizontalSpacing());
		row.add(openButton);
		row.add(Style.createHorizontalSpacing());
		row.add(closeButton);
		jPanel.add(row);
		jPanel.add(Style.createVerticalSpacing());

		mapping.setAlignmentX(Component.LEFT_ALIGNMENT);
		jPanel.add(mapping);
		jPanel.add(Box.createVerticalGlue());
		Box logo = LogoRow.create(75);
		logo.setAlignmentX(Component.LEFT_ALIGNMENT);
		jPanel.add(logo);
	}

	public void update(DoorProgramNodeContribution contribution) {
		updating = true;
		try {
			openButton.setSelected(contribution.isOpen());
			closeButton.setSelected(!contribution.isOpen());
			mapping.setText(contribution.getMappingText());
		} finally {
			updating = false;
		}
	}
}
