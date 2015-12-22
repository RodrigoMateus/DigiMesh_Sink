package com.maykot.mainApp;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import com.digi.xbee.api.listeners.IExplicitDataReceiveListener;
import com.digi.xbee.api.models.ExplicitXBeeMessage;
import com.maykot.http.ProxyHttp;
import com.maykot.maykottracker.radio.ErrorCode;
import com.maykot.maykottracker.radio.ProxyRequest;
import com.maykot.maykottracker.radio.ProxyResponse;

public class ExplicitDataReceiveListener implements IExplicitDataReceiveListener {

	HashMap<String, TreatRequest> listTrataRequest = new HashMap<String, TreatRequest>();

	@Override
	public void explicitDataReceived(ExplicitXBeeMessage explicitXBeeMessage) {
		TreatRequest treatRequest = null;

		if (listTrataRequest.containsKey(explicitXBeeMessage.getDevice().get64BitAddress().toString())) {
			treatRequest = listTrataRequest.get(explicitXBeeMessage.getDevice().get64BitAddress().toString());
		} else {
			treatRequest = new TreatRequest();
			listTrataRequest.put(explicitXBeeMessage.getDevice().get64BitAddress().toString(), treatRequest);
		}
		ExecutorService executor = Executors.newFixedThreadPool(1);
		treatRequest.process(explicitXBeeMessage);
		executor.execute(treatRequest);
		executor.shutdown();
	}

	class TreatRequest extends Thread {

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String mqttClientId = null;
		String mqttMessageId = null;
		ExplicitXBeeMessage explicitXBeeMessage;

		public synchronized void process(ExplicitXBeeMessage explicitXBeeMessage) {
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
					System.out.println(explicitXBeeMessage.getProfileID());
					byteArrayOutputStream.write(explicitXBeeMessage.getData());
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				break;

			case MainApp.ENDPOINT_HTTP_POST_SEND:

				byte[] tempByteArray = byteArrayOutputStream.toByteArray();
				byteArrayOutputStream.reset();

				String fileName = (new String(new SimpleDateFormat("yyyy-MM-dd_HHmmss_").format(new Date())))
						+ "image.txt";

				try {
					FileOutputStream fileChannel = new FileOutputStream(fileName);
					fileChannel.write(tempByteArray);
					fileChannel.close();
				} catch (FileNotFoundException e) {
					System.out.println("ERRO FileChannel");
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				ProxyResponse response = processRequest(tempByteArray, mqttClientId);

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

			case MainApp.ENDPOINT_IMG:

				String fileName2 = (new String(new SimpleDateFormat("yyyy-MM-dd_HHmmss_").format(new Date())))
						+ "image.png";

				try {
					byteArrayOutputStream.write(explicitXBeeMessage.getData());
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				try {
					FileOutputStream fileChannel = new FileOutputStream(fileName2);
					fileChannel.write(byteArrayOutputStream.toByteArray());
					fileChannel.close();
				} catch (FileNotFoundException e) {
					System.out.println("ERRO FileChannel");
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				byteArrayOutputStream.reset();

				break;

			default:
				break;
			}
		}

		private ProxyResponse processRequest(byte[] tempByteArray, String mqttClientId) {
			ProxyResponse response = null;

			try {
				ProxyRequest proxyRequest = (ProxyRequest) SerializationUtils.deserialize(tempByteArray);
				mqttMessageId = proxyRequest.getIdMessage();

				try {
					if (proxyRequest.getVerb().contains("get")) {
						response = ProxyHttp.getFile(proxyRequest);
					} else if (proxyRequest.getVerb().contains("post")) {
						response = ProxyHttp.postFile(proxyRequest);
					} else {
						response = new ProxyResponse(600, "application/json", ErrorCode.e600.getBytes());
					}
				} catch (Exception e) {
					response = new ProxyResponse(601, "application/json", ErrorCode.e601.getBytes());
				}
			} catch (Exception e) {
				response = new ProxyResponse(602, "application/json",
						new String(ErrorCode.e602 + e.getMessage() + "}").getBytes());
			}

			if (response == null) {
				response = new ProxyResponse(603, "application/json", ErrorCode.e603.getBytes());

			}
			System.out.println("MQTT Message ID = " + mqttMessageId);
			response.setMqttClientId(mqttClientId);
			response.setIdMessage(mqttMessageId);
			return response;
		}
	}
}
