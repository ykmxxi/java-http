package org.apache.coyote.http11.controller;

import org.apache.coyote.http11.ContentType;
import org.apache.coyote.http11.request.HttpRequest;
import org.apache.coyote.http11.response.HttpResponse;

public class HelloController extends AbstractController {

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) {
        response.setOkResponse(ContentType.TEXT_HTML, "Hello world!".getBytes());
    }
}
