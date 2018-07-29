/**
 * 
 */
package com.springpoc.auth;

import java.lang.reflect.Method;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.springpoc.auth.AccessManager.AuthorizationHandler;
import com.springpoc.auth.annotations.Authorize;
import com.springpoc.auth.exceptions.ExtractionException;
import com.springpoc.auth.utils.APIUtils;

/**
 * @author swpraman
 *
 */
class AccessManagerHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(AccessManagerHelper.class);
    
    static String getMatchingPath(RequestMapping mapping, HttpServletRequest request) {
        for (String path:mapping.path()) {
            if (APIUtils.matches(path, request.getRequestURI())) {
                return path;
            }
        }
        throw new ExtractionException("Path doesn't match");
    }
    
    static AuthorizationHandler getMatchingHandler(List<AuthorizationHandler> handlers, HttpServletRequest request) {
        for (AuthorizationHandler handler : handlers) {
            if (APIUtils.matches(handler.getApiPath(), request.getRequestURI())) {
                return handler;
            }
        }
        throw new ExtractionException("Path doesn't match");
    }
    
    static AuthorizationHandler registerHandler(Object handler, String prefix, String path, String[] protectedRoles) {
        
        Method defaultAuthorizeMethod = null;
        String apiPath = (prefix + path).replaceAll("\\{.+\\}", "(.*)");
        Method authMethod = null;
        for(Method m: handler.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(Authorize.class)) {
                Authorize a = m.getAnnotation(Authorize.class);
                String protectionPath = a.value().replaceAll("\\{.+\\}", "(.*)");
                if (StringUtils.isEmpty(a.value())) {
                    defaultAuthorizeMethod = m;
                } else if (StringUtils.equals(apiPath, protectionPath)) {
                    authMethod = m;
                    break;
                }
            }
        }
        

        AuthorizationHandler authHandler = new AuthorizationHandler();
        authHandler.setHandler(handler);
        authHandler.setApiPath(prefix + path);
        authHandler.setProtectedRoles(protectedRoles);
        if (authMethod != null) {
            authHandler.setHandlerMethod(authMethod);
        } else if (defaultAuthorizeMethod != null) {
            authHandler.setHandlerMethod(defaultAuthorizeMethod);
        } else {
            throw new IllegalArgumentException("No Authorization method found in Protector class " 
                        +  handler.getClass().getName() + " to match path: " + prefix + path);
        }
        
        return authHandler;
    }
    
    static String[] extractPath(Method m) {
        /*Annotation[] annotations = m.getAnnotations();
        if (annotations != null && annotations.length > 0) {
            for (Annotation a : annotations) {
                logger.debug("Checking annotation: " + a.annotationType().getName());
                if (a.annotationType().isAssignableFrom(RequestMapping.class)) {
                    RequestMapping mapping =  m.getAnnotation(RequestMapping.class);
                    return mapping.path();
                } else if (a.annotationType().isAnnotationPresent(RequestMapping.class)) {
                    
                    Map<String, Object> attributeMap = AnnotationUtils.getAnnotationAttributes(
                            m.getAnnotation(a.annotationType()), false);
                    if (attributeMap.containsKey("path")) {
                        return (String[])attributeMap.get("path");
                    } else {
                        logger.debug("No Path param found in annotation " + a.annotationType().getName());
                    }
                }
            }
        }*/
        
        if (m.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping mapping =  m.getAnnotation(RequestMapping.class);
            logger.debug("RequestMapping for {} : path - {}, value - {}", m.getName(), mapping.path(), mapping.value());
            return mapping.path();
        } else if (m.isAnnotationPresent(GetMapping.class)) {
            GetMapping mapping =  m.getAnnotation(GetMapping.class);
            logger.debug("GetMapping for {} : path - {}, value - {}", m.getName(), mapping.path(), mapping.value());
            return mapping.value();
        } else if (m.isAnnotationPresent(PostMapping.class)) {
            PostMapping mapping =  m.getAnnotation(PostMapping.class);
            logger.debug("PostMapping for {} : path - {}, value - {}", m.getName(), mapping.path(), mapping.value());
            return mapping.value();
        } else if (m.isAnnotationPresent(PutMapping.class)) {
            PutMapping mapping =  m.getAnnotation(PutMapping.class);
            logger.debug("PutMapping for {} : path - {}, value - {}", m.getName(), mapping.path(), mapping.value());
            return mapping.value();
        } else if (m.isAnnotationPresent(DeleteMapping.class)) {
            DeleteMapping mapping =  m.getAnnotation(DeleteMapping.class);
            logger.debug("DeleteMapping for {} : path - {}, value - {}", m.getName(), mapping.path(), mapping.value());
            return mapping.value();
        } 
        
        return null;
    }

}
