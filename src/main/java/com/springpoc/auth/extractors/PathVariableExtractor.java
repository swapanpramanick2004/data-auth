/**
 * 
 */
package com.springpoc.auth.extractors;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.springpoc.auth.exceptions.ExtractionException;

/**
 * @author swpraman
 *
 */
@Component
public class PathVariableExtractor {
    
    private static final Logger logger = LoggerFactory.getLogger(PathVariableExtractor.class);
    
    public Map<String, String> extractPathVariables(String apiPath, HttpServletRequest request) {
        logger.debug("Extracting path variables for apiPath {} and RequestURI {}", apiPath, request.getRequestURI());
        Map<String, String> pathVariables = new HashMap<>();
        String pathVariableNamesPattern = apiPath.replaceAll("\\{.+\\}", "\\\\{(.+)\\\\}");
        Matcher matcher1 = Pattern.compile(pathVariableNamesPattern).matcher(apiPath);
        String pathVariableValuesPattern = apiPath.replaceAll("\\{.+\\}", "(.*)");
        Matcher matcher2 = Pattern.compile(pathVariableValuesPattern).matcher(request.getRequestURI());
        
        for(int i = 1; matcher1.find() && matcher2.find(); i++) {
            logger.debug("Path Variable: {} -> {}", matcher1.group(i), matcher2.group(i));
            pathVariables.put(matcher1.group(i), matcher2.group(i));
        }
        if (pathVariables.isEmpty()) {
            throw new ExtractionException("Path variable could not be extracted");
        }
        return pathVariables;
    }
    

}
