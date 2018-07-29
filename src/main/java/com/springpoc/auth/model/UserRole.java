/**
 * 
 */
package com.springpoc.auth.model;

/**
 * @author swpraman
 *
 */
public class UserRole {
    
    private String userId;
    private String role;
    
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "UserRole [userId=" + userId + ", role=" + role + "]";
    }

    
}
