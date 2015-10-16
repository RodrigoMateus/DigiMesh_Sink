package com.maykot.mainApp;

/*****************
 * DigiMesh Sink *
 *****************/

import java.io.IOException;

import com.digi.xbee.api.DigiMeshDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.APIOutputMode;
import com.digi.xbee.api.utils.DeviceConfig;
import com.digi.xbee.api.utils.Statistic;

public class MainApp {

	/* XTends */
	static DigiMeshDevice myDevice;
	static String XTEND_PORT = null;
	static int XTEND_BAUD_RATE;

	/* Endpoints, clusterID and profileID */
	static final int ENDPOINT_TXT = 11;
	static final int ENDPOINT_HTTP_POST_INIT = 31;
	static final int ENDPOINT_HTTP_POST_DATA = 32;
	static final int ENDPOINT_HTTP_POST_SEND = 33;
	static final int ENDPOINT_HTTP_RESPONSE = 41;
	static final int CLUSTER_ID = 1;
	static final int PROFILE_ID = 1;

	public static void main(String[] args) {
		System.out.println(" +-----------------+");
		System.out.println(" |  DigiMesh Sink  |");
		System.out.println(" +-----------------+\n");

		try {
			DeviceConfig deviceConfig = new DeviceConfig();

			XTEND_PORT = deviceConfig.getXTendPort();
			XTEND_BAUD_RATE = deviceConfig.getXTendBaudRate();

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
		}

		new Statistic();

		openDevice();
	}

	private static void openDevice() {
		try {
			myDevice = new DigiMeshDevice(XTEND_PORT, XTEND_BAUD_RATE);
			myDevice.open();
			myDevice.setAPIOutputMode(APIOutputMode.MODE_EXPLICIT);
			myDevice.addExplicitDataListener(new ExplicitDataReceiveListener());

			System.out.println("\n>> Waiting for data in explicit format...");

		} catch (XBeeException e) {
			myDevice.close();
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
