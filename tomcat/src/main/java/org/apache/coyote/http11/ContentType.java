package org.apache.coyote.http11;

import java.util.Arrays;

public enum ContentType {

    TEXT_HTML(".html", "text/html;charset=utf-8"),
    TEXT_CSS(".css", "text/css;charset=utf-8"),
    APPLICATION_JAVASCRIPT(".js", "application/javascript"),
    IMAGE_X_ICO(".ico", "image/x-icon"),
    IMAGE_SVG(".svg", "image/svg+xml"),
    ;

    private final String extension;
    private final String mimeType;

    ContentType(final String extension, final String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public static ContentType getByPath(final String path) {
        return Arrays.stream(values())
            .filter(type -> path.endsWith(type.extension))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 ContentType 입니다."));
    }

    public String getMimeType() {
        return mimeType;
    }
}
