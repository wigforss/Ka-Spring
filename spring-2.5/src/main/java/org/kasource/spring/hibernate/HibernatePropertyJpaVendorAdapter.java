package org.kasource.spring.hibernate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;

/**
 * A HibernateJpaVendorAdapter which reads hibernate properties from a separate file at runtime. Using this class
 * no hibernate properties needs to "hardcoded" into the persistance.xml.
 * <p> 
 * {@code
 * <bean id="jpaAdapter"
		class="org.kasource.spring.hibernate.HibernatePropertyJpaVendorAdapter">
		<property name="hibernatePropertiesLocation" value="file:${APP_HOME}/conf/hibernate.properties"/>
	</bean>
 * }
 * @author rikardwi
 **/
public class HibernatePropertyJpaVendorAdapter extends HibernateJpaVendorAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(HibernatePropertyJpaVendorAdapter.class);
	private Resource hibernatePropertiesLocation;

	/**
	 * Set the location of the hibernate property file to use.
	 * 
	 * @param hibernatePropertiesLocation location, classpath or file.
	 **/
	@Required
	public void setHibernatePropertiesLocation(Resource hibernatePropertiesLocation) {
		this.hibernatePropertiesLocation = hibernatePropertiesLocation;
	}
	
	/**
	 * Loads the hibernate property and returns the JPA Property Map with
	 * the loaded properties.
	 * 
	 *  @return JPA Property Map containing the loaded properties.
	 **/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, Object> getJpaPropertyMap() {
		Map<String, Object> propertyMap = super.getJpaPropertyMap();
		InputStream is = null;
		Properties hibernateProperties = new Properties();
		try {
			is = hibernatePropertiesLocation.getInputStream();
			hibernateProperties.load(is);
			propertyMap.putAll((Map) hibernateProperties);
		} catch (IOException e) {
			throw new IllegalStateException(
					"Could not load hibernate properties from " + hibernatePropertiesLocation);
		} finally {
			closeResourceStream(is, hibernatePropertiesLocation);
		}
		return propertyMap;
	}
	
	/**
	 * Close the resource stream.
	 * 
	 * @param resourceStream Resource stream to close.
	 * @param resource       The resource.
	 **/
	private void closeResourceStream(InputStream resourceStream, Resource resource) {
		if (resourceStream != null) {
			try {
				resourceStream.close();
			} catch (IOException e) {
				LOG.warn("Could not close inputstream for " + resource);
			}
		}
	}
}
