package com.maykot.maykottracker.models;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ProxyResponse implements Serializable {

	private static final long serialVersionUID = -3573373652237744379L;
	private int statusCode;
	private String contentType;
	private byte[] body;

	public ProxyResponse(int statusCode, String contentType, byte[] body) {
		super();
		this.statusCode = statusCode;
		this.contentType = contentType;
		this.body = body;
	}

	public byte[] toSerialize() {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutput objectOutput;
		try {
			objectOutput = new ObjectOutputStream(byteArrayOutputStream);
			objectOutput.writeObject(this);
			objectOutput.close();
			byteArrayOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return byteArrayOutputStream.toByteArray();
	}

	@Override
	public String toString() {
		return "ProxyResponse [statusCode=" + statusCode + ", contentType=" + contentType + ", body=" + new String(body)
				+ "]";
	}

}
