/**
 * 
 */
package com.springpoc.auth;

import static com.springpoc.auth.AccessManagerHelper.getMatchingHandler;
import static com.springpoc.auth.AccessManagerHelper.getMatchingPath;
import static com.springpoc.auth.AccessManagerHelper.registerHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.springpoc.auth.annotations.Authenticate;
import com.springpoc.auth.annotations.Authenticator;
import com.springpoc.auth.annotations.Path;
import com.springpoc.auth.annotations.Protected;
import com.springpoc.auth.annotations.ProtectedRoles;
import com.springpoc.auth.exceptions.AuthorizationException;
import com.springpoc.auth.extractors.PathVariableExtractor;
import com.springpoc.auth.extractors.RequestBodyExtractor;
import com.springpoc.auth.model.AuthenticationStatus;
import com.springpoc.auth.model.UserRole;
import com.springpoc.auth.utils.APIUtils;

/**
 * @author swpraman
 *
 */
@Component
public class AccessManager {
    
    private static final Logger logger = LoggerFactory.getLogger(AccessManager.class);
    
    @Autowired
    ApplicationContext context;
    
    @Autowired
    private RequestBodyExtractor bodyExtractor;
    @Autowired
    private PathVariableExtractor pathVarExtractor;
    @Autowired
    private AuthTokenManager tokenManager;
    
    private Object authenticator;
    private Method authenticationMethod = null;
    private RequestMapping authenticationMapping = null;
    
    private Map<String, List<AuthorizationHandler>> handlerMap = new HashMap<>();
    
    @Autowired
    private RequestContainer requestContainer;
    
    public AuthenticationStatus authenticate(HttpServletRequest request) {
        if (authenticator == null) {
            throw new IllegalStateException("Authenticator is not defined");
        }
        
        UserRole userRole = null;
        try {
            // extract parameters
            String apiPath = getMatchingPath(authenticationMapping, request);
            
            List<Object> paramValues = extractParameters(apiPath, authenticationMethod, request, null);
            // call authenticator method
            userRole = (UserRole) authenticationMethod.invoke(authenticator, paramValues.toArray());
        } catch (Exception e) {
            logger.debug("Error while doing authentication");
            e.printStackTrace();
        }
        // Create Auth Status
        AuthenticationStatus authStatus = new AuthenticationStatus();
        if (userRole != null) {
            authStatus.setAuthToken(tokenManager.create(userRole));
        } else {
            authStatus.setError("Inavlid credential!!");
        }
        return authStatus;
    }
    
    public void checkAuthorization(String key, HttpServletRequest request) {
        Boolean authorized = false;
        if (handlerMap.containsKey(key)) {
            AuthorizationHandler authHanlder = getMatchingHandler(handlerMap.get(key), request);
            try {
                if (authHanlder != null) {
                    List<Object> paramValues = extractParameters(authHanlder.apiPath, authHanlder.handlerMethod, request, authHanlder.protectedRoles);
                    logger.debug("Protector: Class {}, Method {}, Param values- {}", authHanlder.handler.getClass(), authHanlder.handlerMethod.getName(), paramValues);
                    authorized = (Boolean)authHanlder.handlerMethod.invoke(authHanlder.handler, paramValues.toArray());
                }
            } catch (Exception e) {
                logger.error("Error while doing authorization", e);
                e.printStackTrace();
            }
        } else {
            logger.debug("Invalid key {}", key);
        }
        
        if (!authorized) {
            throw new AuthorizationException();
        }
    }
    
    public boolean isValidToken(String token) {
        logger.debug("Checking if token is valid");
        UserRole userRole = tokenManager.extract(token);
        if (userRole != null) {
            requestContainer.setUserRole(userRole);
            return true;
        }
        return false;
    }
    
    private List<Object> extractParameters(String apiPath, Method m, HttpServletRequest request, String[] protectedRoles) {
        
        List<Object> values = new LinkedList<>();
        Map<String, String> pathVariables = null;
        for (Parameter p : m.getParameters()) {
            if (p.getType().isInstance(request)) {
                values.add(request);
                
            } else if (p.getType().isAssignableFrom(UserRole.class)) {
                values.add(requestContainer.getUserRole());
                
            } else if (p.isAnnotationPresent(RequestBody.class)) {
                // convert to body object
                Object o = bodyExtractor.extractObject(request, p.getType());
                values.add(o);
                
            } else if (p.isAnnotationPresent(Path.class)) {
                // set Path
                values.add(request.getRequestURI());
                
            } else if (p.isAnnotationPresent(ProtectedRoles.class)) {
                // set protected roles
                values.add(protectedRoles);
                
            } else if (p.isAnnotationPresent(PathVariable.class)) {
                // extract path variables
                PathVariable pathVar = p.getAnnotation(PathVariable.class);
                if (pathVariables == null) {
                    pathVariables = pathVarExtractor.extractPathVariables(apiPath, request);
                }
                
                if (pathVariables.containsKey(pathVar.value())) {
                    values.add(pathVariables.get(pathVar.value()));
                }
                
            } else if (p.isAnnotationPresent(RequestParam.class)) {
                // extract request param
                values.add(request.getParameter(p.getName()));
            }
        }
        return values;
    }
    
