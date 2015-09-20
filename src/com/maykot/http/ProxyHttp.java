package com.maykot.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.maykot.model.ProxyRequest;

public class ProxyHttp {

	public static String postFile(ProxyRequest proxyRequest) {

		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse httpResponse = null;
		String response = null;

		try {
			HttpPost request = new HttpPost(proxyRequest.getUrl());
			request.addHeader("content-type", proxyRequest.getContentType());

			InputStream inputStream = new ByteArrayInputStream(proxyRequest.getBody());
			InputStreamEntity inputStreamEntity = new InputStreamEntity(inputStream, -1);
			request.setEntity(inputStreamEntity);

			httpResponse = httpClient.execute(request);

			// Faz alguma coisa com a resposta
			response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
			System.out.println(response);

		} catch (IOException ex) {
			System.out.println("ERRO Proxy");
		}
		return response;
	}
}
