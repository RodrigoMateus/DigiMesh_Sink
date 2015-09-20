package com.maykot.mainApp;

import java.io.IOException;

import com.digi.xbee.api.DigiMeshDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.APIOutputMode;
import com.digi.xbee.api.utils.DeviceConfig;
import com.digi.xbee.api.utils.LogRecord;
import com.digi.xbee.api.utils.Statistic;

public class MainApp {

	/* XTends */
	static DigiMeshDevice myDevice;
	static String XTEND_PORT = null;
	static int XTEND_BAUD_RATE;
	static String REMOTE_NODE_IDENTIFIER = null;

	/* Endpoints, clusterID and profileID */
	static final int ENDPOINT_TXT = 11;
	static final int ENDPOINT_FILENEW = 21;
	static final int ENDPOINT_FILEDATA = 22;
	static final int ENDPOINT_FILECLOSE = 23;
	static final int ENDPOINT_HTTP_POST_INIT = 31;
	static final int ENDPOINT_HTTP_POST_DATA = 32;
	static final int ENDPOINT_HTTP_POST_SEND = 33;
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
			REMOTE_NODE_IDENTIFIER = deviceConfig.getRemoteNodeID();

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		new LogRecord();
		new Statistic();

		myDevice = new DigiMeshDevice(XTEND_PORT, XTEND_BAUD_RATE);

		try {
			myDevice.open();
			myDevice.setAPIOutputMode(APIOutputMode.MODE_EXPLICIT);
			myDevice.addExplicitDataListener(new ExplicitDataReceiveListener());

			System.out.println("\n>> Waiting for data in explicit format...");

		} catch (XBeeException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
