package com.maykot.mainApp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.digi.xbee.api.listeners.IExplicitDataReceiveListener;
import com.digi.xbee.api.models.ExplicitXBeeMessage;
import com.maykot.http.ProxyHttp;
import com.maykot.maykottracker.radio.ProxyRequest;
import com.maykot.maykottracker.radio.ProxyResponse;

public class ExplicitDataReceiveListener implements IExplicitDataReceiveListener {

	CloseableHttpClient httpClient = HttpClients.createDefault();
	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	FileChannel fileChannel;
	ByteBuffer buffer;
	boolean fileExist = false;
	String mqttClientId;

	@Override
	public void explicitDataReceived(ExplicitXBeeMessage explicitXBeeMessage) {
		ExecutorService executor = Executors.newFixedThreadPool(20);
		executor.execute(new TreatRequest(explicitXBeeMessage));
		executor.shutdown();
	}

	class TreatRequest extends Thread {

		ExplicitXBeeMessage explicitXBeeMessage;

		public TreatRequest(ExplicitXBeeMessage explicitXBeeMessage) {
			this.explicitXBeeMessage = explicitXBeeMessage;
		}

		@Override
		public void run() {
			int endPoint = explicitXBeeMessage.getDestinationEndpoint();
			switch (endPoint) {

			case MainApp.ENDPOINT_HTTP_POST_INIT:

				mqttClientId = new String(explicitXBeeMessage.getData());
				System.out.println("MQTT Client ID = " + mqttClientId);
				break;

			case MainApp.ENDPOINT_HTTP_POST_DATA:

				try {
					byteArrayOutputStream.write(explicitXBeeMessage.getData());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				break;

			case MainApp.ENDPOINT_HTTP_POST_SEND:

				byte[] tempByteArray = byteArrayOutputStream.toByteArray();
				byteArrayOutputStream.reset();

				ProxyRequest proxyRequest = (ProxyRequest) SerializationUtils.deserialize(tempByteArray);
				ProxyResponse response = null;

				if (proxyRequest.getVerb().contains("get")) {
					response = ProxyHttp.getFile(proxyRequest);
					response.setMqttClientId(mqttClientId);
				} else {
					response = ProxyHttp.postFile(proxyRequest);
					response.setMqttClientId(mqttClientId);
				}

				byte[] responseToSourceDevice = SerializationUtils.serialize(response);
				byte[] mqttClientIdToBytes = mqttClientId.getBytes();
				byte[] noMessage = new String("noMessage").getBytes();

				SendResponseMessage.send(MainApp.myDevice, mqttClientIdToBytes, MainApp.ENDPOINT_RESPONSE_INIT,
						explicitXBeeMessage.getDevice().get64BitAddress());
				SendResponseMessage.send(MainApp.myDevice, responseToSourceDevice, MainApp.ENDPOINT_RESPONSE_DATA,
						explicitXBeeMessage.getDevice().get64BitAddress());
				SendResponseMessage.send(MainApp.myDevice, noMessage, MainApp.ENDPOINT_RESPONSE_SEND,
						explicitXBeeMessage.getDevice().get64BitAddress());
				break;

			case MainApp.ENDPOINT_TXT:

				System.out.format("From %s >> %s%n", explicitXBeeMessage.getDevice().get64BitAddress(),
						new String(explicitXBeeMessage.getData()));
				break;

			default:
				break;
			}
		}
	}
}
