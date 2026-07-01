package com.xeoscript.modules.customauthtoken.services;

import com.xeoscript.modules.customauthtoken.model.exception.TokenExpiredException;

import java.time.LocalDateTime;
import java.util.Map;

public interface SessionService {

    /**
     * Store a key-value pair in the session storage for a token.
     * Token must exist and not be expired.
     *
     * @param token the token value
     * @param key the session key
     * @param value the session value
     * @throws TokenExpiredException if token is not found or has expired
     */
    void save(String token, String key, String value) throws TokenExpiredException;

    /**
     * Retrieve a value from the session storage for a token.
     * Token must exist and not be expired.
     *
     * @param token the token value
     * @param key the session key
     * @return the session value, or null if key does not exist
     * @throws TokenExpiredException if token is not found or has expired
     */
    String get(String token, String key) throws TokenExpiredException;

    /**
     * Retrieve all session data for a token as a key-value map.
     * Token must exist and not be expired.
     *
     * @param token the token value
     * @return a map of all session key-value pairs, or empty map if no data exists
     * @throws TokenExpiredException if token is not found or has expired
     */
    Map<String, String> getAll(String token) throws TokenExpiredException;

    /**
     * Delete a specific session key from storage for a token.
     * Token must exist and not be expired.
     *
     * @param token the token value
     * @param key the session key
     * @return true if the key was deleted, false if it did not exist
     * @throws TokenExpiredException if token is not found or has expired
     */
    boolean delete(String token, String key) throws TokenExpiredException;

    /**
     * Clear all session data for a token.
     * Token must exist and not be expired.
     *
     * @param token the token value
     * @throws TokenExpiredException if token is not found or has expired
     */
    void clearAll(String token) throws TokenExpiredException;

    /**
     * Clean up all session data for tokens that have expired before the given time.
     * This is a maintenance operation and does not require token validation.
     *
     * @param before the cutoff time
     * @return the number of session records deleted
     */
    long cleanupExpiredSessions(LocalDateTime before);
}

