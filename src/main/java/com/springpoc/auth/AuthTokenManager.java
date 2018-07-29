/**
 * 
 */
package com.springpoc.auth;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.springpoc.auth.model.UserRole;

/**
 * @author swpraman
 *
 */
@Component
public class AuthTokenManager {
    
    @Value("${io.zoko.auth.encrypt.secret}")
    private String secret;
    
    @Value("${io.zoko.auth.encrypt.initVector}")
    private String initVector;

    public String create(UserRole userRole) {
        if (userRole == null)
            return null;
        
        try {
            return encrypt(secret, initVector, userRole.getUserId() + "|" + userRole.getRole());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public UserRole extract(String token) {
        try {
            String decrypted = decrypt(secret, initVector, token);
            if (decrypted != null) {
                String[] vals = decrypted.split("\\|");
                UserRole userRole = new UserRole();
                userRole.setUserId(vals[0]);
                userRole.setRole(vals[1]);
                return userRole;
            }
        } catch (Exception e) {
            System.out.println("Error extracting token " + token);
            e.printStackTrace();
        }
        return null;
    }
    
    private String encrypt(String key, String initVector, String value) throws Exception {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            System.out.println("encrypted string: "
                    + Base64.encodeBase64String(encrypted));

            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {
            throw ex;
        }

    }

    private String decrypt(String key, String initVector, String encrypted) throws Exception {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

            return new String(original);
        } catch (Exception ex) {
            throw ex;
        }

    }
    
    

}
