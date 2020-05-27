package com.github.hanavan99.netcopy.ui;

import javax.swing.JButton;

public class ModeWindow extends Window {

	private JButton sendButton;
	private JButton receiveButton;

	public ModeWindow() {
		super("Netcopy - Select Mode", 50, 50, 340, 150);

		sendButton = new JButton("Send ->");
		sendButton.setBounds(10, 10, 150, 100);
		sendButton.addActionListener((_e) -> {
			frame.setVisible(false);
			new SendWindow(this);
		});
		frame.add(sendButton);

		receiveButton = new JButton("-> Receive");
		receiveButton.setBounds(170, 10, 150, 100);
		receiveButton.addActionListener((_e) -> {
			frame.setVisible(false);
			new ReceiveWindow(this);
		});
		frame.add(receiveButton);
	}
}
