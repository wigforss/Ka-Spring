package org.kasource.spring.integration;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

public class SpringContextTest {

    @Test
    public void getBean() {
        System.setProperty(SpringContext.ENABLE_SYSTEM_PROPERTY, "true");
        assertEquals("TestString", SpringContext.getBean("testBean", String.class));
        
    }
    
    @Test
    public void injectDependencies() {
        MyClass target = new MyClass();
        System.setProperty(SpringContext.ENABLE_SYSTEM_PROPERTY, "true");
        SpringContext.injectDependencies(target);
        assertEquals("TestString", target.getMyString());
        
    }
    
    
    private static class MyClass {
        
        @Resource
        private String myString;
        
        public String getMyString() {
            return myString;
        }
    }
    
}
