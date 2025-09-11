package org.apache.coyote.http11.response;

import java.io.IOException;
import java.io.OutputStream;

public class HttpResponseWriter {

    private final OutputStream outputStream;

    public HttpResponseWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void write(HttpResponse response) throws IOException {
        outputStream.write(response.parseStatusLine().getBytes());
        outputStream.write(response.parseResponseHeaders().getBytes());

        if (response.getResponseBody() != null) {
            outputStream.write(response.getResponseBody());
        }
    }
}
