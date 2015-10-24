package com.maykot.mainApp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
	String mqttClientId = null;
	String mqttMessageId = null;


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
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

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

			default:
				break;
			}
		}

		private ProxyResponse processRequest(byte[] tempByteArray, String mqttClientId) {
			ProxyResponse response = null;

			try{
				ProxyRequest proxyRequest = (ProxyRequest) SerializationUtils.deserialize(tempByteArray);
				mqttMessageId = proxyRequest.getIdMessage();

				try{
					if (proxyRequest.getVerb().contains("get")) {
						response = ProxyHttp.getFile(proxyRequest);
					} else if (proxyRequest.getVerb().contains("post"))  {
						response = ProxyHttp.postFile(proxyRequest);
					}else{
						response = new ProxyResponse(600, "application/json", 
								new String("{exception:verb invalid}").getBytes());
					}
				}catch (Exception e) {
					response = new ProxyResponse(601, "application/json", 
							new String("{exception:not verb}").getBytes());
				}
			}catch (Exception e) {
				response = new ProxyResponse(602, "application/json", 
						new String("{exception:proxy request invalid, message:"+e.getMessage()+"}").getBytes());
			}
			
			if(response == null){
				response = new ProxyResponse(603, "application/json", 
						new String("{exception:request problem}").getBytes());
		
			}
			System.out.println("MQTT Message ID = " + mqttMessageId);
			response.setMqttClientId(mqttClientId);
			response.setIdMessage(mqttMessageId);
			return response;
		}
	}
}
