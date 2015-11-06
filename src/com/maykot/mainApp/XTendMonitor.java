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
				MainApp.myDevice.getPowerLevel().getValue();
			} catch (TimeoutException e1) {
			} catch (Exception e1) {
				System.out.println("Reset");
				MainApp.openDevice();
			}

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
