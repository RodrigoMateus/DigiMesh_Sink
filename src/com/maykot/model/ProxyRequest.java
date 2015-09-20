package com.maykot.model;

import java.io.Serializable;

public class ProxyRequest implements Serializable {

    private static final long serialVersionUID = -4707248583815599159L;
    private String url;
    private String contentType;
    private byte[] body;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
