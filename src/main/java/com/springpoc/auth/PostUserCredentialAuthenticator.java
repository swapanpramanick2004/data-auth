/**
 * 
 */
package com.springpoc.auth;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.springpoc.auth.annotations.Authenticate;
import com.springpoc.auth.annotations.Authenticator;
import com.springpoc.auth.model.UserCred;
import com.springpoc.auth.model.UserRole;

/**
 * @author swpraman
 *
 */
@Authenticator
public class PostUserCredentialAuthenticator {

    @Authenticate
    @RequestMapping(path="/auth/login", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE)
    public UserRole authenticate(@RequestBody UserCred userCred) {
        UserRole userRole = null;
        if (StringUtils.equals(userCred.getUserId(), "swapan")
                && StringUtils.equals(userCred.getPassword(), "swapan123")) {
            userRole = new UserRole();
            userRole.setUserId(userCred.getUserId());
            userRole.setRole("GENERAL");
        }
        return userRole;
    }

    
}
