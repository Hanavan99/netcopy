package com.github.hanavan99.netcopy.ui;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

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
import com.github.hanavan99.netcopy.net.NetworkTransfer;
import com.github.hanavan99.netcopy.net.NetworkTransferServer;

public class SendWindow extends Window {

	private JTextField pathText;
	private JButton browseButton;
	private JCheckBox allowClientChanges;
	private JCheckBox createMissingFolders;
	private JCheckBox purgeDirectory;
	private JCheckBox transferNonexistingFiles;
	private JCheckBox transferExistingFiles;
	private JComboBox<FilePreferenceMode> filePreferenceMode;
	private JTextArea statusText;
	private JProgressBar progressBar;
	private JButton runButton;
	private NetworkTransferServer server;

	public SendWindow(Window parent) {
		super("Netcopy - Send", 50, 50, 725, 750);

		// create socket
		try {
			ServerSocket ss = new ServerSocket();
			ss.bind(null);
			createLabel("Server address: " + InetAddress.getLocalHost().getHostAddress() + ":" + ss.getLocalPort(), 10, 10);
			Thread t = new Thread(() -> {
				try {
					server = new NetworkTransferServer(ss.accept(), new File(pathText.getText()), NetworkTransfer.DEFAULT_BUFFER_SIZE, new NC());
					JOptionPane.showMessageDialog(frame, "Client connected.");
					ss.close();
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(frame, "Failed to receive client connection: " + e.getMessage());
				}
			});
			t.start();
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Failed to bind server: " + e.getMessage());
		}

		createLabel("Transfer path:", 10, 40);

		pathText = new JTextField(System.getProperty("user.dir"));
		pathText.setBounds(100, 40, 500, 20);
		frame.add(pathText);

		browseButton = new JButton("Browse");
		browseButton.setBounds(610, 40, 100, 20);
		browseButton.addActionListener((e) -> {
			JFileChooser chooser = new JFileChooser(new File(pathText.getText()));
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				if (server != null) {
					server.setDirectory(file);
				}
				pathText.setText(file.getAbsolutePath());
			}
		});
		frame.add(browseButton);

