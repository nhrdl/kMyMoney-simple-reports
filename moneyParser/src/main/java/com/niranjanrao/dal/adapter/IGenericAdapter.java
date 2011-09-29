package com.niranjanrao.dal.adapter;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;

public interface IGenericAdapter<DomainObject, PK extends Serializable> {

	public DomainObject load(PK id);

	public void update(DomainObject object);

	public void save(DomainObject object);

	public void delete(DomainObject object);

	public void deleteById(PK id);

	public List<DomainObject> getList();

	public Long deleteAll();

	public Long count();

	public List<DomainObject> query(DetachedCriteria ct);

	public int bulkSave(Collection<DomainObject> colln);
}
