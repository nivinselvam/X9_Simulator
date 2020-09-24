package com;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.JButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.NumberFormatter;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JFormattedTextField;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class GUI {

	public JFrame frmX9Simulator;
	private JTextField txtIPaddress;
	JTextField txtApprovalAmount;
	private JTextArea txtLogs;
	private JButton btnStart;
	private JButton btnStop;
	private JLabel lblPort;
	private JTextField txtPort;
	private JLabel lblServerStatus;
	public JComboBox cbxTransactionResult;
	public JTextField txtResponseCode;

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmX9Simulator = new JFrame();
		frmX9Simulator.setFont(new Font("Dialog", Font.PLAIN, 21));
		frmX9Simulator.setTitle("X9 Simulator");
		frmX9Simulator.setBounds(100, 100, 501, 733);
		frmX9Simulator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		frmX9Simulator.setJMenuBar(menuBar);

		JMenu mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				System.exit(JFrame.EXIT_ON_CLOSE);
			}
		});
		mnNewMenu.add(mntmExit);

		JTabbedPane tpConfigurationPane = new JTabbedPane(JTabbedPane.TOP);

		JButton btnClearLogs = new JButton("Clear logs");
		btnClearLogs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				try {
					txtLogs.getDocument().remove(0, txtLogs.getDocument().getLength());
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

		JLabel lblRunTimeLogs = new JLabel("Run time logs");

		JScrollPane scrollPane = new JScrollPane();

		txtLogs = new JTextArea();
		scrollPane.setViewportView(txtLogs);
			PrintStream printStream = new PrintStream(new CustomOutputStream(txtLogs));
			System.setOut(printStream);
			System.setErr(printStream);
		JPanel pnServerConfiguration = new JPanel();
		tpConfigurationPane.addTab("Server Configuration", null, pnServerConfiguration, null);

		JLabel lblIPaddress = new JLabel("IP Address");

		lblPort = new JLabel("Port");

		btnStop = new JButton("Stop");
		btnStop.setEnabled(false);
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try {
					Main.server.ss.close();
					btnStop.setEnabled(false);
					btnStart.setEnabled(true);
					lblServerStatus.setText("Offline");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					System.out.println(e1.toString());
				}
			}
		});

		btnStart = new JButton("Start");

		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (validatePortNumber(txtPort.getText())) {
					Main.server = new Server();
					Main.server.start();
					btnStart.setEnabled(false);
					btnStop.setEnabled(true);
					lblServerStatus.setText("Online");
				}
			
			}
		});

		txtIPaddress = new JTextField();
		txtIPaddress.setEnabled(false);
		txtIPaddress.setColumns(10);
		try {
			InetAddress systemDetails = InetAddress.getLocalHost();
			txtIPaddress.setText(systemDetails.getHostAddress());
		} catch (UnknownHostException e) {

			e.printStackTrace();
		}

		JLabel lblStatus = new JLabel("Status :");

		 lblServerStatus = new JLabel("Offline");

		NumberFormat format = NumberFormat.getInstance();
		NumberFormatter portFormat = new NumberFormatter(format);
		portFormat.setValueClass(Integer.class);
		portFormat.setMinimum(1025);
		portFormat.setMaximum(65535);
		portFormat.setAllowsInvalid(false);
		portFormat.setCommitsOnValidEdit(true);

		txtPort = new JTextField();
		txtPort.setColumns(10);
		GroupLayout gl_pnServerConfiguration = new GroupLayout(pnServerConfiguration);
		gl_pnServerConfiguration.setHorizontalGroup(gl_pnServerConfiguration.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnServerConfiguration.createSequentialGroup().addGap(79)
						.addComponent(btnStart, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE).addGap(18)
						.addComponent(btnStop, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_pnServerConfiguration.createSequentialGroup().addGap(12)
						.addGroup(gl_pnServerConfiguration.createParallelGroup(Alignment.LEADING)
								.addComponent(lblIPaddress).addComponent(lblPort))
						.addGap(6)
						.addGroup(gl_pnServerConfiguration.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_pnServerConfiguration.createSequentialGroup()
										.addComponent(txtPort, GroupLayout.PREFERRED_SIZE, 190,
												GroupLayout.PREFERRED_SIZE)
										.addGap(73).addComponent(lblStatus).addGap(18).addComponent(lblServerStatus))
								.addComponent(txtIPaddress, GroupLayout.PREFERRED_SIZE, 363,
										GroupLayout.PREFERRED_SIZE))));
		gl_pnServerConfiguration
				.setVerticalGroup(
						gl_pnServerConfiguration.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_pnServerConfiguration.createSequentialGroup().addGap(23)
										.addGroup(gl_pnServerConfiguration.createParallelGroup(Alignment.LEADING)
												.addGroup(gl_pnServerConfiguration
														.createSequentialGroup().addGap(3).addComponent(lblIPaddress))
												.addComponent(
														txtIPaddress, GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
										.addGap(18)
										.addGroup(gl_pnServerConfiguration.createParallelGroup(Alignment.LEADING)
												.addGroup(gl_pnServerConfiguration.createSequentialGroup().addGap(3)
														.addGroup(gl_pnServerConfiguration
																.createParallelGroup(Alignment.BASELINE)
																.addComponent(lblPort).addComponent(txtPort,
																		GroupLayout.PREFERRED_SIZE,
																		GroupLayout.DEFAULT_SIZE,
																		GroupLayout.PREFERRED_SIZE)))
												.addGroup(gl_pnServerConfiguration.createSequentialGroup().addGap(3)
														.addComponent(lblStatus))
												.addGroup(gl_pnServerConfiguration.createSequentialGroup().addGap(3)
														.addComponent(lblServerStatus)))
										.addGap(29)
										.addGroup(gl_pnServerConfiguration.createParallelGroup(Alignment.LEADING)
												.addComponent(btnStart).addComponent(btnStop))));
		pnServerConfiguration.setLayout(gl_pnServerConfiguration);

		JPanel pnResponseConfiguration = new JPanel();
		tpConfigurationPane.addTab("Response Configuration", null, pnResponseConfiguration, null);

		JLabel lblTransactionResult = new JLabel("Transaction Result");

		JLabel lblResponseCode = new JLabel("Response Code");

		JLabel lblApprovalAmount = new JLabel("Approval Amount");

		String[] transactionResults = { "Approve", "Decline" };

		 cbxTransactionResult = new JComboBox(transactionResults);
		 

		txtApprovalAmount = new JTextField();
		txtApprovalAmount.setColumns(10);

		JCheckBox chckbxApproveForHalf = new JCheckBox("Approve for half amount");
		
		txtResponseCode = new JTextField();
		txtResponseCode.setColumns(10);
		GroupLayout gl_pnResponseConfiguration = new GroupLayout(pnResponseConfiguration);
		gl_pnResponseConfiguration.setHorizontalGroup(
			gl_pnResponseConfiguration.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnResponseConfiguration.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_pnResponseConfiguration.createParallelGroup(Alignment.LEADING)
						.addComponent(lblTransactionResult)
						.addComponent(lblResponseCode)
						.addComponent(lblApprovalAmount))
					.addGap(18)
					.addGroup(gl_pnResponseConfiguration.createParallelGroup(Alignment.LEADING, false)
						.addComponent(chckbxApproveForHalf)
						.addComponent(cbxTransactionResult, 0, 306, Short.MAX_VALUE)
						.addComponent(txtApprovalAmount, GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
						.addComponent(txtResponseCode))
					.addContainerGap())
		);
		gl_pnResponseConfiguration.setVerticalGroup(
			gl_pnResponseConfiguration.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnResponseConfiguration.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_pnResponseConfiguration.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblTransactionResult)
						.addComponent(cbxTransactionResult, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(18)
					.addGroup(gl_pnResponseConfiguration.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblResponseCode)
						.addComponent(txtResponseCode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(18)
					.addGroup(gl_pnResponseConfiguration.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblApprovalAmount)
						.addComponent(txtApprovalAmount, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(18)
					.addComponent(chckbxApproveForHalf)
					.addContainerGap(12, Short.MAX_VALUE))
		);
		pnResponseConfiguration.setLayout(gl_pnResponseConfiguration);
		GroupLayout groupLayout = new GroupLayout(frmX9Simulator.getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup().addGap(12).addComponent(tpConfigurationPane,
						GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(groupLayout.createSequentialGroup().addGap(12).addComponent(lblRunTimeLogs).addGap(292)
						.addComponent(btnClearLogs))
				.addGroup(groupLayout.createSequentialGroup().addGap(14)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE).addGap(12)));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup().addGap(13)
						.addComponent(tpConfigurationPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(13)
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblRunTimeLogs))
								.addComponent(btnClearLogs))
						.addGap(11).addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 385, Short.MAX_VALUE)
						.addGap(13)));
		frmX9Simulator.getContentPane().setLayout(groupLayout);
	}

	public boolean validatePortNumber(String portNumber) {

		try {

			if (Integer.parseInt(portNumber) < 1025 || Integer.parseInt(portNumber) > 65535) {
				JOptionPane.showMessageDialog(txtPort, "Port number should be between 1025 and 65535");
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {

			JOptionPane.showMessageDialog(txtPort, "Please enter only number for Port");
			return false;

		}
	}
}
