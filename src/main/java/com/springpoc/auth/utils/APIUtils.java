/**
 * 
 */
package com.springpoc.auth.utils;

import java.util.Arrays;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author swpraman
 *
 */
public class APIUtils {
    
    public static boolean hasMatchingPath(String path1, String path2) {
        if (path1 == null || path2 == null) {
            return false;
        }
        
        String[] path1Parts = path1.split("/");
        String [] path2Parts = path2.split("/");
        if (path1Parts.length != path2Parts.length) {
            return false;
        }
        for (int i = 0; i < path2Parts.length; i++) {
            if (!StringUtils.equals(path1Parts[i], path2Parts[i])) {
                if ((StringUtils.startsWith(path1Parts[i], "{")
                        && StringUtils.endsWith(path1Parts[i], "}"))
                        || (StringUtils.startsWith(path2Parts[i], "{")
                                && StringUtils.endsWith(path2Parts[i], "}"))) {
                    continue;
                } else {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public static boolean matches(String pathPattern, String path) {
        System.out.println("Path Pattern: " + pathPattern);
        System.out.println("Path : " + path);
        return Pattern.compile(pathPattern.replaceAll("\\{.+\\}", "(.*)")).matcher(path).matches();
    }

    public static boolean isEqualAPI(HttpServletRequest request, RequestMapping mapping) {
        RequestMethod rm = Enum.valueOf(RequestMethod.class, request.getMethod());
        
        // Matching methods
        RequestMethod[] methods = mapping.method();
        if (methods == null || methods.length == 0) {
            methods = new RequestMethod[] {RequestMethod.GET};
        }
        
        if (!Arrays.asList(methods).contains(rm)) {
            return false;
        }
        
        // Matching paths
        String [] paths = mapping.path();
        boolean matched = false;
        for (String apiPath : paths) {
            if (hasMatchingPath(apiPath, request.getRequestURI())) {
                matched = true;
                break;
            }
        }
        
        // TODO Produces and consumes also should be matched here
        
        return matched;
    }

}
