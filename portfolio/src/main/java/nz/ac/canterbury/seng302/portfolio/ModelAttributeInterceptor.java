package nz.ac.canterbury.seng302.portfolio;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import nz.ac.canterbury.seng302.portfolio.utils.PrincipalData;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;

/**
 * Interceptor for the template renderer.
 * 
 * Use this class to add any values that you want to be globally accessable.
 */
@Component
public class ModelAttributeInterceptor implements AsyncHandlerInterceptor {

    /**
     * Add any globally accessable attributes you want below.
     * <p>
     * <b>One condition:</b> Give them a G_ prefix to prevent namespace
     * clashes.
     * Example: <code>G_fullname</code> instead of <code>fullname</code>
     * </p>
     */
    @Override
    public void postHandle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler,
            final ModelAndView modelAndView) throws Exception {
        // Don't bother on pages without models (i.e. response bodies)
        if (modelAndView == null) {
            return;
        }
        ModelMap model = modelAndView.getModelMap();
        
        // * OK then, add 'em below
        // Adds the user's principaldata globally, so any template (namely the header
        // fragment) can easily access it.
        PrincipalData thisUser = extractPrincipalDataFromRequest(request);
        model.addAttribute("G_PrincipalData", thisUser);
    }

    /**
     * <h3>Grabs the Principal from a HttpServletRequest</h3>
     * This method exists because of the weird casting needed to get the Principal.
     * @return A PrincipalData from the request. Note that it can be an authenticated Principal.
     */
    private static PrincipalData extractPrincipalDataFromRequest(HttpServletRequest request) {
        // For some reason, getUserPrincipal() gets the whole token.
        PreAuthenticatedAuthenticationToken token = (PreAuthenticatedAuthenticationToken) request.getUserPrincipal();
        if (token == null) {
            return PrincipalData.unauthenticated();
        }
        // Get the principal for real this time.
        AuthState principal = (AuthState) token.getPrincipal();
        if (principal == null) {
            return PrincipalData.unauthenticated();
        }
        return PrincipalData.from(principal);
    }
}
