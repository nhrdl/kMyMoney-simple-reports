package com.niranjanrao.dal.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "payee")
public class Payee extends DataBase {

	@Column
	String matchingenabled;

	@Column
	String matchkey;

	@Column
	String email;

	@Column
	String usingmatchkey;

	@Column
	String id;

	@Column
	String matchignorecase;

	@Column
	String name;

	@Column
	String reference;
}
