package com.maykot.mainApp;

import java.util.Arrays;

import com.digi.xbee.api.DigiMeshDevice;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.utils.Statistic;

public class SendResponseMessage {

	public static void send(DigiMeshDevice myDevice, byte[] responseToSourceDevice, int ENDPOINT,
			XBee64BitAddress sourceDevice) {

		try {
			if (!myDevice.isOpen()) {
				myDevice.open();
				System.out.println("Device is open now!");
			}

			switch (ENDPOINT) {

			case MainApp.ENDPOINT_RESPONSE_INIT:

				myDevice.sendExplicitData(sourceDevice, ENDPOINT, ENDPOINT, MainApp.CLUSTER_ID, MainApp.PROFILE_ID,
						responseToSourceDevice);
				break;

			case MainApp.ENDPOINT_RESPONSE_DATA:
				int dataSize = responseToSourceDevice.length;
				int first = 0;
				int last = MainApp.PAYLOAD;

				do {
					try {
						byte[] partOfData = Arrays.copyOfRange(responseToSourceDevice, first, last);
						myDevice.sendExplicitData(sourceDevice, ENDPOINT, ENDPOINT, MainApp.CLUSTER_ID,
								MainApp.PROFILE_ID, partOfData);
						first = last;
						last = last + MainApp.PAYLOAD;
						if (last > dataSize)
							last = dataSize;
						Statistic.incrementCountOK();
					} catch (TimeoutException e) {
						System.out.println("TimeOut ERROR");
					}
				} while (first < dataSize);
				break;

			case MainApp.ENDPOINT_RESPONSE_SEND:
				myDevice.sendExplicitData(sourceDevice, ENDPOINT, ENDPOINT, MainApp.CLUSTER_ID, MainApp.PROFILE_ID,
						responseToSourceDevice);
				break;
			}
		} catch (TimeoutException e) {
			System.out.println("TimeOut ERROR");
		} catch (XBeeException e) {
			Statistic.incrementCountBadPack();
			e.printStackTrace();
		} finally {
		}
	}
}