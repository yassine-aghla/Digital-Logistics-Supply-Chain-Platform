package org.example.digitallogisticssupplychainplatform.service;

import org.example.digitallogisticssupplychainplatform.entity.User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SimpleSessionService {

    private final Map<String, User> activeSessions = new ConcurrentHashMap<>();

    public String createSession(User user) {
        String token = "SESS_" + System.currentTimeMillis() + "_" + user.getId();
        activeSessions.put(token, user);
        return token;
    }

    public User getUserFromToken(String token) {
        return activeSessions.get(token);
    }

    public boolean isValidToken(String token) {
        return activeSessions.containsKey(token);
    }

    public void logout(String token) {
        activeSessions.remove(token);
    }
}