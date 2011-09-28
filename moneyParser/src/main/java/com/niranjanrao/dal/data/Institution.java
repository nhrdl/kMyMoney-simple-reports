package com.niranjanrao.dal.data;

import javax.persistence.Column;

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
	public void setSortcode(String sortcode) {
		this.sortcode = sortcode;
	}
	public String getManager() {
		return manager;
	}
	public void setManager(String manager) {
		this.manager = manager;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
