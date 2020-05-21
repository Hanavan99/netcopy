package com.github.hanavan99.netcopy.ui;

import javax.swing.JFrame;
import javax.swing.JLabel;

public abstract class Window {

	protected final JFrame frame;

	public Window(String name, int x, int y, int width, int height) {
		frame = new JFrame(name);
		frame.setBounds(x, y, width, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(null);
		frame.setResizable(false);
		frame.setVisible(true);
	}

	protected void createLabel(String text, int x, int y) {
		JLabel label = new JLabel(text);
		label.setBounds(x, y, label.getFontMetrics(label.getFont()).stringWidth(text) + 10, 20);
		frame.add(label);
	}

}
