package org.apache.coyote.http11.request;

import org.apache.coyote.http11.HttpMethod;

public class RequestLine {

    private final HttpMethod method;
    private final String path;
    private final String protocol;

    private RequestLine(final HttpMethod method, final String path, final String protocol) {
        this.method = method;
        this.path = path;
        this.protocol = protocol;
    }

    public static RequestLine of(final String requestLine) {
        final String[] requestLineTokens = requestLine.split(" ");
        if (requestLineTokens.length != 3) {
            throw new IllegalArgumentException("잘못된 HTTP RequestLine 입니다.");
        }
        return new RequestLine(HttpMethod.valueOf(requestLineTokens[0]), requestLineTokens[1], requestLineTokens[2]);
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
}
