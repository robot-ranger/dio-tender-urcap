package com.processrobotics.dioTender.style;

//import constants
import com.processrobotics.dioTender.Constants;

import java.awt.*;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextPane;

public abstract class Style {
	// SPACING
	public static final int HORIZONTAL_SPACING = 10;
	public static final int VERTICAL_SPACING = 10;
	public static final int LARGE_VERTICAL_SPACING = 25;
	public static final int XLARGE_VERTICAL_SPACING = 50;

	// FONT SIZES
	public static final int FONT_TINY = 6;
	public static final int FONT_VERY_SMALL = 8;
	public static final int FONT_SMALL = 10;
	public static final int FONT_DEFAULT = 12;
	public static final int FONT_MEDIUM = 13;
	public static final int FONT_LARGE = 14;
	public static final int FONT_VERY_LARGE = 16;
	public static final int FONT_MAIN_HEADING = 24;
	public static final int FONT_HUGE = 36;

	// BORDER WIDTHS
	public static final int BORDER_THIN = 1;
	public static final int BORDER_NORMAL = 2;
	public static final int BORDER_WIDE = 5;

	// DIMENSIONS
	public static final int HEIGHT_TINY = 15;
	public static final int HEIGHT_VERY_SMALL = 25;
	public static final int HEIGHT_SMALL = 30;
	public static final int HEIGHT_DEFAULT = 32;
	public static final int HEIGHT_MEDIUM = 40;
	public static final int HEIGHT_LARGE = 50;
	public static final int HEIGHT_VERY_LARGE = 75;
	public static final int HEIGHT_BIG = 85;
	public static final int HEIGHT_HUGE = 160;
	public static final int WIDTH_INPUTS = 200;
	public static final Dimension INPUTFIELD_SIZE = new Dimension(WIDTH_INPUTS, HEIGHT_SMALL);

	// COLORS
	public static final Color BLACK = new Color(0, 0, 0);
	public static final Color DARK_GRAY = new Color(51, 51, 51);
	public static final Color GRAY_1 = new Color(100, 100, 100);
	public static final Color GRAY_2 = new Color(115, 115, 115);
	public static final Color GRAY_3 = new Color(136, 136, 136);
	public static final Color GRAY_4 = new Color(179, 179, 179);
	public static final Color GRAY_5 = new Color(192, 192, 192);
	public static final Color GRAY_6 = new Color(205, 205, 205);
	public static final Color LIGHT_GRAY_1 = new Color(210, 210, 210);
	public static final Color LIGHT_GRAY_2 = new Color(230, 230, 230);
	public static final Color LIGHT_GRAY_3 = new Color(240, 240, 240);
	public static final Color LIGHT_GRAY_4 = new Color(243, 243, 243);
	public static final Color WHITE = new Color(255, 255, 255);
	public static final Color NAVY_BLUE = new Color(47, 91, 183);
	public static final Color UR_BLUE = new Color(86, 160, 211);
	public static final Color LIGHT_BLUE = new Color(194, 225, 246);
	public static final Color DARK_YELLOW = new Color(204, 168, 99);
	public static final Color GOLD_YELLOW = new Color(230, 206, 76);
	public static final Color GOLD_YELLOW_2 = new Color(235, 202, 132);
	public static final Color YELLOW = new Color(255, 255, 112);
	public static final Color LIGHT_YELLOW = new Color(251, 238, 198);
	public static final Color RED = new Color(255, 0, 0);
	public static final Color GRAY_RED = new Color(236, 165, 165);
	public static final Color LIGHT_RED = new Color(237, 206, 206);
	public static final Color GREEN = new Color(0, 146, 69);
	public static final Color BRIGHT_GREEN = new Color(134, 222, 136);
	public static final Color LIGHT_GREEN = new Color(194, 246, 214);

	public Component createHorizontalSpacing(int width) {
		return Box.createRigidArea(new Dimension(width, 0));
	}

	public static Component createHorizontalSpacing() {
		return Box.createRigidArea(new Dimension(HORIZONTAL_SPACING, 0));
	}

	public static Component createVerticalSpacing(int height) {
		return Box.createRigidArea(new Dimension(0, height));
	}

	public static Component createVerticalSpacing() {
		return Box.createRigidArea(new Dimension(0, VERTICAL_SPACING));
	}

	public static JComboBox<String> buildComboBox(String[] items) {
		JComboBox<String> comboBox = new JComboBox<String>(items);
		Font font = new Font("DejaVu Sans", Font.PLAIN, FONT_VERY_LARGE);
		comboBox.setFont(font);
		Dimension dim = new Dimension(WIDTH_INPUTS, HEIGHT_SMALL);
		comboBox.setPreferredSize(dim);
		comboBox.setMaximumSize(dim);
		comboBox.setMinimumSize(dim);
		return comboBox;
	}

	public static JLabel buildLabel(String text) {
		JLabel label = new JLabel(text);
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		Font font = new Font("DejaVu Sans", Font.PLAIN, FONT_VERY_LARGE);
		label.setFont(font);
		return label;
	}

	public static JTextField buildInputField() {
		JTextField textField = new JTextField();
		Font font = new Font("DejaVu Sans", Font.PLAIN, FONT_VERY_LARGE);
		textField.setFont(font);
		Dimension dim = new Dimension(WIDTH_INPUTS, HEIGHT_SMALL);
		textField.setPreferredSize(dim);
		textField.setMaximumSize(dim);
		textField.setMinimumSize(dim);
		return textField;
	}

	public JTextPane buildTextPane() {
		JTextPane textPane = new JTextPane();
		Font font = new Font("DejaVu Sans", Font.PLAIN, FONT_VERY_LARGE);
		textPane.setFont(font);
		return textPane;
	}

	/**
	 * Builds an input field with a label.
	 * @param label the label to display for the input field
	 * @return a Box containing the label and input field
	 */
	public Box buildInputFieldWithLabel(String label) {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.CENTER_ALIGNMENT);
		box.add(buildLabel(label));
		box.add(createHorizontalSpacing(HORIZONTAL_SPACING));
		box.add(buildInputField());
		return box;
	}

	/**
	 * Builds an input field with a label and units.
	 * @param label the label to display for the input field
	 * @param units the units to display after the input field
	 * @return a Box containing the label, input field, and units
	 */
	public Box buildInputFieldWithLabel(String label, String units) {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.CENTER_ALIGNMENT);
		box.add(buildLabel(label));
		box.add(createHorizontalSpacing(HORIZONTAL_SPACING));
		box.add(buildInputField());
		box.add(createHorizontalSpacing(HORIZONTAL_SPACING));
		box.add(buildLabel(units));
		return box;
	}

	public Dimension getInputfieldSize() {
		return INPUTFIELD_SIZE;
	}
}
