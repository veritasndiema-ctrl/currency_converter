package com.mustcc.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.mustcc.dao.RoleDAO;
import com.mustcc.dao.SessionDAO;
import com.mustcc.dao.UserDAO;
import com.mustcc.exception.AuthException;
import com.mustcc.model.User;

/**
 * Handles the two things the schema anticipated but the original CLI never
 * used: users and user_sessions. Passwords are never stored or compared as
 * plain text — only their bcrypt hash ever reaches the database.
 */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    private final SessionDAO sessionDAO = new SessionDAO();

    public User register(String username, String email, String rawPassword) throws AuthException {
        if (userDAO.findByUsername(username).isPresent()) {
            throw new AuthException("Username already taken: " + username);
        }
        if (userDAO.findByEmail(email).isPresent()) {
            throw new AuthException("Email already registered: " + email);
        }

        int roleId = roleDAO.findIdByName("USER")
                .orElseThrow(() -> new AuthException("USER role is not seeded in the roles table"));

        String hash = BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray());
        User newUser = new User(0, username, email, hash, User.Role.USER);
        return userDAO.save(newUser, roleId);
    }

    /** Returns the logged-in user plus the session_id opened for them. */
    public LoginResult login(String username, String rawPassword, String ipAddress) throws AuthException {
        User user = userDAO.findByUsername(username)
                .orElseThrow(() -> new AuthException("Invalid username or password"));

        BCrypt.Result result = BCrypt.verifyer().verify(rawPassword.toCharArray(), user.getPasswordHash());
        if (!result.verified) {
            throw new AuthException("Invalid username or password");
        }

        int sessionId = sessionDAO.open(user.getUserId(), ipAddress);
        return new LoginResult(user, sessionId);
    }

    public void logout(int sessionId) {
        sessionDAO.close(sessionId);
    }

    public static final class LoginResult {
        public final User user;
        public final int sessionId;

        LoginResult(User user, int sessionId) {
            this.user = user;
            this.sessionId = sessionId;
        }
    }
}
