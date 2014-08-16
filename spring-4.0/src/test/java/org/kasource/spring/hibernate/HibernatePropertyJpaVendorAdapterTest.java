package org.kasource.spring.hibernate;


import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

import org.easymock.classextension.EasyMock;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.Resource;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.Mock;
import org.unitils.inject.annotation.TestedObject;

@Ignore
@RunWith(UnitilsJUnit4TestClassRunner.class)
public class HibernatePropertyJpaVendorAdapterTest {

	@Mock
	private Resource hibernatePropertiesLocation;
	
	@Mock
	private InputStream inputStream;
	
	@TestedObject
	private HibernatePropertyJpaVendorAdapter jpaAdapter;
	
	@Test
	public void getJpaPropertyMapTest() throws IOException {
		Properties properties = new Properties();
		properties.setProperty("test.property1", "testValue1");
		properties.setProperty("test.property2", "testValue2");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		properties.store(out, "");
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		expect(hibernatePropertiesLocation.getInputStream()).andReturn(in);
		EasyMockUnitils.replay();
		jpaAdapter.setHibernatePropertiesLocation(hibernatePropertiesLocation);
		 Map<String, Object> jpaProperties = jpaAdapter.getJpaPropertyMap();
		 
		 assertEquals("testValue1", jpaProperties.get("test.property1"));
		 assertEquals("testValue2", jpaProperties.get("test.property2"));
	}
	
	@Test(expected = IllegalStateException.class)
	public void getJpaPropertyMapTestFail() throws IOException {
		
		expect(hibernatePropertiesLocation.getInputStream()).andReturn(inputStream);
		expect(inputStream.read(EasyMock.isA(byte[].class))).andThrow(new IOException("test"));
		inputStream.close();
		expectLastCall();
		EasyMockUnitils.replay();
		jpaAdapter.setHibernatePropertiesLocation(hibernatePropertiesLocation);
		jpaAdapter.getJpaPropertyMap();
	}
	
	@Test(expected = IllegalStateException.class)
	public void getJpaPropertyMapTestFailClosedFailed() throws IOException {
		
		expect(hibernatePropertiesLocation.getInputStream()).andReturn(inputStream);
		expect(inputStream.read(EasyMock.isA(byte[].class))).andThrow(new IOException("test"));
		inputStream.close();
		expectLastCall().andThrow(new IOException("test"));
		EasyMockUnitils.replay();
		jpaAdapter.setHibernatePropertiesLocation(hibernatePropertiesLocation);
		jpaAdapter.getJpaPropertyMap();
	}
	
	/**
	 * Test of null.
	 * @throws Exception if something fails in introspection.
	 */
	@Test
	public final void testClosing() throws Exception {
	    Method method =  HibernatePropertyJpaVendorAdapter.class.getDeclaredMethod("closeResourceStream", InputStream.class, Resource.class);
	    method.setAccessible(true);
	    method.invoke(jpaAdapter, null, null);
	    assertTrue(true);
	}
	
}
