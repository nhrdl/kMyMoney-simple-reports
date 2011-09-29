package com.niranjanrao.dal.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "institution")
public class Institution extends DataBase {

	@Column
	String manager;
	@Column
	String id;
	@Column
	String name;
	@Column
	String sortcode;

	public String getSortcode() {
		return sortcode;
	}

	public void setSortcode(final String sortcode) {
		this.sortcode = sortcode;
	}

	public String getManager() {
		return manager;
	}

	public void setManager(final String manager) {
		this.manager = manager;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}
}
