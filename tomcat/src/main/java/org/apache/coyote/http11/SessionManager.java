package org.apache.coyote.http11;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();
    private static final SessionManager INSTANCE = new SessionManager();

    public void add(final Session session) {
        SESSIONS.put(session.getId(), session);
    }

    public Session findSession(final String id) {
        return SESSIONS.get(id);
    }

    public void remove(final String id) {
        SESSIONS.remove(id);
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public Session createSession() {
        final String sessionId = UUID.randomUUID().toString();
        final Session session = new Session(sessionId);
        add(session);
        return session;
    }

    private SessionManager() {}
}

