package org.example.oct19.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/** Institution authentication integration boundary; a bearer string alone is not an authenticated identity. */
public interface AuthenticationAdapter {
    /** Verifies approved credentials; returns their identity, or empty for absent/invalid credentials. */
    Optional<Identity> authenticate(HttpServletRequest request);
}
