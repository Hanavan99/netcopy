package com.github.hanavan99.netcopy;

import javax.swing.JFrame;

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

}
