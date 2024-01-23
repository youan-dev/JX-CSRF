package burp;

import java.net.URL;

public class RequestResponseData  {
    private String method;
    private String url;
    private short statusCode;
    private int length;
    private byte[] request;
    private byte[] response;

    // 构造函数、getter和setter方法...
    // 构造函数
    public RequestResponseData(String method, String url, short statusCode, int length,
                               byte[] requestContent, byte[] responseContent) {
        this.method = method;
        this.url = url;
        this.statusCode = statusCode;
        this.length = length;
        this.request = requestContent;
        this.response = responseContent;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(short statusCode) {
        this.statusCode = statusCode;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getRequest() {
        return request;
    }

    public void setRequest(byte[] request) {
        this.request = request;
    }

    public byte[] getResponse() {
        return response;
    }

    public void setResponse(byte[] response) {
        this.response = response;
    }
}
