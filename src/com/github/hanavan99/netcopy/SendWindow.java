package com.github.hanavan99.netcopy;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.github.hanavan99.netcopy.Window;

public class SendWindow extends Window {

	private JTextField pathText;
	private JButton browseButton;
	private JButton cancelButton;
	private JButton runButton;
	private JLabel statusLabel;
	private Socket client;

	public SendWindow(Window parent) {
		super("Netcopy - Send", 50, 50, 500, 500);

		// create socket
		try {
			ServerSocket ss = new ServerSocket();
			ss.bind(null);
			JOptionPane.showMessageDialog(frame, "Waiting for connection on port " + ss.getLocalPort() + ". Click OK to continue.");
			Thread t = new Thread(() -> {
				try {
					client = ss.accept();
					JOptionPane.showMessageDialog(frame, "Client connected.");
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(frame, "Failed to receive client connection: " + e.getMessage());
				}
			});
			t.start();

		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("failed to get client connection");
		}

		pathText = new JTextField(System.getProperty("user.dir"));
		pathText.setBounds(10, 10, 300, 20);
		frame.add(pathText);

		browseButton = new JButton("Browse");
		browseButton.setBounds(320, 10, 100, 20);
		browseButton.addActionListener((e) -> {
			JFileChooser chooser = new JFileChooser(new File(pathText.getText()));
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
				pathText.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		});
		frame.add(browseButton);

		runButton = new JButton("Transfer");
		runButton.setBounds(10, 40, 100, 20);
		runButton.addActionListener((_e) -> {
			try {
				DataOutputStream dos = new DataOutputStream(client.getOutputStream());
				File dir = new File(pathText.getText());
				transferDirectory(dir, dir, dos, statusLabel);
				JOptionPane.showMessageDialog(frame, "Done.");
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(frame, e.getMessage());
			}
		});
		frame.add(runButton);

		statusLabel = new JLabel("Done.");
		statusLabel.setBounds(10, 70, 300, 20);
		frame.add(statusLabel);

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener((_e) -> {
			frame.setVisible(false);
			parent.frame.setVisible(true);
		});
		frame.add(cancelButton);

		frame.repaint();
	}

}
