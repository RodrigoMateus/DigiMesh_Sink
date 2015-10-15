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

import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IExplicitDataReceiveListener;
import com.digi.xbee.api.models.ExplicitXBeeMessage;
import com.digi.xbee.api.utils.LogRecord;
import com.maykot.http.ProxyHttp;
import com.maykot.maykottracker.models.ProxyRequest;
import com.maykot.maykottracker.models.ProxyResponse;

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

//				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(tempByteArray);
//				ObjectInput objectInput = null;
//				ProxyRequest proxyRequest = null;
//
//				try {
//					objectInput = new ObjectInputStream(byteArrayInputStream);
//					proxyRequest = (ProxyRequest) objectInput.readObject();
//				} catch (ClassNotFoundException e2) {
//					e2.printStackTrace();
//				} catch (IOException e2) {
//					e2.printStackTrace();
//				}
				byteArrayOutputStream.reset();
				
				ProxyRequest proxyRequest = (ProxyRequest) SerializationUtils.deserialize(tempByteArray);

				ProxyResponse response = ProxyHttp.postFile(proxyRequest);
				response.setMqttClientId(mqttClientId);
				
				byte[] responseToSourceDevice = SerializationUtils.serialize(response);

				if (proxyRequest.getUrl().contentEquals("http://localhost:8000"))
					LogRecord.insertLog("localhost", new String(proxyRequest.getBody()));
				else
					LogRecord.insertLog("otmisnet", new String(proxyRequest.getBody()));

				// Envia a resposta do POST para o dispositivo que enviou a
				// mensagem original (explicitXBeeMessage)
				try {
					MainApp.myDevice.sendExplicitData(explicitXBeeMessage.getDevice().get64BitAddress(),
							MainApp.ENDPOINT_HTTP_RESPONSE, MainApp.ENDPOINT_HTTP_RESPONSE, MainApp.CLUSTER_ID,
							MainApp.PROFILE_ID, responseToSourceDevice);
				} catch (TimeoutException e1) {
					e1.printStackTrace();
				} catch (XBeeException e1) {
					e1.printStackTrace();
				}
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
