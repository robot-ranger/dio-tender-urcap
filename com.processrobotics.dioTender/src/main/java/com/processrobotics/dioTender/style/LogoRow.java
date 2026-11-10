package com.processrobotics.dioTender.style;

import com.processrobotics.dioTender.Constants;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.awt.Dimension;

public class LogoRow {
	public static Box create(int height) {
		Box logoBox = Box.createHorizontalBox();
		ImageIcon icon = new ImageIcon(LogoRow.class.getResource(Constants.PROCESS_ROBOTICS_LOGO));
		JLabel logo = new JLabel(icon);
		logo.setPreferredSize(new Dimension(icon.getIconWidth(), height));
		logoBox.add(logo);
		logoBox.add(Box.createHorizontalGlue());
		return logoBox;
	}
}
