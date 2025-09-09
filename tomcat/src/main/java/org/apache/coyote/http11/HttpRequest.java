package org.apache.coyote.http11;

import java.io.BufferedReader;
import java.io.IOException;

public class HttpRequest {

    private final HttpMethod method;
    private final String path;
    private final String protocol;
    private final RequestHeaders headers;
    private final String body;

    private HttpRequest(HttpMethod method, String path, String protocol, RequestHeaders headers, String body) {
        this.method = method;
        this.path = path;
        this.protocol = protocol;
        this.headers = headers;
        this.body = body;
    }

    public static HttpRequest from(final BufferedReader reader) {
        try {
            final String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isBlank()) {
                throw new IllegalArgumentException("HTTP 요청 라인이 존재하지 않습니다.");
            }
            String[] requestLineToken = requestLine.split(" ");
            if (requestLineToken.length != 3) {
                throw new IllegalArgumentException("HTTP 요청 라인이 형식에 맞지 않습니다.");
            }

            final HttpMethod method = HttpMethod.valueOf(requestLineToken[0]);
            final String path = requestLineToken[1];
            final String protocol = requestLineToken[2];
            final RequestHeaders requestHeaders = new RequestHeaders(reader);
            final String body = getBody(method, requestHeaders, reader);
            return new HttpRequest(method, path, protocol, requestHeaders, body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getBody(final HttpMethod method, final RequestHeaders headers, final BufferedReader reader) {
        if (method.isGet()) {
            return "";
        }
        try {
            int contentLength = headers.getContentLength();
            char[] buffer = new char[contentLength];
            reader.read(buffer, 0, contentLength);
            return new String(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isGet() {
        return method.isGet();
    }

    public boolean isPost() {
        return method.isPost();
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getProtocol() {
        return protocol;
    }

    public RequestHeaders getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}
