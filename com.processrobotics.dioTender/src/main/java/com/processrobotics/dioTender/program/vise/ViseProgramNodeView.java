package com.processrobotics.dioTender.program.vise;

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
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ViseProgramNodeView implements SwingProgramNodeView<ViseProgramNodeContribution> {

	private final Style style;
	private final JRadioButton vise1Button = new JRadioButton("Vise 1");
	private final JRadioButton vise2Button = new JRadioButton("Vise 2");
	private final JRadioButton openButton = new JRadioButton("Open");
	private final JRadioButton closeButton = new JRadioButton("Close");
	private final JTextField dwellField = Style.buildInputField();
	private boolean updating = false;

	public ViseProgramNodeView(Style style) {
		this.style = style;
	}

	@Override
	public void buildUI(JPanel jPanel, final ContributionProvider<ViseProgramNodeContribution> provider) {
		jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
		JLabel info = new JLabel("<html>Sets the vise output, then dwells for the set time so the vise can settle (there is no vise feedback signal).</html>");
		info.setAlignmentX(Component.LEFT_ALIGNMENT);
		jPanel.add(info);
		jPanel.add(Style.createVerticalSpacing(Style.VERTICAL_SPACING * 2));

		Font font = new Font("DejaVu Sans", Font.PLAIN, Style.FONT_VERY_LARGE);
		for (JRadioButton b : new JRadioButton[] { vise1Button, vise2Button, openButton, closeButton }) {
			b.setFont(font);
		}
		ButtonGroup viseGroup = new ButtonGroup();
		viseGroup.add(vise1Button);
		viseGroup.add(vise2Button);
		ButtonGroup actionGroup = new ButtonGroup();
		actionGroup.add(openButton);
		actionGroup.add(closeButton);

		vise1Button.addActionListener(viseListener(provider, 1));
		vise2Button.addActionListener(viseListener(provider, 2));
		openButton.addActionListener(openListener(provider, true));
		closeButton.addActionListener(openListener(provider, false));

		jPanel.add(row("Vise:", vise1Button, vise2Button));
		jPanel.add(Style.createVerticalSpacing());
		jPanel.add(row("Action:", openButton, closeButton));
		jPanel.add(Style.createVerticalSpacing());

		dwellField.setEditable(false);
		dwellField.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				ViseProgramNodeContribution c = provider.get();
				c.getDwellKeyboard().show(dwellField, c.getDwellCallback());
			}
		});
		Box dwellRow = Box.createHorizontalBox();
		dwellRow.setAlignmentX(Component.LEFT_ALIGNMENT);
		dwellRow.add(Style.buildLabel("Dwell:"));
		dwellRow.add(Style.createHorizontalSpacing());
		dwellRow.add(dwellField);
		dwellRow.add(Style.createHorizontalSpacing());
		dwellRow.add(Style.buildLabel("s"));
		jPanel.add(dwellRow);

		jPanel.add(Box.createVerticalGlue());
		Box logo = LogoRow.create(75);
		logo.setAlignmentX(Component.LEFT_ALIGNMENT);
		jPanel.add(logo);
	}

	private Box row(String caption, JRadioButton first, JRadioButton second) {
		Box row = Box.createHorizontalBox();
		row.setAlignmentX(Component.LEFT_ALIGNMENT);
		row.add(Style.buildLabel(caption));
		row.add(Style.createHorizontalSpacing());
		row.add(first);
		row.add(Style.createHorizontalSpacing());
		row.add(second);
		return row;
	}

	private ActionListener viseListener(final ContributionProvider<ViseProgramNodeContribution> provider, final int vise) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!updating) {
					provider.get().setVise(vise);
				}
			}
		};
	}

	private ActionListener openListener(final ContributionProvider<ViseProgramNodeContribution> provider, final boolean open) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!updating) {
					provider.get().setOpen(open);
				}
			}
		};
	}

	public void update(ViseProgramNodeContribution contribution) {
		updating = true;
		try {
			vise1Button.setSelected(contribution.getVise() == 1);
			vise2Button.setSelected(contribution.getVise() == 2);
			openButton.setSelected(contribution.isOpen());
			closeButton.setSelected(!contribution.isOpen());
			dwellField.setText(String.valueOf(contribution.getDwell()));
		} finally {
			updating = false;
		}
	}
}
