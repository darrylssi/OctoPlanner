package nz.ac.canterbury.seng302.portfolio.controller;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

public class CustomErrorController implements ErrorController {
    
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
    
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("feedback", "We couldn't find the page you're looking for");
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("feedback", "You do not have permission to view this page");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("feedback", "The server wasn't able to complete that request");
            } else {
                model.addAttribute("feedback", "An unexpected error occurred");
            }
            
        }
        // Do logging
        return "error";
    }
}