    void setAuthenticator(Object authenticator) {
        Class<?> authenticatorClazz = authenticator.getClass();
        if (authenticatorClazz.isAnnotationPresent(Authenticator.class)) {
            for (Method m: authenticatorClazz.getDeclaredMethods()) {
                if (m.isAnnotationPresent(Authenticate.class)
                        && m.isAnnotationPresent(RequestMapping.class)) {
                    this.authenticator = authenticator;
                    this.authenticationMethod = m;
                    this.authenticationMapping = authenticationMethod.getAnnotation(RequestMapping.class);
                    break;
                }
            }
        }
        
        if (this.authenticator == null) {
            throw new IllegalArgumentException("Invalid Authenticator Class: " + authenticatorClazz.getName());
        }
    }
    
    public boolean isAuthenticationAPI(HttpServletRequest request) {
        if (authenticationMethod.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping mapping = authenticationMethod.getAnnotation(RequestMapping.class);
            return APIUtils.isEqualAPI(request, mapping);
        }
        return false;
    }

    public void scanProtectedMethods(Class<? extends Object> coltrollerClazz) {
        String controllerPathPrefix = "";
        if (coltrollerClazz.isAnnotationPresent(RequestMapping.class)) {
            String[] controllerPath = coltrollerClazz.getAnnotation(RequestMapping.class).value();
            controllerPathPrefix = controllerPath.length == 0 ? "" : controllerPath[0];
            if (!StringUtils.startsWith(controllerPathPrefix, "/")) {
                controllerPathPrefix = "/" + controllerPathPrefix;
            }
        }
        logger.debug("Controller {} has path mapping {}", coltrollerClazz.getName(), controllerPathPrefix);
        
        for (Method m: coltrollerClazz.getMethods()) {
            logger.debug("Scanning Method Name: {}", m.getName());
            if (m.isAnnotationPresent(Protected.class)) {
                logger.debug("Protected Method Name: {}", m.getName());
                Protected p = m.getAnnotation(Protected.class);
                String[] roles = p.forRoles();
                Class<?> protector = p.by();
                logger.debug("Protector class {}", protector.getName());
                Object handler =  context.getBean(protector);
                if (handler == null) {
                    throw new IllegalArgumentException("Protector class " +  protector.getName() + " is not a Spring Bean");
                }
                
                String[] paths = AccessManagerHelper.extractPath(m);
                logger.debug("Protected Paths: " + paths);
                List<AuthorizationHandler> handlers = new LinkedList<>();
                if (paths != null && paths.length > 0) {
                    for (String methodPath: paths) {
                        logger.debug("Protected Path: " + methodPath);
                        handlers.add(registerHandler(handler, controllerPathPrefix, methodPath, roles));
                    }
                } else {
                    handlers.add(registerHandler(handler, controllerPathPrefix, "", roles));
                }
                
                handlerMap.put(coltrollerClazz.getName() + "." + m.getName(), handlers);

            }
        }
        
    }
    
    
    
    static class AuthorizationHandler {
        private Object handler = null;
        private Method handlerMethod = null;
        private String[] protectedRoles = null;
        private String apiPath = null;
        /**
         * @return the handler
         */
        Object getHandler() {
            return handler;
        }
        /**
         * @param handler the handler to set
         */
        void setHandler(Object handler) {
            this.handler = handler;
        }
        /**
         * @return the handlerMethod
         */
        Method getHandlerMethod() {
            return handlerMethod;
        }
        /**
         * @param handlerMethod the handlerMethod to set
         */
        void setHandlerMethod(Method handlerMethod) {
            this.handlerMethod = handlerMethod;
        }
        /**
         * @return the protectedRoles
         */
        String[] getProtectedRoles() {
            return protectedRoles;
        }
        /**
         * @param protectedRoles the protectedRoles to set
         */
        void setProtectedRoles(String[] protectedRoles) {
            this.protectedRoles = protectedRoles;
        }
        /**
         * @return the apiPath
         */
        String getApiPath() {
            return apiPath;
        }
        /**
         * @param apiPath the apiPath to set
         */
        void setApiPath(String apiPath) {
            this.apiPath = apiPath;
        }
    }
    
    public static void main(String[] args) {
        String patternString = "abc/def/(.*)/pqr";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher("abc/def/xyz/pqr");
        while(matcher.find()) {
            logger.debug("found: " + matcher.group(1));
        }
        
        String apiPath = "/v1/store/name/{name}";
        String pathVariableNamesPattern = apiPath.replaceAll("\\{.+\\}", "\\\\{(.+)\\\\}");
        logger.debug("pathVariableNames pattern: " + pathVariableNamesPattern);
        Matcher matcher1 = Pattern.compile(pathVariableNamesPattern).matcher(apiPath);
        String pathVariableValuesPattern = apiPath.replaceAll("\\{.+\\}", "(.*)");
        logger.debug("pathVariableValues pattern: " + pathVariableValuesPattern);
        Matcher matcher2 = Pattern.compile(pathVariableValuesPattern).matcher("/v1/store/name/Store2");
        
        for(int i = 1; matcher1.find() && matcher2.find(); i++) {
            logger.debug("found: " + matcher1.group(i) + " -> " + matcher2.group(i));
        }
        
        System.out.println(UUID.randomUUID().toString());
 
    }
    
    
}
