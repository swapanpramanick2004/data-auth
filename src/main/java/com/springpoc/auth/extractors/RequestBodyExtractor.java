/**
 * 
 */
package com.springpoc.auth.extractors;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springpoc.auth.exceptions.ExtractionException;

/**
 * @author swpraman
 *
 */
@Component
public class RequestBodyExtractor {
    
    ObjectMapper mapper = new ObjectMapper();
    
    public <T> T extractObject(HttpServletRequest request, Class<T> clazz) {
        try {
            return mapper.readValue(request.getInputStream(), clazz);
        } catch (IOException e) {
            throw new ExtractionException("Could not convert body of the request to " + clazz, e);
        }
    }

}
