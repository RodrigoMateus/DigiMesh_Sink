package com.maykot.mainApp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.digi.xbee.api.listeners.IExplicitDataReceiveListener;
import com.digi.xbee.api.models.ExplicitXBeeMessage;
import com.digi.xbee.api.utils.LogRecord;
import com.maykot.http.ProxyHttp;

import br.com.jacto.otmisnet.mqtt.ProxyRequest;
import br.com.jacto.otmisnet.mqtt.ProxyResponse;

public class ExplicitDataReceiveListener implements IExplicitDataReceiveListener {

	CloseableHttpClient httpClient = HttpClients.createDefault();
	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	FileChannel fileChannel;
	ByteBuffer buffer;
	boolean fileExist = false;

	@Override
	public void explicitDataReceived(ExplicitXBeeMessage explicitXBeeMessage) {
		ExecutorService executor = Executors.newFixedThreadPool(20);
		executor.execute(new TrataRequisao(explicitXBeeMessage));
		executor.shutdown();
	}

	class TrataRequisao extends Thread {

		ExplicitXBeeMessage explicitXBeeMessage;

		public TrataRequisao(ExplicitXBeeMessage explicitXBeeMessage) {
			this.explicitXBeeMessage = explicitXBeeMessage;
		}

		@SuppressWarnings("resource")
		@Override
		public void run() {
			int endPoint = explicitXBeeMessage.getDestinationEndpoint();
			switch (endPoint) {

			case MainApp.ENDPOINT_HTTP_POST_INIT:

				System.out.println("Post INIT");
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

				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(tempByteArray);
				ObjectInput objectInput = null;
				ProxyRequest proxyRequest = null;

				try {
					objectInput = new ObjectInputStream(byteArrayInputStream);
					proxyRequest = (ProxyRequest) objectInput.readObject();
				} catch (ClassNotFoundException e2) {
					e2.printStackTrace();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				byteArrayOutputStream.reset();

				@SuppressWarnings("unused")
				ProxyResponse response = ProxyHttp.postFile(proxyRequest);

				if (proxyRequest.getUrl().contentEquals("http://localhost:8000"))
					LogRecord.insertLog("localhost", new String(proxyRequest.getBody()));
				else
					LogRecord.insertLog("otmisnet", new String(proxyRequest.getBody()));

				// Envia a resposta do POST para o dispositivo que enviou a
				// mensagem original (explicitXBeeMessage)
				// try {
				// MainApp.myDevice.sendData(explicitXBeeMessage.getDevice().get64BitAddress(),
				// response.toSerialize());
				// } catch (TimeoutException e1) {
				// e1.printStackTrace();
				// } catch (XBeeException e1) {
				// e1.printStackTrace();
				// }
				break;

			case MainApp.ENDPOINT_TXT:

				System.out.format("From %s >> %s%n", explicitXBeeMessage.getDevice().get64BitAddress(),
						new String(explicitXBeeMessage.getData()));
				break;

			case MainApp.ENDPOINT_FILENEW:
				String fileName = (new String(new SimpleDateFormat("yyyy-MM-dd_HHmmss_").format(new Date())))
						+ explicitXBeeMessage.getDataString();

				try {
					fileChannel = new FileOutputStream(fileName, true).getChannel();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				break;

			case MainApp.ENDPOINT_FILEDATA:
				byte[] dataReceived = explicitXBeeMessage.getData();
				buffer = ByteBuffer.wrap(dataReceived);

				try {
					fileChannel.write(buffer);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;

			case MainApp.ENDPOINT_FILECLOSE:

				try {
					fileChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;

			default:
				break;
			}
		}
	}
}