		JPanel panel = new JPanel();
		panel.setBounds(10, 70, 700, 200);
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder("Settings"));

		allowClientChanges = new JCheckBox("Allow client to modify settings", true);
		allowClientChanges.setBounds(10, 20, 600, 20);
		allowClientChanges.addActionListener((_e) -> {
			if (server != null) {
				server.sendSettingsChanged(allowClientChanges.isSelected(), createMissingFolders.isSelected(), purgeDirectory.isSelected(), transferNonexistingFiles.isSelected(), transferExistingFiles.isSelected(),
						filePreferenceMode.getSelectedIndex());
			}
		});
		panel.add(allowClientChanges);

		createMissingFolders = new JCheckBox("Create missing directories for transferred files", true);
		createMissingFolders.setBounds(10, 50, 600, 20);
		createMissingFolders.addActionListener((_e) -> {
			if (server != null) {
				server.sendSettingsChanged(allowClientChanges.isSelected(), createMissingFolders.isSelected(), purgeDirectory.isSelected(), transferNonexistingFiles.isSelected(), transferExistingFiles.isSelected(),
						filePreferenceMode.getSelectedIndex());
			}
		});
		panel.add(createMissingFolders);

		purgeDirectory = new JCheckBox("Delete parent directory on the client before transfer (cannot be undone!)", false);
		purgeDirectory.setBounds(10, 80, 600, 20);
		purgeDirectory.addActionListener((_e) -> {
			if (server != null) {
				server.sendSettingsChanged(allowClientChanges.isSelected(), createMissingFolders.isSelected(), purgeDirectory.isSelected(), transferNonexistingFiles.isSelected(), transferExistingFiles.isSelected(),
						filePreferenceMode.getSelectedIndex());
			}
		});
		panel.add(purgeDirectory);

		transferNonexistingFiles = new JCheckBox("Transfer files that don't exist on the client", true);
		transferNonexistingFiles.setBounds(10, 110, 600, 20);
		transferNonexistingFiles.addActionListener((_e) -> {
			if (server != null) {
				server.sendSettingsChanged(allowClientChanges.isSelected(), createMissingFolders.isSelected(), purgeDirectory.isSelected(), transferNonexistingFiles.isSelected(), transferExistingFiles.isSelected(),
						filePreferenceMode.getSelectedIndex());
			}
		});
		panel.add(transferNonexistingFiles);

		transferExistingFiles = new JCheckBox("Transfer files that exist on the client", true);
		transferExistingFiles.setBounds(10, 140, 250, 20);
		transferExistingFiles.addActionListener((_e) -> {
			if (server != null) {
				server.sendSettingsChanged(allowClientChanges.isSelected(), createMissingFolders.isSelected(), purgeDirectory.isSelected(), transferNonexistingFiles.isSelected(), transferExistingFiles.isSelected(),
						filePreferenceMode.getSelectedIndex());
			}
		});
		panel.add(transferExistingFiles);

		JLabel label1 = new JLabel("Transfer mode:");
		label1.setBounds(10, 170, 100, 20);
		panel.add(label1);

		filePreferenceMode = new JComboBox<FilePreferenceMode>(FilePreferenceMode.values());
		filePreferenceMode.setBounds(120, 170, 400, 20);
		filePreferenceMode.addActionListener((_e) -> {
			if (server != null) {
				server.sendSettingsChanged(allowClientChanges.isSelected(), createMissingFolders.isSelected(), purgeDirectory.isSelected(), transferNonexistingFiles.isSelected(), transferExistingFiles.isSelected(),
						filePreferenceMode.getSelectedIndex());
			}
		});
		panel.add(filePreferenceMode);

		frame.add(panel);

		statusText = new JTextArea();
		statusText.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(statusText);
		scrollPane.setBounds(10, 280, 700, 400);
		frame.add(scrollPane);

		progressBar = new JProgressBar();
		progressBar.setBounds(10, 690, 590, 20);
		frame.add(progressBar);

		runButton = new JButton("Transfer");
		runButton.setBounds(610, 690, 100, 20);
		runButton.addActionListener((_e) -> {
			if (server != null) {
				Thread t = new Thread(() -> {
					pathText.setEnabled(false);
					browseButton.setEnabled(false);
					runButton.setEnabled(false);
					server.setDirectory(new File(pathText.getText()));
					try {
						server.transfer();
						JOptionPane.showMessageDialog(frame, "Transfer finished!");
					} catch (IOException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(frame, "Failed to complete file transfer: " + e.getMessage());
						server = null;
					}
					pathText.setEnabled(true);
					browseButton.setEnabled(true);
					runButton.setEnabled(true);
				});
				t.start();
			} else {
				JOptionPane.showMessageDialog(frame, "No client has connected.");
			}
		});
		frame.add(runButton);

		frame.repaint();
	}

	private class NC implements INetworkCallback {

		@Override
		public void transferStarted() {
			// do nothing here
		}

		@Override
		public void transferEnded() {
			// do nothing here
		}

		@Override
		public void messageLogged(String message) {
			statusText.append(message);
		}

		@Override
		public void progressUpdated(int cur, int max) {
			progressBar.setMaximum(max);
			progressBar.setValue(cur);
		}

		@Override
		public void settingsChanged(boolean allowClientChanges, boolean createMissingFolders, boolean purgeDirectory, boolean transferNonexistingFiles, boolean transferExistingFiles, int filePreferenceMode) {
			if (SendWindow.this.allowClientChanges.isSelected()) {
				SendWindow.this.createMissingFolders.setSelected(createMissingFolders);
				SendWindow.this.purgeDirectory.setSelected(purgeDirectory);
				SendWindow.this.transferNonexistingFiles.setSelected(transferNonexistingFiles);
				SendWindow.this.transferExistingFiles.setSelected(transferExistingFiles);
				SendWindow.this.filePreferenceMode.setSelectedItem(FilePreferenceMode.values()[filePreferenceMode]);
			}
		}

	}

}
