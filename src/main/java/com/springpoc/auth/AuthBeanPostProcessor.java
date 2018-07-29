/**
 * 
 */
package com.springpoc.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import com.springpoc.auth.annotations.Authenticator;



/**
 * @author swpraman
 *
 */
@Component
public class AuthBeanPostProcessor implements BeanPostProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthBeanPostProcessor.class);
    
    @Autowired
    private AccessManager manager;

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        logger.trace("[AuthBeanPostProcessor] Scanning: " + beanName + "[" + bean.getClass().getName() + "]");
        if (bean.getClass().isAnnotationPresent(Authenticator.class)) {
            logger.trace("Setting authenticator: " + beanName + "[" + bean.getClass().getName() + "]");
            manager.setAuthenticator(bean);
        }
        
        if (bean.getClass().isAnnotationPresent(Controller.class)
                || bean.getClass().isAnnotationPresent(RestController.class)) {
            manager.scanProtectedMethods(bean.getClass());
        }
        return bean;
    }

}
