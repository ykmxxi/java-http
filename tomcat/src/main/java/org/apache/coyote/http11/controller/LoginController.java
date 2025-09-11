package org.apache.coyote.http11.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Collectors;

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
            response.sendRedirect("/index.html");
            return;
        }

        try {
            response.sendOk(ContentType.TEXT_HTML, getResponseBody("/login.html"));
        } catch (URISyntaxException | IOException e) {
            response.sendError(getResponseBody("/500.html"));
        }
    }

    @Override
    protected void doPost(HttpRequest request, HttpResponse response) throws Exception {
        try {
            Map<String, String> params = request.parseFormData();
            if (!params.containsKey("account") || !params.containsKey("password")) {
                response.sendRedirect("/401.html");
                return;
            }

            String account = params.get("account");
            String password = params.get("password");
            User user = InMemoryUserRepository.findByAccount(account)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
            if (!user.checkPassword(password)) {
                response.sendRedirect("/401.html");
                return;
            }

            final Session session = request.getSession(true);
            final HttpCookie httpCookie = new HttpCookie();
            session.setAttribute("user", user);
            httpCookie.addCookie("JSESSIONID", session.getId());
            String cookieValue = httpCookie.getCookies().entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("; "));
            response.addHeader("Set-Cookie", cookieValue);
            response.sendRedirect("/index.html");
        } catch (IllegalArgumentException e) {
            response.sendRedirect("/401.html");
        }
    }

    private User getUser(Session session) {
        return (User)session.getAttribute("user");
    }
}
