package org.apache.coyote.http11.response;

import java.util.Map;

import org.apache.coyote.http11.ContentType;
import org.apache.coyote.http11.request.HttpRequest;

public class HttpResponse {

    private static final String CRLF = "\r\n";

    private final String protocol;
    private final ResponseHeaders headers;

    private StatusCode statusCode;
    private byte[] responseBody;

    public HttpResponse(final HttpRequest request) {
        this.protocol = request.getProtocol();
        this.headers = new ResponseHeaders();
    }

    public void addHeader(String name, String value) {
        headers.setHeader(name, value);
    }

    public void setOkResponse(final ContentType contentType, final byte[] body) {
        statusCode = StatusCode.OK;
        responseBody = body;
        headers.setHeader("Content-Type", contentType.getMimeType());
        headers.setHeader("Content-Length", String.valueOf(body.length));
    }

    public void setRedirectionResponse(final String location) {
        statusCode = StatusCode.FOUND;
        headers.setHeader("Location", location);
    }

    public void setErrorResponse(byte[] body) {
        statusCode = StatusCode.INTERNAL_SERVER_ERROR;
        responseBody = body;
    }

    public String parseStatusLine() {
        return String.join(" ", protocol, String.valueOf(statusCode.getCode()), statusCode.getReasonPhrase(), CRLF);
    }

    public String parseResponseHeaders() {
        StringBuilder headerLines = new StringBuilder();
        for (Map.Entry<String, String> entry : headers.getHeaders().entrySet()) {
            String headerLine = String.join(": ", entry.getKey(), entry.getValue());
            headerLines.append(headerLine).append(" ").append(CRLF);
        }
        headerLines.append(CRLF);
        return headerLines.toString();
    }

    public String getProtocol() {
        return protocol;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public ResponseHeaders getHeaders() {
        return headers;
    }

    public byte[] getResponseBody() {
        return responseBody;
    }
}
