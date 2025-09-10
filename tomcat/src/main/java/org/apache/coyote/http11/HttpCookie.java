package org.apache.coyote.http11;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpCookie {

    private final Map<String, String> cookies = new HashMap<>();

    public void addCookie(final String name, final String value) {
        cookies.put(name, value);
    }

    public Map<String, String> getCookies() {
        return Collections.unmodifiableMap(cookies);
    }
}
