package com.github.hanavan99.netcopy.ui;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.github.hanavan99.netcopy.net.FilePreferenceMode;
import com.github.hanavan99.netcopy.net.INetworkCallback;
import com.github.hanavan99.netcopy.net.NetworkTransferClient;

public class ReceiveWindow extends TransferWindow {

	private JButton connectButton;
	private NetworkTransferClient client;

	public ReceiveWindow(Window parent) {
		super("Netcopy - Receive", 50, 50, 725, 720);

		createLabel("Transfer path:", 10, 10);

		pathText = new JTextField(System.getProperty("user.dir"));
		pathText.setBounds(100, 10, 500, 20);
		frame.add(pathText);

		browseButton = new JButton("Browse...");
		browseButton.setBounds(610, 10, 100, 20);
		browseButton.addActionListener((e) -> {
			JFileChooser chooser = new JFileChooser(new File(pathText.getText()));
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				if (client != null) {
					client.setDirectory(file);
				}
				pathText.setText(file.getAbsolutePath());
			}
		});
		frame.add(browseButton);

		JPanel panel = new JPanel();
		panel.setBounds(10, 40, 700, 170);
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder("Settings"));

		createMissingFolders = new JCheckBox("Create missing directories for transferred files", true);
		createMissingFolders.setBounds(10, 20, 600, 20);
		createMissingFolders.addActionListener((_e) -> {
			if (client != null) {
				try {
					client.writeSettingsPacket(false, createMissingFolders.isSelected(), purgeDirectory.isSelected(), transferNonexistingFiles.isSelected(), transferExistingFiles.isSelected(), filePreferenceMode.getSelectedIndex());
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(frame, "Failed to send settings to server: " + e.getMessage());
				}
			}
		});
		panel.add(createMissingFolders);

		purgeDirectory = new JCheckBox("Delete parent directory on the client before transfer (cannot be undone!)", false);
		purgeDirectory.setBounds(10, 50, 600, 20);
		purgeDirectory.addActionListener((_e) -> {
			try {
				client.writeSettingsPacket(false, createMissingFolders.isSelected(), purgeDirectory.isSelected(), transferNonexistingFiles.isSelected(), transferExistingFiles.isSelected(), filePreferenceMode.getSelectedIndex());
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Failed to send settings to server: " + e.getMessage());
			}
		});
		panel.add(purgeDirectory);

		transferNonexistingFiles = new JCheckBox("Transfer files that don't exist on the client", true);
		transferNonexistingFiles.setBounds(10, 80, 600, 20);
		transferNonexistingFiles.addActionListener((_e) -> {
			try {
				client.writeSettingsPacket(false, createMissingFolders.isSelected(), purgeDirectory.isSelected(), transferNonexistingFiles.isSelected(), transferExistingFiles.isSelected(), filePreferenceMode.getSelectedIndex());
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Failed to send settings to server: " + e.getMessage());
			}
		});
		panel.add(transferNonexistingFiles);

		transferExistingFiles = new JCheckBox("Transfer files that exist on the client", true);
		transferExistingFiles.setBounds(10, 110, 250, 20);
		transferExistingFiles.addActionListener((_e) -> {
			try {
				client.writeSettingsPacket(false, createMissingFolders.isSelected(), purgeDirectory.isSelected(), transferNonexistingFiles.isSelected(), transferExistingFiles.isSelected(), filePreferenceMode.getSelectedIndex());
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Failed to send settings to server: " + e.getMessage());
			}
		});
		panel.add(transferExistingFiles);

		JLabel label1 = new JLabel("Transfer mode:");
		label1.setBounds(10, 140, 100, 20);
		panel.add(label1);

		filePreferenceMode = new JComboBox<FilePreferenceMode>(FilePreferenceMode.values());
		filePreferenceMode.setBounds(120, 140, 400, 20);
		filePreferenceMode.addActionListener((_e) -> {
			try {
				client.writeSettingsPacket(false, createMissingFolders.isSelected(), purgeDirectory.isSelected(), transferNonexistingFiles.isSelected(), transferExistingFiles.isSelected(), filePreferenceMode.getSelectedIndex());
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Failed to send settings to server: " + e.getMessage());
			}
		});
		panel.add(filePreferenceMode);

		frame.add(panel);

		statusText = new JTextArea();
		statusText.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(statusText);
		scrollPane.setBounds(10, 220, 700, 400);
		frame.add(scrollPane);

		progressLabel = new JLabel("Total progress: (0/0)");
		progressLabel.setBounds(10, 630, 180, 20);
		frame.add(progressLabel);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(200, 630, 400, 20);
		progressBar.setStringPainted(true);
		frame.add(progressBar);
		
		fileProgressLabel = new JLabel("File progress: (0B/0B)");
		fileProgressLabel.setBounds(10, 660, 180, 20);
		frame.add(fileProgressLabel);

		fileProgressBar = new JProgressBar();
		fileProgressBar.setBounds(200, 660, 400, 20);
		fileProgressBar.setStringPainted(true);
		frame.add(fileProgressBar);

		connectButton = new JButton("Connect");
		connectButton.setBounds(610, 630, 100, 20);
		connectButton.addActionListener((_e) -> {
			// get address and port number
			String address = JOptionPane.showInputDialog(frame, "Please enter the address of the server to connect to.");
			String[] addressParts = address.split(":", 2);

			// try to connect to server
			try {
				Socket socket = new Socket();
				socket.connect(new InetSocketAddress(addressParts[0], Integer.parseInt(addressParts[1])));
				client = new NetworkTransferClient(socket, new File(pathText.getText()), NetworkTransferClient.DEFAULT_BUFFER_SIZE, new NC());
				connectButton.setEnabled(false);
				JOptionPane.showMessageDialog(frame, "Connected to server.");
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Failed to connect to server: " + e.getMessage());
			} catch (NumberFormatException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Invalid port number \"" + addressParts[1] + "\".");
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Invalid address.");
			}
		});
		frame.add(connectButton);

		frame.repaint();
	}
	
	@Override
	public void setUIState(boolean enabled) {
		super.setUIState(enabled);
		pathText.setEnabled(enabled);
		browseButton.setEnabled(enabled);
	}

	private final class NC implements INetworkCallback {

		@Override
		public void transferStarted() {
			statusText.setText("");
			setUIState(false);
			client.setDirectory(new File(pathText.getText()));
		}

		@Override
		public void transferEnded() {
			setUIState(true);
			JOptionPane.showMessageDialog(frame, "Transfer finished!");
		}

		@Override
		public void messageLogged(String message) {
			statusText.append(message);
		}

		@Override
		public void progressUpdated(int cur, int max) {
			updateProgress(cur, max);
		}

		@Override
		public void settingsChanged(boolean allowClientChanges, boolean createMissingFolders, boolean purgeDirectory, boolean transferNonexistingFiles, boolean transferExistingFiles, int filePreferenceMode) {
			updateSettings(allowClientChanges, createMissingFolders, purgeDirectory, transferNonexistingFiles, transferExistingFiles, filePreferenceMode);
		}

		@Override
		public void fileProgressUpdated(long cur, long max) {
			updateFileProgress(cur, max);
		}

	}
}
