package nz.ac.canterbury.seng302.portfolio.controller;

import io.grpc.StatusRuntimeException;
import nz.ac.canterbury.seng302.portfolio.authentication.CookieUtil;
import nz.ac.canterbury.seng302.portfolio.service.AuthenticateClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
public class LoginController {

    @Autowired
    private AuthenticateClientService authenticateClientService;

    /**
     * Shows the login page as the default page
     *
     * @return a redirect to the Login page
     */
    @GetMapping("/")
    public String home() {
        return "redirect:./login";
    }

    /**
     * Shows the login page.
     *
     * @param principal authenticated user
     * @return login page or user profile if logged in
     */
    @GetMapping("/login")
    public String login (
            @AuthenticationPrincipal AuthState principal
    ) {
        if (principal != null) {
            String userId = principal.getClaimsList().stream()
                    .filter(claim -> claim.getType().equals("nameid"))
                    .findFirst()
                    .map(ClaimDTO::getValue)
                    .orElse("-1");
            return "redirect:./users/" + userId;
        }
        return "login";
    }

    /**
     * Attempts to authenticate with the Identity Provider via gRPC.
     *
     * This process works in a few stages:
     *  1.  Upon button click, the username and password from the input fields are sent to the IdP
     *  2.  The response is checked. If it is successful, a cookie to the HTTP response so that
     *      the client's browser will store it to be used for future authentication with this service.
     *  3.  Thymeleaf login template is returned if it is unsuccessful with the 'message' given by the identity
     *      provider. This message will be something along the lines of "Bad username or password", etc.
     *      Otherwise, the page is redirected to the user's profile.
     *
     * @param request HTTP request sent to this endpoint
     * @param response HTTP response that will be returned by this endpoint
     * @param username Username of account to log in to IdP with
     * @param password Password associated with username
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return Message generated by IdP about authenticate attempt
     */
    @PostMapping("/login")
    public String login(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(name="username") String username,
            @RequestParam(name="password") String password,
            Model model
    ) {
        AuthenticateResponse loginReply;
        try {
            loginReply = authenticateClientService.authenticate(username, password);
        } catch (StatusRuntimeException e){
            model.addAttribute("loginMessage", "Error connecting to Identity Provider...");
            return "login";
        }
        if (loginReply.getSuccess()) {
            createCookie(request, response, loginReply);
            return "redirect:./users/" + loginReply.getUserId();
        }
        model.addAttribute("loginMessage", loginReply.getMessage());
        return "login";
    }


    public void createCookie(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticateResponse loginReply) {
        var domain = request.getHeader("host");
        CookieUtil.create(
                response,
                "lens-session-token",
                loginReply.getToken(),
                true,
                5 * 60 * 60, // Expires in 5 hours
                domain.startsWith("localhost") ? null : domain
        );
    }

}
