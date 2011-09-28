package com.niranjanrao.dal.adapter;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("unchecked")
@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
public abstract class GenericAdapter<T, PK extends Serializable> extends
		HibernateDaoSupport implements IGenericAdapter<T, PK> {

	protected Class<T> domainClass = this.getDomainClass();

	protected Class<T> getDomainClass() {
		if (this.domainClass == null) {
			ParameterizedType thisType = (ParameterizedType) this.getClass()
					.getGenericSuperclass();
			this.domainClass = (Class<T>) thisType.getActualTypeArguments()[0];
		}
		return this.domainClass;
	}

	@Autowired
	public void init(SessionFactory factory) {
		this.setSessionFactory(factory);
	}

	@Override
	public List<T> query(DetachedCriteria ct) {
		return this.getHibernateTemplate().findByCriteria(ct);
	}

	/**
	 * Method to return the class of the domain object
	 */

	@Override
	public T load(PK id) {

		return this.getHibernateTemplate().load(this.domainClass, id);
	}

	@Override
	public void update(T t) {
		this.getHibernateTemplate().update(t);
	}

	@Override
	public void save(T t) {
		this.getHibernateTemplate().saveOrUpdate(t);
	}

	@Override
	public void delete(T t) {
		this.getHibernateTemplate().delete(t);
	}

	@Override
	public List<T> getList() {
		return (this.getHibernateTemplate().find("from "
				+ this.domainClass.getName() + " x"));
	}

	@Override
	public void deleteById(PK id) {
		Object obj = this.load(id);
		this.getHibernateTemplate().delete(obj);
	}

	@Override
	public Long deleteAll() {

		Long val = this.getHibernateTemplate().execute(
				new HibernateCallback<Long>() {
					@Override
					public Long doInHibernate(Session session)
							throws HibernateException {
						String hqlDelete = "delete "
								+ GenericAdapter.this.domainClass.getName();
						long count = session.createQuery(hqlDelete)
								.executeUpdate();
						return Long.valueOf(count);
					}

				});

		return val;
	}

	@Override
	public Long count() {
		@SuppressWarnings("rawtypes")
		List list = this.getHibernateTemplate().find(
				"select count(*) from " + this.domainClass.getName() + " x");
		Long count = (Long) list.get(0);
		return count;
	}

}
