package org.apache.coyote.http11.controller;

import org.apache.coyote.http11.request.HttpRequest;
import org.apache.coyote.http11.response.HttpResponse;

public class StaticResourceController extends AbstractController {

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) throws Exception {
        try {
            response.setOkResponse(getContentType(request.getPath()), getResponseBody(request.getPath()));
        } catch (IllegalArgumentException e) {
            response.setErrorResponse(getResponseBody("/404.html"));
        }
    }
}
