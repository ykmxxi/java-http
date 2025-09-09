package org.apache.coyote.http11;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class HttpResponse {

    private static final String CRLF = "\r\n";

    private final String protocol;
    private final ResponseHeaders headers;
    private final OutputStream outputStream;

    private StatusCode statusCode;
    private byte[] responseBody;

    public HttpResponse(final OutputStream outputStream, final HttpRequest request) {
        this.protocol = request.getProtocol();
        this.headers = new ResponseHeaders();
        this.outputStream = outputStream;
    }

    public void addHeader(String name, String value) {
        headers.setHeader(name, value);
    }

    public void sendOk(final ContentType contentType, final byte[] body) {
        statusCode = StatusCode.OK;
        responseBody = body;
        headers.setHeader("Content-Type", contentType.getMimeType());
        headers.setHeader("Content-Length", String.valueOf(body.length));

        try {
            outputStream.write(parseStatusLine().getBytes());
            outputStream.write(parseResponseHeaders().getBytes());
            outputStream.write(responseBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String parseStatusLine() {
        return String.join(" ", protocol, String.valueOf(statusCode.getCode()), statusCode.getReasonPhrase(), CRLF);
    }

    private String parseResponseHeaders() {
        StringBuilder headerLines = new StringBuilder();
        for (Map.Entry<String, String> entry : headers.getHeaders().entrySet()) {
            String headerLine = String.join(": ", entry.getKey(), entry.getValue());
            headerLines.append(headerLine).append(" ").append(CRLF);
        }
        headerLines.append(CRLF);
        return headerLines.toString();
    }

    public void sendNotFound(final ContentType contentType, final byte[] body) {
        statusCode = StatusCode.NOT_FOUND;
        responseBody = body;
        headers.setHeader("Content-Type", contentType.getMimeType());
        headers.setHeader("Content-Length", String.valueOf(body.length));
    }

    public void sendError() {
        statusCode = StatusCode.INTERNAL_SERVER_ERROR;

        try {
            outputStream.write(parseStatusLine().getBytes());
            outputStream.write(parseResponseHeaders().getBytes());
            outputStream.write(responseBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendRedirect(final String location) {
        statusCode = StatusCode.FOUND;
        headers.setHeader("Location", location);

        try {
            outputStream.write(parseStatusLine().getBytes());
            outputStream.write(parseResponseHeaders().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
