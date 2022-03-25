package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import io.grpc.Status;
import io.grpc.StatusException;

/**
 * TODO [Andrew]: This is a testing controller, delete it once integrated
 */
@Controller
public class DebugAddRoleController {

    @Autowired
    private UserAccountClientService userAccountClientService;

    @GetMapping("/andys-testing-space/addrole/{id}/{role}")
    @ResponseBody
    private String addRole(
        @PathVariable("id") int id,
        @PathVariable("role") UserRole role
    ) {
        try {
            var response = userAccountClientService.addRoleToUser(id, role);
            return String.valueOf(response);
        } catch (StatusException e) {
            if (e.getStatus().getCode() == Status.NOT_FOUND.getCode()) {
                return "Error: Invalid ID";
            } else {
                throw new RuntimeException(e);
            }
        }
    }


    
}
