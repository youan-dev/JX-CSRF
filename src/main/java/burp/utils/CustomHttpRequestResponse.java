package burp.utils;

import burp.IHttpRequestResponse;
import burp.IHttpService;

/**
 * 自定义IHttpRequestResponse 对象生成
 */
public class CustomHttpRequestResponse implements IHttpRequestResponse {

    private byte[] request;
    private byte[] response;
    private IHttpService httpService;

    public  CustomHttpRequestResponse(byte[] request,byte[] response,IHttpService httpService){
        this.request=request;
        this.response=response;
        this.httpService=httpService;
    }

    @Override
    public byte[] getRequest() {
        return null == request ? new byte[0]:request;
    }

    @Override
    public void setRequest(byte[] message) {
        this.request=message;
    }

    @Override
    public byte[] getResponse() {
        return null == response ? new byte[0]:response;
    }

    @Override
    public void setResponse(byte[] message) {
        this.response=message;
    }

    @Override
    public String getComment() {
        return null;
    }

    @Override
    public void setComment(String comment) {

    }

    @Override
    public String getHighlight() {
        return null;
    }

    @Override
    public void setHighlight(String color) {

    }

    @Override
    public IHttpService getHttpService() {
        return null == httpService ? null:httpService;
    }

    @Override
    public void setHttpService(IHttpService httpService) {
        this.httpService=httpService;
    }
}
