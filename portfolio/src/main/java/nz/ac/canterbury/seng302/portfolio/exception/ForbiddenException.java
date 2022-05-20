package nz.ac.canterbury.seng302.portfolio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * <code>Code: HTTP 403</code>
 * <p>The current user IS logged in, but doesn't have permission to access.</p>
 */
@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "You do not have permission to access this endpoint.")
public class ForbiddenException extends RuntimeException {}
