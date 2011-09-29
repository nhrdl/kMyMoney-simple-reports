package com.niranjanrao.dal.adapter;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
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
			final ParameterizedType thisType = (ParameterizedType) this
					.getClass().getGenericSuperclass();
			this.domainClass = (Class<T>) thisType.getActualTypeArguments()[0];
		}
		return this.domainClass;
	}

	@Autowired
	public void init(final SessionFactory factory) {
		this.setSessionFactory(factory);
	}

	@Override
	public List<T> query(final DetachedCriteria ct) {
		return this.getHibernateTemplate().findByCriteria(ct);
	}

	/**
	 * Method to return the class of the domain object
	 */

	@Override
	public T load(final PK id) {

		return this.getHibernateTemplate().load(this.domainClass, id);
	}

	@Override
	public void update(final T t) {
		this.getHibernateTemplate().update(t);
	}

	@Override
	public void save(final T t) {
		this.getHibernateTemplate().saveOrUpdate(t);
	}

	@Override
	public void delete(final T t) {
		this.getHibernateTemplate().delete(t);
	}

	@Override
	public List<T> getList() {
		return this.getHibernateTemplate().find(
				"from " + this.domainClass.getName() + " x");
	}

	@Override
	public void deleteById(final PK id) {
		final Object obj = this.load(id);
		this.getHibernateTemplate().delete(obj);
	}

	@Override
	public Long deleteAll() {

		final Long val = this.getHibernateTemplate().execute(
				new HibernateCallback<Long>() {
					@Override
					public Long doInHibernate(final Session session)
							throws HibernateException {
						final String hqlDelete = "delete "
								+ GenericAdapter.this.domainClass.getName();
						final long count = session.createQuery(hqlDelete)
								.executeUpdate();
						return Long.valueOf(count);
					}

				});

		return val;
	}

	@Override
	public Long count() {
		@SuppressWarnings("rawtypes")
		final List list = this.getHibernateTemplate().find(
				"select count(*) from " + this.domainClass.getName() + " x");
		final Long count = (Long) list.get(0);
		return count;
	}

	@Override
	public int bulkSave(final Collection<T> colln) {
		for (final T t : colln) {
			save(t);
		}
		return colln.size();
	}
}
