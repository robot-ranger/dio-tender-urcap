package com.processrobotics.dioTender.installation;

import com.processrobotics.dioTender.style.LogoRow;
import com.processrobotics.dioTender.style.Style;
import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeView;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DioTenderInstallationNodeView implements SwingInstallationNodeView<DioTenderInstallationNodeContribution> {

	private static final int ROW_HEIGHT = 30;
	private static final Dimension COMBO_SIZE = new Dimension(320, ROW_HEIGHT);

	private final Style style;
	private final Map<Signal, JComboBox<IoOption>> combos = new EnumMap<Signal, JComboBox<IoOption>>(Signal.class);
	private final Map<Signal, JCheckBox> inverts = new EnumMap<Signal, JCheckBox>(Signal.class);
	private final Map<Signal, JLabel> labels = new EnumMap<Signal, JLabel>(Signal.class);
	private final Map<Signal, JLabel> warnings = new EnumMap<Signal, JLabel>(Signal.class);
	private static final int POLL_MS = 250;
	private static final int DASHBOARD_EVERY_N_TICKS = 4;
	private static final Dimension INDICATOR_SIZE = new Dimension(40, 24);
	private static final Dimension BUTTON_SIZE = new Dimension(90, ROW_HEIGHT);
	private static final String LOCKED_TEXT = "Program running: outputs locked";

	private final Map<Signal, JLabel> indicators = new EnumMap<Signal, JLabel>(Signal.class);
	private final Map<Signal, JButton> onButtons = new EnumMap<Signal, JButton>(Signal.class);
	private final Map<Signal, JButton> offButtons = new EnumMap<Signal, JButton>(Signal.class);
	private final JLabel status = new JLabel(" ");
	private Timer pollTimer;
	private ExecutorService executor;
	// null = unknown (treated as running / locked). Only touched on the EDT.
	private Boolean programRunning = null;
	private boolean sending = false;
	private boolean dashboardCheckPending = false;
	private int tick = 0;
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

		addSection(jPanel, "Inputs (machine / door \u2192 robot)", true);
		jPanel.add(Style.createVerticalSpacing(Style.VERTICAL_SPACING));
		addSection(jPanel, "Outputs (robot \u2192 machine / vise / door)", false);
		status.setAlignmentX(Component.LEFT_ALIGNMENT);
		status.setForeground(Style.GRAY_1);
		jPanel.add(status);
		sizeLabelsToWidest();

		jPanel.add(Box.createVerticalGlue());
		Box logo = LogoRow.create(75);
		logo.setAlignmentX(Component.LEFT_ALIGNMENT);
		jPanel.add(logo);
	}

	/** Adds a bold category header followed by the rows of every signal with the given direction. */
	private void addSection(JPanel jPanel, String title, boolean inputs) {
		JLabel header = new JLabel(title);
		header.setFont(header.getFont().deriveFont(Font.BOLD));
		header.setAlignmentX(Component.LEFT_ALIGNMENT);
		jPanel.add(header);
		jPanel.add(Style.createVerticalSpacing());
		for (Signal signal : Signal.values()) {
			if (signal.isInput() == inputs) {
				jPanel.add(createSignalRow(signal));
				jPanel.add(Style.createVerticalSpacing());
			}
		}
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

		final JCheckBox invert = new JCheckBox("Invert");
		invert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!updating && contribution != null) {
					contribution.setInverted(signal, invert.isSelected());
				}
			}
		});
		inverts.put(signal, invert);
		row.add(invert);
		row.add(Style.createHorizontalSpacing());

		if (signal.isInput()) {
			JLabel indicator = new JLabel("\u2013", SwingConstants.CENTER);
			indicator.setOpaque(true);
			indicator.setBorder(BorderFactory.createLineBorder(Style.GRAY_4, Style.BORDER_THIN));
			indicator.setPreferredSize(INDICATOR_SIZE);
			indicator.setMinimumSize(INDICATOR_SIZE);
			indicator.setMaximumSize(INDICATOR_SIZE);
			indicators.put(signal, indicator);
			row.add(indicator);
		} else {
			boolean pulse = signal == Signal.MFIN;
			JButton on = createOutputButton(signal, pulse ? "ON" : "OPEN", true);
			JButton off = createOutputButton(signal, pulse ? "OFF" : "CLOSE", false);
			onButtons.put(signal, on);
			offButtons.put(signal, off);
			row.add(on);
			row.add(Style.createHorizontalSpacing());
			row.add(off);
		}
		row.add(Style.createHorizontalSpacing());

		JLabel warning = new JLabel("");
		warning.setForeground(Style.RED);
		warnings.put(signal, warning);
		row.add(warning);

		row.add(Box.createHorizontalGlue());
		return row;
	}

	private JButton createOutputButton(final Signal signal, String text, final boolean on) {
		JButton button = new JButton(text);
		button.setPreferredSize(BUTTON_SIZE);
		button.setMinimumSize(BUTTON_SIZE);
		button.setMaximumSize(BUTTON_SIZE);
		button.setOpaque(true);
		button.setEnabled(false);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onOutputClicked(signal, on);
			}
		});
		return button;
	}

	/** Starts the 250 ms poll that refreshes indicators / highlights / enabled state. Call on the EDT. */
	void startPolling() {
		if (executor == null) {
			executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r, "dio-tender-controller");
					t.setDaemon(true);
					return t;
				}
			});
		}
		if (pollTimer == null) {
			pollTimer = new Timer(POLL_MS, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onTick();
				}
			});
		}
		tick = 0;
		programRunning = null; // unknown until the first Dashboard answer
		pollTimer.start();
		onTick();
	}

	void stopPolling() {
		if (pollTimer != null) {
			pollTimer.stop();
		}
	}

	private void onTick() {
		if (contribution == null) {
			return;
		}
		if (tick++ % DASHBOARD_EVERY_N_TICKS == 0 && !dashboardCheckPending && executor != null) {
			dashboardCheckPending = true;
			executor.submit(new Runnable() {
				@Override
				public void run() {
					Boolean running;
					try {
						running = Boolean.valueOf(ControllerClient.isProgramRunning());
					} catch (IOException ex) {
						running = null; // fail closed
					}
					final Boolean result = running;
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							programRunning = result;
							dashboardCheckPending = false;
						}
					});
				}
			});
		}
		boolean unlocked = Boolean.FALSE.equals(programRunning);
		for (Signal signal : Signal.values()) {
			Boolean state = contribution.readState(signal);
			if (signal.isInput()) {
				showIndicator(indicators.get(signal), state);
			} else {
				JButton on = onButtons.get(signal);
				JButton off = offButtons.get(signal);
				highlight(on, state != null && state);
				highlight(off, state != null && !state);
				boolean enabled = state != null && unlocked && !sending;
				on.setEnabled(enabled);
				off.setEnabled(enabled);
			}
		}
		if (programRunning == null) {
			if (status.getForeground() != Style.RED) {
				status.setForeground(Style.GRAY_1);
				status.setText("Controller state unknown: outputs locked");
			}
		} else if (programRunning) {
			status.setForeground(Style.GRAY_1);
			status.setText(LOCKED_TEXT);
		} else if (status.getForeground() != Style.RED) {
			status.setText(" ");
		}
	}

	private static void showIndicator(JLabel indicator, Boolean state) {
		if (state == null) {
			indicator.setBackground(Style.LIGHT_GRAY_3);
			indicator.setForeground(Style.BLACK);
			indicator.setText("\u2013");
		} else if (state) {
			indicator.setBackground(Style.GREEN);
			indicator.setForeground(Style.WHITE);
			indicator.setText("ON");
		} else {
			indicator.setBackground(Style.LIGHT_GRAY_1);
			indicator.setForeground(Style.BLACK);
			indicator.setText("OFF");
		}
	}

	private static void highlight(JButton button, boolean active) {
		if (active) {
			button.setBackground(Style.UR_BLUE);
			button.setForeground(Style.WHITE);
		} else {
			button.setBackground(null);
			button.setForeground(null);
		}
	}

	private void onOutputClicked(final Signal signal, final boolean on) {
		if (sending || executor == null || contribution == null) {
			return;
		}
		sending = true;
		status.setForeground(Style.GRAY_1);
		status.setText(" ");
		for (Signal s : onButtons.keySet()) {
			onButtons.get(s).setEnabled(false);
			offButtons.get(s).setEnabled(false);
		}
		final String command = contribution.buildCommand(signal, on);
		executor.submit(new Runnable() {
			@Override
			public void run() {
				String error = null;
				try {
					// time-of-check guard: a program may have started since the last poll
					if (ControllerClient.isProgramRunning()) {
						error = LOCKED_TEXT;
					} else {
						ControllerClient.sendPrimary(command);
					}
				} catch (IOException ex) {
					error = "Could not reach controller: " + ex.getMessage();
				}
				final String message = error;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						sending = false;
						if (message != null) {
							status.setForeground(LOCKED_TEXT.equals(message) ? Style.GRAY_1 : Style.RED);
							status.setText(message);
						}
					}
				});
			}
		});
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
				inverts.get(signal).setSelected(installationNode.isInverted(signal));
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
