package org.kasource.spring.integration;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;

import javax.annotation.Resource;

import org.kasource.commons.reflection.FieldFilterBuilder;
import org.kasource.commons.util.reflection.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Provides static access to a spring context. Can be used in integration scenarios when Spring is not bootstrapped with
 * the application.
 * 
 * @author rikardwi
 **/
public class SpringContext {

    public static final String ENABLE_SYSTEM_PROPERTY = "spring.context.enable";
    public static final String CONTEXT_LOCATION_SYSTEM_PROPERTY = "spring.context.location";
    private static final String DEFAULT_CONTEXT_LOCATION = "spring/application-context.xml";

    private static final SpringContext INSTANCE = new SpringContext();

    private ApplicationContext applicationContext;
    private String contextLocation;

    private SpringContext() {
        loadContext();
    }

    /**
     * Returns the bean from the Spring Application context of the supplied name and type.
     * 
     * @param <T>
     *            Type of the bean
     * @param beanName
     *            Name of the bean to retrieve.
     * @param ofType
     *            Type of the bean
     * 
     * @return The bean in the spring context found for the supplied name and type.
     * 
     * @throws IllegalStateException
     *             if not enabled
     **/
    public static <T> T getBean(String beanName, Class<T> ofType) throws IllegalStateException {
        checkEnabled();
        return INSTANCE.getBeanOfType(beanName, ofType);
    }

    /**
     * Inject dependencies of Fields on the target object, which is annotated with @Resource or @Autowired.
     * 
     * @param target
     *            Object to inject dependencies to.
     * 
     * @throws IllegalStateException
     *             if not enabled or dependencies could not be injected or error occurs while trying to inject values.
     **/
    public static void injectDependencies(Object target) throws IllegalStateException {
        checkEnabled();
        INSTANCE.injectDependenciesForObject(target);

    }

    /**
     * Returns true of the context loading is enabled, else false.
     * 
     * @return true of the context loading is enabled, else false.
     **/
    private static boolean isEnabled() {
        String enable = System.getProperty(ENABLE_SYSTEM_PROPERTY);
        if (enable != null && enable.toLowerCase().equals("true")) {
            return true;
        }
        return false;
    }

    /**
     * Loads a spring application context from classpath.
     **/
    private void loadContext() {

        if (isEnabled()) {
            System.out.println("loads app context ");
            contextLocation = System.getProperty(CONTEXT_LOCATION_SYSTEM_PROPERTY, DEFAULT_CONTEXT_LOCATION);
            applicationContext = new ClassPathXmlApplicationContext(contextLocation);
        }

    }

    /**
     * Throws exception if not enabled.
     * 
     * @throws IllegalStateException
     *             if the context loading is not enabled.
     **/
    private static void checkEnabled() throws IllegalStateException {
        if (!isEnabled()) {
            throw new IllegalStateException("The Spring Context is disabled, set system property "
                        + ENABLE_SYSTEM_PROPERTY + "=true to enable it.");
        }
    }

    /**
     * Returns the bean from the Spring Application context of the supplied name and type.
     * 
     * @param <T>
     *            Type of the bean
     * @param beanName
     *            Name of the bean to retrieve.
     * @param ofType
     *            Type of the bean
     * 
     * @return The bean in the spring context found for the supplied name and type.
     **/
    private <T> T getBeanOfType(String beanName, Class<T> ofType) {

        return applicationContext.getBean(beanName, ofType);
    }

    /**
     * Inject dependencies of Fields on the target object, which is annotated with @Resource or @Autowired.
     * 
     * @param target
     *            Object to inject dependencies to.
     * 
     * @throws IllegalStateException
     *             if dependencies could not be injected or error occurs while trying to inject values.
     **/
    private void injectDependenciesForObject(Object target) throws IllegalStateException {

        Set<Field> annotatedFields = FieldUtils.getFields(target.getClass(),
                    new FieldFilterBuilder().annotated(Resource.class).or().annotated(Autowired.class).build());
        for (Field field : annotatedFields) {
            injectFieldValue(target, field);
        }

    }

    /**
     * Inject dependencies to the supplied field that is annotated with @Resource or @Autowired.
     * 
     * @param target
     *            Object to inject dependencies to.
     * @param field
     *            The field to inject dependency into.
     * 
     * @throws IllegalStateException
     *             if dependencies could not be injected or error occurs while trying to inject values.
     **/
    private void injectFieldValue(Object target, Field field) throws IllegalStateException {
        try {
            if (field.isAnnotationPresent(Autowired.class)) {
                injectByAutowired(target, field);
            } else {
                injectByResource(target, field);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not inject value into " + field, e);
        }
    }

    /**
     * Inject value to field by @Resource annotation.
     * 
     * @param target
     *            Object to inject dependencies to.
     * @param field
     *            The field to inject dependency into.
     * 
     * @throws IllegalAccessException
     *             If setting the value the field causes this exception.
     **/
    private void injectByResource(Object target, Field field) throws IllegalAccessException {
        Resource resource = field.getAnnotation(Resource.class);
        if (resource.name().length() > 0) {
            injectByName(target, field, resource.name());
        } else {
            injectByType(target, field);
        }
    }

    /**
     * Inject value to field by @Autowired annotation.
     * 
     * @param target
     *            Object to inject dependencies to.
     * @param field
     *            The field to inject dependency into.
     * 
     * @throws IllegalAccessException
     *             If setting the value the field causes this exception.
     **/
    private void injectByAutowired(Object target, Field field) throws IllegalAccessException {
        Qualifier qualifier = field.getAnnotation(Qualifier.class);
        if (qualifier != null && qualifier.value().length() > 0) {
            injectByName(target, field, qualifier.value());
        } else {
            injectByType(target, field);
        }
    }

    /**
     * Inject dependency into the field, looks up beans in the application context of the fields type.
     * 
     * @param target
     *            Target object to inject dependency to.
     * @param field
     *            Field to inject value to.
     * 
     * @throws IllegalArgumentException
     *             If setting the value the field causes this exception.
     * @throws IllegalAccessException
     *             If setting the value the field causes this exception.
     * @throws IllegalStateException
     *             If no bean of the fields type could be found or if more than one bean of the fields type is found.
     **/
    private void injectByType(Object target, Field field) throws IllegalArgumentException, IllegalAccessException,
                IllegalStateException {
        String[] beans = applicationContext.getBeanNamesForType(field.getType());
        if (beans.length == 1) {
            injectByName(target, field, beans[0]);
        } else if (beans.length == 0) {
            throw new IllegalStateException("No beans found of type " + field.getType() + " in context "
                        + contextLocation);
        } else {
            throw new IllegalStateException("More than one bean found of type " + field.getType() + " in context "
                        + contextLocation + " possible candidates are: " + Arrays.asList(beans));
        }
    }

    /**
     * Inject dependency into the field, by looking up bean with the supplied name in the spring application context.
     * 
     * @param target
     *            Target object to inject dependency to.
     * @param field
     *            Field to inject value to.
     * @param name
     *            Name of the bean to inject into field.
     * 
     * @throws IllegalArgumentException
     *             If setting the value the field causes this exception.
     * @throws IllegalAccessException
     *             If setting the value the field causes this exception.
     */
    private void injectByName(Object target, Field field, String name) throws IllegalArgumentException,
                IllegalAccessException {
        Object object = applicationContext.getBean(name);
        field.setAccessible(true);
        field.set(target, object);
    }

}
