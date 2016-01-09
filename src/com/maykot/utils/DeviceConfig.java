package com.maykot.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DeviceConfig {

	private static DeviceConfig uniqueInstance;
	private static Properties properties;

	private DeviceConfig() throws IOException {
		FileInputStream file = new FileInputStream("DeviceConfig.properties");
		properties = new Properties();
		properties.load(file);
	}

	public static DeviceConfig getInstance() throws IOException {
		if (uniqueInstance == null) {
			uniqueInstance = new DeviceConfig();
		}
		return uniqueInstance;
	}

	public String getXTendPort() throws IOException {
		String port = properties.getProperty("XTendPort");
		return port;
	}

	public int getXTendBaudRate() throws IOException {
		int baudRate = Integer.parseInt(properties.getProperty("XTendBaudRate"));
		return baudRate;
	}

	public int getTimeOutForSyncOperations() throws IOException {
		int timeout = Integer.parseInt(properties.getProperty("TimeOutForSyncOperations"));
		return timeout;
	}

	public String getXBeePort() throws IOException {
		String port = properties.getProperty("XBeePort");
		return port;
	}

	public int getXBeeBaudRate() throws IOException {
		int baudRate = Integer.parseInt(properties.getProperty("XBeeBaudRate"));
		return baudRate;
	}

	public String getBrokerURL() throws IOException {
		String routerIP = properties.getProperty("BrokerURL");
		return routerIP;
	}

	public String getRemoteNodeID() throws IOException {
		String remoteNodeID = properties.getProperty("RemoteNodeID");
		return remoteNodeID;
	}

	public String getClientId() throws IOException {
		String routerIP = properties.getProperty("ClientId");
		return routerIP;
	}

	public String getSubscribedTopic() throws IOException {
		String routerIP = properties.getProperty("SubscribedTopic");
		return routerIP;
	}

	public int getQoS() throws IOException {
		int qos = Integer.parseInt(properties.getProperty("QoS"));
		return qos;
	}
}
