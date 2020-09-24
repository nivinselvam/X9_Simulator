 package com;

import java.awt.EventQueue;
import java.util.Map;

public class Main {

	static String fepName = "X9";
	static boolean isGUI = false;
	static Server server;
	static GUI window;

	public static void main(String[] args) {

		if (isGUI) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						window = new GUI();
						window.frmX9Simulator.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} else {
			server = new Server();
			server.start();
		}

	}

}
