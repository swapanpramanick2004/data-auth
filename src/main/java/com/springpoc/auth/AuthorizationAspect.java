/**
 * 
 */
package com.springpoc.auth;

import java.lang.reflect.Method;

import javax.annotation.PostConstruct;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author swpraman
 *
 */
@Aspect
@Component
public class AuthorizationAspect {
    
    @Autowired
    private AccessManager accessManager;
    
    @Autowired
    private RequestContainer requestContainer;
    
    @PostConstruct
    public void init() {
        System.out.println("Aspect constructed");
    }
    
    @Around("@annotation(com.springpoc.auth.annotations.Protected)")
    public Object checkAuthorization(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("AuthorizationAspect called");
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String key = method.getDeclaringClass().getName() + "." + method.getName();
        accessManager.checkAuthorization(key, requestContainer.getRequest());
        
        return joinPoint.proceed();
    }

}
