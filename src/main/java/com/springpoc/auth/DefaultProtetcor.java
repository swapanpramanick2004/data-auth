/**
 * 
 */
package com.springpoc.auth;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.springpoc.auth.annotations.Authorize;
import com.springpoc.auth.annotations.Path;
import com.springpoc.auth.annotations.ProtectedRoles;
import com.springpoc.auth.model.UserRole;

/**
 * @author swpraman
 *
 */
@Component
public class DefaultProtetcor {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultProtetcor.class);
    
    @Authorize
    public boolean isAuthorized(UserRole userRole, @Path String path, @ProtectedRoles String[] protectedRoles) {
        List<String> roles = Arrays.asList(protectedRoles);
        logger.debug("API is Protected for Roles {} and current UserRole is {}", roles, userRole);
        System.out.println("API is Protected for Roles " + roles + " and current UserRole is " + userRole);
        if (roles.contains(userRole.getRole())) {
            return true;
        }
        return false;
    }

}
