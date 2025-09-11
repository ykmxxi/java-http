package org.apache.coyote.http11;

import java.util.HashMap;
import java.util.Map;

import org.apache.coyote.http11.controller.Controller;
import org.apache.coyote.http11.controller.HelloController;
import org.apache.coyote.http11.controller.LoginController;
import org.apache.coyote.http11.controller.RegisterController;
import org.apache.coyote.http11.controller.StaticResourceController;
import org.apache.coyote.http11.request.HttpRequest;

public class RequestMapping {

    private static final Map<String, Controller> CONTROLLERS;
    private static final StaticResourceController STATIC_RESOURCE_CONTROLLER = new StaticResourceController();

    static {
        CONTROLLERS = new HashMap<>();
        CONTROLLERS.put("/", new HelloController());
        CONTROLLERS.put("/login", new LoginController());
        CONTROLLERS.put("/register", new RegisterController());
    }

    private RequestMapping() {
    }

    public static Controller getController(final HttpRequest request) {
        return CONTROLLERS.getOrDefault(request.getPath(), STATIC_RESOURCE_CONTROLLER);
    }
}
