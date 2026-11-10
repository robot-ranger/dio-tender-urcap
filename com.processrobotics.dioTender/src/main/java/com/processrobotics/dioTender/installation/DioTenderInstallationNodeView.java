package com.processrobotics.dioTender.installation;

import com.processrobotics.dioTender.style.LogoRow;
import com.processrobotics.dioTender.style.Style;
import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeView;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class DioTenderInstallationNodeView implements SwingInstallationNodeView<DioTenderInstallationNodeContribution> {

	private static final int ROW_HEIGHT = 30;
	private static final Dimension COMBO_SIZE = new Dimension(320, ROW_HEIGHT);

	private final Style style;
	private final Map<Signal, JComboBox<IoOption>> combos = new EnumMap<Signal, JComboBox<IoOption>>(Signal.class);
	private final Map<Signal, JLabel> labels = new EnumMap<Signal, JLabel>(Signal.class);
	private final Map<Signal, JLabel> warnings = new EnumMap<Signal, JLabel>(Signal.class);
	private final JCheckBox enableCheckBox = new JCheckBox("Enable");
	private DioTenderInstallationNodeContribution contribution;
	// guards against combo listeners writing to the model while combos are repopulated
	private boolean updating = false;

	public DioTenderInstallationNodeView(Style style) {
		this.style = style;
	}

	@Override
	public void buildUI(JPanel jPanel, DioTenderInstallationNodeContribution installationNode) {
		this.contribution = installationNode;
		jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

		enableCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		enableCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!updating && contribution != null) {
					contribution.setEnabled(enableCheckBox.isSelected());
				}
			}
		});
		jPanel.add(enableCheckBox);
		JLabel enableInfo = new JLabel("<html>When enabled, the DIO Tender before-start checks and M21 watcher run at the start of "
				+ "every program using this installation. Leave disabled in installations that don't run DIO Tender.</html>");
		enableInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
		jPanel.add(enableInfo);
		jPanel.add(Style.createVerticalSpacing(Style.VERTICAL_SPACING * 2));

		JLabel info = new JLabel("<html>Map each tender signal to a digital I/O (standard, configurable or tool) or to a "
				+ "Modbus signal configured in Installation &gt; Fieldbus &gt; MODBUS. Inputs list inputs only; outputs list "
				+ "outputs only. Modbus: 0 = off, &gt;0 = on.</html>");
		info.setAlignmentX(Component.LEFT_ALIGNMENT);
		jPanel.add(info);
		jPanel.add(Style.createVerticalSpacing(Style.VERTICAL_SPACING * 2));

		for (Signal signal : Signal.values()) {
			jPanel.add(createSignalRow(signal));
			jPanel.add(Style.createVerticalSpacing());
		}
		sizeLabelsToWidest();

		jPanel.add(Box.createVerticalGlue());
		Box logo = LogoRow.create(75);
		logo.setAlignmentX(Component.LEFT_ALIGNMENT);
		jPanel.add(logo);
	}

	private Box createSignalRow(final Signal signal) {
		Box row = Box.createHorizontalBox();
		row.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel label = Style.buildLabel(signal.getLabel() + (signal.isInput() ? " (in)" : " (out)"));
		labels.put(signal, label);
		row.add(label);
		row.add(Style.createHorizontalSpacing());

		final JComboBox<IoOption> combo = new JComboBox<IoOption>();
		combo.setPreferredSize(COMBO_SIZE);
		combo.setMinimumSize(COMBO_SIZE);
		combo.setMaximumSize(COMBO_SIZE);
		combo.setRenderer(new GroupedRenderer());
		combo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (updating || contribution == null) {
					return;
				}
				IoOption selected = (IoOption) combo.getSelectedItem();
				if (selected == null || !selected.isSelectable()) {
					// group header picked: put the stored selection back
					updating = true;
					try {
						combo.setSelectedItem(contribution.getSelection(signal));
					} finally {
						updating = false;
					}
					return;
				}
				contribution.setSignal(signal, selected);
				warnings.get(signal).setText("");
			}
		});
		combos.put(signal, combo);
		row.add(combo);
		row.add(Style.createHorizontalSpacing());

		JLabel description = new JLabel(signal.getDescription());
		row.add(description);
		row.add(Style.createHorizontalSpacing());

		JLabel warning = new JLabel("");
		warning.setForeground(Style.RED);
		warnings.put(signal, warning);
		row.add(warning);

		row.add(Box.createHorizontalGlue());
		return row;
	}

	/** Give every signal label the width of the widest one so none are clipped and the combos line up. */
	private void sizeLabelsToWidest() {
		int width = 0;
		for (JLabel label : labels.values()) {
			width = Math.max(width, label.getPreferredSize().width);
		}
		Dimension size = new Dimension(width + Style.HORIZONTAL_SPACING, ROW_HEIGHT);
		for (JLabel label : labels.values()) {
			label.setPreferredSize(size);
			label.setMinimumSize(size);
			label.setMaximumSize(size);
		}
	}

	/** Repopulate every combo from the current digital I/O + Modbus configuration and select the stored signal. */
	void update(DioTenderInstallationNodeContribution installationNode) {
		this.contribution = installationNode;
		updating = true;
		try {
			enableCheckBox.setSelected(installationNode.isEnabled());
			List<IoOption> inputs = installationNode.getOptions(true);
			List<IoOption> outputs = installationNode.getOptions(false);
			for (Signal signal : Signal.values()) {
				List<IoOption> options = signal.isInput() ? inputs : outputs;
				IoOption stored = installationNode.getSelection(signal);
				JComboBox<IoOption> combo = combos.get(signal);
				combo.removeAllItems();
				for (IoOption option : options) {
					combo.addItem(option);
				}
				JLabel warning = warnings.get(signal);
				if (stored.getSource() == IoOption.Source.NONE || options.contains(stored)) {
					warning.setText("");
					combo.setSelectedItem(options.get(options.indexOf(stored)));
				} else {
					// keep the stored mapping visible instead of silently dropping it
					IoOption missing = stored.asMissing();
					combo.addItem(missing);
					combo.setSelectedItem(missing);
					warning.setText("not found in I/O or MODBUS setup");
				}
			}
		} finally {
			updating = false;
		}
	}

	/** Group headers render bold and greyed out; they can't be kept as a selection (see the combo listener). */
	private static class GroupedRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof IoOption && !((IoOption) value).isSelectable()) {
				c.setFont(c.getFont().deriveFont(Font.BOLD));
				c.setForeground(Style.GRAY_2);
				c.setBackground(Style.LIGHT_GRAY_3);
			}
			return c;
		}
	}
}
