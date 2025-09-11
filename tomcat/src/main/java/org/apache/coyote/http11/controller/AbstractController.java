package org.apache.coyote.http11.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.coyote.http11.ContentType;
import org.apache.coyote.http11.request.HttpRequest;
import org.apache.coyote.http11.response.HttpResponse;

import com.techcourse.exception.NotFoundException;

public abstract class AbstractController implements Controller {

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        if (request.isGet()) {
            doGet(request, response);
        }
        if (request.isPost()) {
            doPost(request, response);
        }
    }

    protected ContentType getContentType(final String path) {
        return ContentType.getByPath(path);
    }

    protected byte[] getResponseBody(final String requestTarget) throws URISyntaxException, IOException {
        try {
            final Path resourcePath = Paths.get(getClass().getClassLoader()
                .getResource(String.join("", "static", requestTarget))
                .toURI());
            return Files.readAllBytes(resourcePath);
        } catch (NullPointerException e) {
            throw new NotFoundException("존재하지 않는 리소스입니다.");
        }
    }

    protected void doGet(HttpRequest request, HttpResponse response) throws Exception {
    }

    protected void doPost(HttpRequest request, HttpResponse response) throws Exception {
    }
}
