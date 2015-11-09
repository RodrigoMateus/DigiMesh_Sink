package com.mykot.test;

import java.util.HashMap;

import org.junit.Ignore;
import org.junit.Test;

import com.maykot.http.ProxyHttp;
import com.maykot.radiolibrary.ProxyRequest;
import com.maykot.radiolibrary.ProxyResponse;

public class TestHttp {

	@Ignore
	@Test
	public void test() {

		HashMap<String, String> header = new HashMap<>();
		header.put("content-type", "application/json");
		ProxyRequest proxyRequest = new ProxyRequest();
		proxyRequest.setUrl("www.univem.edu.br");
		proxyRequest.setVerb("get");
		proxyRequest.setIdMessage("0");
		proxyRequest.setHeader(header);
	}

	@Test
	public void testProcessRequest() {

		HashMap<String, String> header = new HashMap<>();
		header.put("content-type", "application/json");
		ProxyRequest proxyRequest = new ProxyRequest();
		proxyRequest.setUrl("http://www.univem.edu.br");
		proxyRequest.setVerb("get");
		proxyRequest.setIdMessage("0");
		proxyRequest.setHeader(header);
		// proxyRequest.setBody("sdsds".getBytes());

		ProxyResponse response = null;

		String mqttClientId = "teste";

		try {

			try {
				if (proxyRequest.getVerb().contentEquals("get")) {
					response = ProxyHttp.getFile(proxyRequest);
				} else if (proxyRequest.getVerb().contentEquals("post")) {
					response = ProxyHttp.postFile(proxyRequest);
				} else {
					response = new ProxyResponse(600, "application/json",
							new String("{exception:verb invalid}").getBytes());
				}
			} catch (Exception e) {
				response = new ProxyResponse(601, "application/json", new String("{exception:not verb}").getBytes());
			}
		} catch (Exception e) {
			response = new ProxyResponse(602, "application/json",
					new String("{exception:proxy request invalid, message:" + e.getMessage() + "}").getBytes());
		}

		if (response == null) {
			response = new ProxyResponse(603, "application/json", new String("{exception:request problem}").getBytes());

		}
		response.setMqttClientId(mqttClientId);

		response.toString();
	}

}
