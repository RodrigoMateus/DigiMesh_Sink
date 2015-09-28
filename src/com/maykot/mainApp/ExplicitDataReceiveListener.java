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

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IExplicitDataReceiveListener;
import com.digi.xbee.api.models.ExplicitXBeeMessage;
import com.maykot.http.ProxyHttp;
import com.maykot.model.ProxyRequest;

public class ExplicitDataReceiveListener implements IExplicitDataReceiveListener {

	CloseableHttpClient httpClient = HttpClients.createDefault();
	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	FileChannel fileChannel;
	ByteBuffer buffer;
	boolean fileExist = false;

	@SuppressWarnings("resource")
	@Override
	public void explicitDataReceived(ExplicitXBeeMessage explicitXBeeMessage) {

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
			String response = ProxyHttp.postFile(proxyRequest);

			// Envia a resposta do POST para o dispositivo que enviou a
			// mensagem original (explicitXBeeMessage)
			try {
				MainApp.myDevice.sendData(explicitXBeeMessage.getDevice().get64BitAddress(), response.getBytes());
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
