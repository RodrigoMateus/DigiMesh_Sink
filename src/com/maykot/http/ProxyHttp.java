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

import com.maykot.maykottracker.models.ProxyRequest;
import com.maykot.maykottracker.models.ProxyResponse;

public class ProxyHttp {

	public static ProxyResponse postFile(ProxyRequest proxyRequest) {

		ProxyResponse proxyResponse = null;

		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse httpResponse = null;
		String response = null;
		System.out.println(proxyRequest.toString());
		try {
			HttpPost request = new HttpPost(proxyRequest.getUrl());
			request.addHeader("content-type", proxyRequest.getContentType());

			InputStream inputStream = new ByteArrayInputStream(proxyRequest.getBody());
			InputStreamEntity inputStreamEntity = new InputStreamEntity(inputStream, -1);
			request.setEntity(inputStreamEntity);

			httpResponse = httpClient.execute(request);

			// Faz alguma coisa com a resposta
			response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

			proxyResponse = new ProxyResponse(httpResponse.getStatusLine().getStatusCode(), "", response.getBytes());
			System.out.println(proxyResponse.toString());

		} catch (IOException ex) {
			System.out.println("ERRO Proxy");
		}
		return proxyResponse;
	}
}
