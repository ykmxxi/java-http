package org.apache.coyote.http11;

public enum HttpMethod {

    GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE, CONNECT, PATCH,
    ;

    public boolean isGet() {
        return this == GET;
    }

    public boolean isPost() {
        return this == POST;
    }
}
