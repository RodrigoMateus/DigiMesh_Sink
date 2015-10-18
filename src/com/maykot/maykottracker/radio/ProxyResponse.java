package com.maykot.maykottracker.radio;

import java.io.Serializable;
import java.util.Arrays;

public class ProxyResponse implements Serializable {

	private static final long serialVersionUID = -5387268491251047957L;
	private int statusCode;
	private String contentType;
	private String mqttClientId;
	private String idMessage;
	private byte[] body;
	
	public ProxyResponse(int statusCode, String contentType, byte[] body) {
		super();
		this.statusCode = statusCode;
		this.contentType = contentType;
		this.body = body;
	}
	
	public String getIdMessage() {
		return idMessage;
	}

	public void setIdMessage(String idMessage) {
		this.idMessage = idMessage;
	}
	
	public int getStatusCode() {
		return statusCode;
	}

	public String getContentType() {
		return contentType;
	}

	public byte[] getBody() {
		return body;
	}

	public String getMqttClientId() {
		return mqttClientId;
	}

	public void setMqttClientId(String mqttClientId) {
		this.mqttClientId = mqttClientId;
	}

	@Override
	public String toString() {
		return "ProxyResponse [statusCode=" + statusCode + ", contentType=" + contentType + ", mqttClientId="
				+ mqttClientId + ", body=" + Arrays.toString(body) + "]";
	}
}
