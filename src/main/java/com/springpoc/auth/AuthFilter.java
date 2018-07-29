/**
 * 
 */
package com.springpoc.auth;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;

import com.springpoc.auth.model.AuthenticationStatus;

/**
 * @author swpraman
 *
 */
public class AuthFilter implements Filter {
    
    private static final String AUTH_TOKEN = "X-Auth-Token"; 
    private AccessManager manager;
    
    public AuthFilter(ApplicationContext context) {
        manager = context.getBean(AccessManager.class);
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        System.out.println("Auth Filter has been called");
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;
        String token;
        if (manager.isAuthenticationAPI(request)) {
            AuthenticationStatus authStatus = manager.authenticate(request);
            if (authStatus != null && authStatus.getError() == null) {
                response.setHeader(AUTH_TOKEN, authStatus.getAuthToken());
                response.setStatus(HttpStatus.OK.value());
            } else {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.getWriter().println(authStatus.getError());
            }            
        } else if ((token = request.getHeader(AUTH_TOKEN)) != null
                && manager.isValidToken(token)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().println("Not Authorized");
        }

    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
    }

}
