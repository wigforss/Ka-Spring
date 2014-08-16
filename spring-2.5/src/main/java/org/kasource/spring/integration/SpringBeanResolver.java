package org.kasource.spring.integration;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.kasource.di.bean.BeanNotFoundException;
import org.kasource.di.bean.BeanResolver;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Repository;

@Repository
public class SpringBeanResolver implements BeanResolver, ApplicationContextAware {
    private ApplicationContext applicationContext;
    
    @Override
    public <T> T getBean(String beanName, Class<T> ofType) throws BeanNotFoundException {
       
        try {
            return (T) applicationContext.getBean(beanName, ofType);
        } catch (BeansException e) {
            throw new BeanNotFoundException("Could not find any bean named: " + beanName + " of class: " + ofType, e);
        }
    }


    @Override
    public <T> Set<T> getBeans(Class<T> ofType) {
        Set<T> beansFound = new HashSet<T>();
        beansFound.addAll(applicationContext.getBeansOfType(ofType).values());
        return beansFound;
    }
    

    @SuppressWarnings("unchecked")
    @Override
    public <T> Set<T> getBeansByQualifier(Class<T> ofType, Class<? extends Annotation>... qualifiers) {
        Set<T> beansFound = new HashSet<T>();
        if (qualifiers.length == 0) {
            for ( Class<? extends Annotation> qualifier : qualifiers) {
                Map<String, T> beans =  applicationContext.getBeansOfType(ofType);
                for (Object bean : beans.values()) {
                    for (Class<? extends Annotation> qualifierClass : qualifiers) {
                        if (bean.getClass().isAnnotationPresent(qualifierClass)) {
                            beansFound.add((T) bean);
                        }
                    }
                }
            }
        } else {
           Map<String, T> beans =  applicationContext.getBeansOfType(ofType);
           beansFound.addAll(beans.values());
        }   
        return beansFound;
    }



    @Override
    public <T> Set<T> getBeansByQualifier(Class<T> ofType, Annotation... qualifiers) {
        Set<T> beansFound = new HashSet<T>();
        Map<String, T> beans =  applicationContext.getBeansOfType(ofType);
        if (qualifiers.length == 0) {
            beansFound.addAll(beans.values());
        }
        for (T bean : beans.values()) {
            for (Annotation annotation : qualifiers) {
                Annotation found = bean.getClass().getAnnotation(annotation.getClass());
                if (annotation.equals(found)) {
                    beansFound.add(bean);
                }
            }
        }
        return beansFound;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }



   



    

}
