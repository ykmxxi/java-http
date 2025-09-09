package org.apache.coyote.http11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.coyote.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.techcourse.db.InMemoryUserRepository;
import com.techcourse.exception.UncheckedServletException;
import com.techcourse.model.User;

public class Http11Processor implements Runnable, Processor {

    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Socket connection;

    public Http11Processor(final Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.info("connect host: {}, port: {}", connection.getInetAddress(), connection.getPort());
        process(connection);
    }

    @Override
    public void process(final Socket connection) {
        try (final var inputStream = connection.getInputStream();
             final var outputStream = connection.getOutputStream();
             final var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
        ) {
            final HttpRequest request = HttpRequest.from(reader);
            final HttpResponse response = new HttpResponse(outputStream, request);

            sendResponse(request, response);
        } catch (IOException | UncheckedServletException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void sendResponse(final HttpRequest request, final HttpResponse response) {
        if (request.isGet()) {
            handleGetRequest(request, response);
        }
        if (request.isPost()) {
            handlePostRequest(request, response);
        }
    }

    private void handleGetRequest(final HttpRequest request, final HttpResponse response) {
        String path = request.getPath();

        if (path.startsWith("/login")) {
            handleLoginGet(response);
        } else if (path.startsWith("/register")) {
            handleRegisterGet(response);
        } else {
            try {
                response.sendOk(getContentType(path), getResponseBody(path));
            } catch (URISyntaxException | IOException e) {
                response.sendError();
            }
        }
    }

    private void handleLoginGet(final HttpResponse response) {
        try {
            response.sendOk(getContentType("/login.html"), getResponseBody("/login.html"));
        } catch (URISyntaxException | IOException e) {
            response.sendError();
        }
    }

    private void handleRegisterGet(final HttpResponse response) {
        try {
            response.sendOk(getContentType("/register.html"), getResponseBody("/register.html"));
        } catch (URISyntaxException | IOException e) {
            response.sendError();
        }
    }

    private void handlePostRequest(final HttpRequest request, final HttpResponse response) {
        String path = request.getPath();
        if ("/login".equals(path)) {
            handleLogin(request, response);
        }
        if ("/register".equals(path)) {
            handleRegister(request, response);
        }
    }

    private void handleLogin(final HttpRequest request, final HttpResponse response) {
        try {
            String body = request.getBody();
            Map<String, String> params = parseFormData(body);

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
            HttpCookie httpCookie = new HttpCookie();
            httpCookie.addCookie("JSESSIONID", UUID.randomUUID().toString());
            String cookieValue = httpCookie.getCookies().entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("; "));
            response.addHeader("Set-Cookie", cookieValue);
            response.sendRedirect("/index.html");
        } catch (IllegalArgumentException e) {
            response.sendRedirect("/401.html");
        }
    }

    private void handleRegister(final HttpRequest request, final HttpResponse response) {
        String body = request.getBody();
        Map<String, String> params = parseFormData(body);

        if (!params.containsKey("account") || !params.containsKey("password") || !params.containsKey("email")) {
            response.sendRedirect("/401.html");
            return;
        }

        String account = params.get("account");
        String password = params.get("password");
        String email = params.get("email");
        InMemoryUserRepository.save(new User(account, password, email));
        response.sendRedirect("/index.html");
    }

    private Map<String, String> parseFormData(final String body) {
        Map<String, String> params = new HashMap<>();
        if (body != null && !body.isEmpty()) {
            for (String param : body.split("&")) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }

    private ContentType getContentType(final String requestTarget) {
        try {
            return ContentType.getByRequestTarget(requestTarget);
        } catch (IllegalArgumentException e) {
            return ContentType.TEXT_HTML;
        }
    }

    private byte[] getResponseBody(final String requestTarget) throws URISyntaxException, IOException {
        if ("/".equals(requestTarget)) {
            return "Hello world!".getBytes(StandardCharsets.UTF_8);
        }
        final Path resourcePath = Path.of(Objects.requireNonNull(
            getClass().getClassLoader()
                .getResource(String.join("", "static", requestTarget))
        ).toURI());
        return Files.readAllBytes(resourcePath);
    }
}
