package org.apache.coyote.http11.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.coyote.http11.HttpMethod;
import org.apache.coyote.http11.Session;
import org.apache.coyote.http11.SessionManager;

public class HttpRequest {

    private final RequestLine requestLine;
    private final RequestHeaders headers;
    private final String body;

    private HttpRequest(final RequestLine requestLine, RequestHeaders headers, String body) {
        this.requestLine = requestLine;
        this.headers = headers;
        this.body = body;
    }

    public static HttpRequest from(final BufferedReader reader) {
        try {
            final String firstLine = reader.readLine();
            if (firstLine == null || firstLine.isEmpty()) {
                throw new IllegalArgumentException("HTTP 요청 라인이 존재하지 않습니다.");
            }
            String[] requestLineToken = firstLine.split(" ");
            if (requestLineToken.length != 3) {
                throw new IllegalArgumentException("HTTP 요청 라인이 형식에 맞지 않습니다.");
            }

            final RequestLine requestLine = RequestLine.of(firstLine);
            final RequestHeaders requestHeaders = new RequestHeaders(reader);
            final String body = getBody(requestLine, requestHeaders, reader);
            return new HttpRequest(requestLine, requestHeaders, body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getBody(final RequestLine method, final RequestHeaders headers, final BufferedReader reader) {
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
        return requestLine.isGet();
    }

    public boolean isPost() {
        return requestLine.isPost();
    }

    public Session getSession(final boolean create) {
        final String jsessionId = getJSessionIdFromCookie();
        final SessionManager sessionManager = SessionManager.getInstance();
        if (jsessionId != null) {
            final Session session = sessionManager.findSession(jsessionId);
            if (session != null) {
                return session;
            }
        }
        if (create) {
            return sessionManager.createSession();
        }
        return null;
    }

    private String getJSessionIdFromCookie() {
        final String cookieHeader = headers.getCookie();
        if (cookieHeader == null) {
            return null;
        }
        for (String cookie : cookieHeader.split(";")) {
            final String[] parts = cookie.trim().split("=");
            if (parts.length == 2 && "JSESSIONID".equals(parts[0])) {
                return parts[1];
            }
        }
        return null;
    }

    public Map<String, String> parseFormData() {
        Map<String, String> params = new HashMap<>();
        if (body != null && !body.isEmpty()) {
            for (String param : body.split("&")) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }

    public HttpMethod getMethod() {
        return requestLine.getMethod();
    }

    public String getPath() {
        return requestLine.getPath();
    }

    public String getProtocol() {
        return requestLine.getProtocol();
    }

    public RequestHeaders getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}
