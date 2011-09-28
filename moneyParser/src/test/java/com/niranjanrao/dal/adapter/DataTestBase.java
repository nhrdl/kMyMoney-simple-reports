package com.niranjanrao.dal.adapter;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = { "classpath:spring-config.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class DataTestBase implements ApplicationContextAware {

	protected ApplicationContext applicationContext;
	private AnnotationSessionFactoryBean annotationSessionFactory;

	@Override
	public void setApplicationContext(
			final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;

		this.annotationSessionFactory = (AnnotationSessionFactoryBean) applicationContext
				.getBean("&sessionFactory");
	}

	@Before
	public void setupDatabase() {
		this.annotationSessionFactory.dropDatabaseSchema();
		this.annotationSessionFactory.createDatabaseSchema();

	}
}