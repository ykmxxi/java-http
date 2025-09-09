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
            String path = request.getPath();
            try {
                response.sendOk(getContentType(path), getResponseBody(path));
            } catch (URISyntaxException | IOException e) {
                response.sendError();
            }
        }
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
        if (requestTarget.startsWith("/login")) {
            int index = requestTarget.indexOf("?");
            if (index == -1) {
                throw new IllegalArgumentException("ID와 PW를 입력해주세요.");
            }
            String path = requestTarget.substring(0, index);
            String queryString = requestTarget.substring(index + 1);
            Map<String, String> params = new HashMap<>();
            for (String param : queryString.split("&")) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }

            User user = InMemoryUserRepository.findByAccount(params.get("account"))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
            if (!user.checkPassword(params.get("password"))) {
                throw new IllegalArgumentException("ID 또는 PW가 일치하지 않습니다.");
            }
            log.info("로그인 성공. user: {}", user);
            log.info("path: {}", path);

            final Path resourcePath = Path.of(Objects.requireNonNull(
                getClass().getClassLoader()
                    .getResource(String.join("", "static", path + ".html"))
            ).toURI());
            return Files.readAllBytes(resourcePath);
        }
        final Path resourcePath = Path.of(Objects.requireNonNull(
            getClass().getClassLoader()
                .getResource(String.join("", "static", requestTarget))
        ).toURI());
        return Files.readAllBytes(resourcePath);
    }
}
