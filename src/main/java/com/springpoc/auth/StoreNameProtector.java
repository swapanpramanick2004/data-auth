/**
 * 
 */
package com.springpoc.auth;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import com.springpoc.auth.annotations.Authorize;
import com.springpoc.auth.model.UserRole;

/**
 * @author swpraman
 *
 */
@Component
public class StoreNameProtector {

    @Authorize
    public boolean authorize(UserRole userRole, @PathVariable("name") String storeName) {
        if (StringUtils.equals(storeName, "Store1")
                && StringUtils.equals(userRole.getUserId(), "swapan")) {
            return true;
        }
        return false;
    }
}
