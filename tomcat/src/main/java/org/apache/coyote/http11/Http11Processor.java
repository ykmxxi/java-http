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
            handleLoginGet(request, response);
        } else {
            try {
                response.sendOk(getContentType(path), getResponseBody(path));
            } catch (URISyntaxException | IOException e) {
                response.sendError();
            }
        }
    }

    private void handleLoginGet(final HttpRequest request, final HttpResponse response) {
        String fullPath = request.getPath();

        if ("/login".equals(fullPath)) {
            try {
                response.sendOk(getContentType("/login.html"), getResponseBody("/login.html"));
            } catch (URISyntaxException | IOException e) {
                response.sendError();
            }
        } else if (fullPath.startsWith("/login?")) {
            handleLoginWithParams(fullPath, response);
        }
    }

    private void handleLoginWithParams(final String fullPath, final HttpResponse response) {
        try {
            int index = fullPath.indexOf("?");
            if (index == -1) {
                throw new IllegalArgumentException("ID와 PW를 입력해주세요.");
            }
            String queryString = fullPath.substring(index + 1);
            Map<String, String> params = new HashMap<>();
            for (String param : queryString.split("&")) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }

            String account = params.get("account");
            String password = params.get("password");
            User user = InMemoryUserRepository.findByAccount(account)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
            if (!user.checkPassword(password)) {
                response.sendRedirect("/401.html");
                return;
            }
            response.sendRedirect("/index.html");
        } catch (Exception e) {
            log.error("Login processing error: " + e.getMessage(), e);
            response.sendRedirect("/401.html");
        }
    }

    private void handlePostRequest(final HttpRequest request, final HttpResponse response) {
        String path = request.getPath();
        if ("/login".equals(path)) {
            handleLogin(request, response);
        }
    }

    private void handleLogin(final HttpRequest request, final HttpResponse response) {
        try {
            String body = request.getBody();
            Map<String, String> params = parseFormData(body);

            String account = params.get("account");
            String password = params.get("password");

            if ("gugu".equals(account) && "password".equals(password)) {
                response.sendRedirect("/index.html");
            } else {
                response.sendRedirect("/401.html");
            }
        } catch (Exception e) {
            log.error("Login processing error: " + e.getMessage(), e);
            response.sendRedirect("/401.html");
        }
    }

    private Map<String, String> parseFormData(String body) {
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
