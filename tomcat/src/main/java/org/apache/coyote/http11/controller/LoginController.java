package org.apache.coyote.http11.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.coyote.http11.ContentType;
import org.apache.coyote.http11.HttpCookie;
import org.apache.coyote.http11.Session;
import org.apache.coyote.http11.request.HttpRequest;
import org.apache.coyote.http11.response.HttpResponse;

import com.techcourse.db.InMemoryUserRepository;
import com.techcourse.model.User;

public class LoginController extends AbstractController {

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) throws Exception {
        final Session session = request.getSession(false);
        if (session != null && getUser(session) != null) {
            response.setRedirectionResponse("/index.html");
            return;
        }

        try {
            response.setOkResponse(ContentType.TEXT_HTML, getResponseBody("/login.html"));
        } catch (URISyntaxException | IOException e) {
            response.setErrorResponse(getResponseBody("/500.html"));
        }
    }

    @Override
    protected void doPost(HttpRequest request, HttpResponse response) throws Exception {
        try {
            User user = findUser(request, response);
            if (user == null) {
                return;
            }

            final Session session = request.getSession(true);
            session.setAttribute("user", user);
            final HttpCookie httpCookie = new HttpCookie();
            httpCookie.addCookie("JSESSIONID", session.getId());
            final String cookieValue = httpCookie.getCookieValue();
            response.addHeader("Set-Cookie", cookieValue);
            response.setRedirectionResponse("/index.html");
        } catch (IllegalArgumentException e) {
            response.setRedirectionResponse("/401.html");
        }
    }

    private User findUser(HttpRequest request, HttpResponse response) {
        final Map<String, String> params = request.parseFormData();
        if (!params.containsKey("account") || !params.containsKey("password")) {
            response.setRedirectionResponse("/401.html");
            return null;
        }

        final String account = params.get("account");
        final String password = params.get("password");
        final User user = InMemoryUserRepository.findByAccount(account)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        if (!user.checkPassword(password)) {
            response.setRedirectionResponse("/401.html");
            return null;
        }
        return user;
    }

    private User getUser(Session session) {
        return (User)session.getAttribute("user");
    }
}
