package org.apache.coyote.http11;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResponseHeaders {

    private final Map<String, String> headers = new LinkedHashMap<>();

    public ResponseHeaders() {
    }

    public void setHeader(String header, String value) {
        headers.put(header, value);
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }
}
