/**
 * 
 */
package com.springpoc.auth.model;

/**
 * @author swpraman
 *
 */
public class AuthenticationStatus {
    
    private String authToken;
    private String error;
    
    public String getAuthToken() {
        return authToken;
    }
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }

}
