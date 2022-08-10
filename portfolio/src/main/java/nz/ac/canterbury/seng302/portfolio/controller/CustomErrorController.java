package nz.ac.canterbury.seng302.portfolio.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@RestController
public class CustomErrorController implements ErrorController {

    private static final String FEEDBACK = "feedback";

    @PostMapping("/error")
    public ModelAndView handleError(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            // Remove the code number from the string
            String error = HttpStatus.valueOf(statusCode).toString().split(" ")[1];
            //Swap the underscores for spaces and make title case
            error = StringUtils.capitalize(error.replace('_', ' ').toLowerCase());
    
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                modelAndView.addObject(FEEDBACK, "We couldn't find the page you're looking for");
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                modelAndView.addObject(FEEDBACK, "You do not have permission to view this page");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                modelAndView.addObject(FEEDBACK, "The server wasn't able to complete that request");
            } else {
                modelAndView.addObject(FEEDBACK, "An unexpected error occurred");
            }
            modelAndView.addObject("status", status);
            modelAndView.addObject("error", error);
        }
        // Do logging
        return modelAndView;
    }

}
