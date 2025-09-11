package org.apache.coyote.http11.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.coyote.http11.ContentType;
import org.apache.coyote.http11.request.HttpRequest;
import org.apache.coyote.http11.response.HttpResponse;

import com.techcourse.db.InMemoryUserRepository;
import com.techcourse.model.User;

public class RegisterController extends AbstractController {

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) throws Exception {
        try {
            response.setOkResponse(ContentType.TEXT_HTML, getResponseBody("/register.html"));
        } catch (URISyntaxException | IOException e) {
            response.setErrorResponse(getResponseBody("/500.html"));
        }
    }

    @Override
    protected void doPost(HttpRequest request, HttpResponse response) throws Exception {
        final Map<String, String> params = request.parseFormData();
        if (!params.containsKey("account") || !params.containsKey("password") || !params.containsKey("email")) {
            response.setRedirectionResponse("/401.html");
            return;
        }

        final String account = params.get("account");
        final String password = params.get("password");
        final String email = params.get("email");
        InMemoryUserRepository.save(new User(account, password, email));
        response.setRedirectionResponse("/index.html");
    }
}
