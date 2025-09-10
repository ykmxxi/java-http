package org.apache.coyote.http11.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RequestHeaders {

    private final Map<String, String> headers = new HashMap<>();

    public RequestHeaders(final BufferedReader reader) {
        try {
            String line = "";
            while (!(line = reader.readLine()).isEmpty()) {
                int index = line.indexOf(":");
                if (index == -1) {
                    throw new IllegalArgumentException("HTTP 헤더가 형식에 맞지 않습니다.");
                }
                String key = line.substring(0, index).trim();
                String value = line.substring(index + 1).trim();
                headers.put(key, value);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getContentLength() {
        if (headers.containsKey("Content-Length")) {
            return Integer.parseInt(headers.get("Content-Length"));
        }
        throw new IllegalStateException("Content-Length 헤더가 존재하지 않습니다.");
    }

    public String getCookie() {
        return headers.get("Cookie");
    }
}
