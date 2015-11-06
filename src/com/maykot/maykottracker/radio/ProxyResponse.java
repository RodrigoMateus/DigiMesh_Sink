package com.maykot.maykottracker.radio;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

public class ProxyResponse implements Serializable {

	private static final long serialVersionUID = -5387268491251047957L;
	private int statusCode;
	private HashMap<String, String> header;
	private String mqttClientId;
	private String idMessage;
	private byte[] body;

	public ProxyResponse(int statusCode, String contentType, byte[] body) {
		super();
		this.statusCode = statusCode;
		this.body = body;
	}

	public int getStatusCode() { return statusCode; }

	public HashMap<String, String> getHeader() { return header; }

	public void setHeader(HashMap<String, String> header) { this.header = header; }

	public String getMqttClientId() { return mqttClientId; }

	public void setMqttClientId(String mqttClientId) { this.mqttClientId = mqttClientId; }

	public String getIdMessage() { return idMessage; }

	public void setIdMessage(String idMessage) { this.idMessage = idMessage; }

	public byte[] getBody() { return body; }

	@Override
	public String toString() {
		return "ProxyResponse [statusCode=" + statusCode + ", mqttClientId=" + mqttClientId + ", body="
				+ Arrays.toString(body) + "]";
	}
}
