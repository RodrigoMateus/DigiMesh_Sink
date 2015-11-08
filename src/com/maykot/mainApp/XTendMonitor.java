package com.maykot.mainApp;

import com.digi.xbee.api.exceptions.TimeoutException;

public class XTendMonitor extends Thread {

	public XTendMonitor() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		super.run();

		while (true) {
			try {
				Thread.sleep(5000);
				MainApp.myDevice.getPowerLevel().getValue();
				System.out.println("OK!");
			} catch (TimeoutException e1) {
			} catch (Exception e1) {
				System.out.println("Reset");
				MainApp.myDevice.close();
				MainApp.openDevice();
			}
		}
	}
}
