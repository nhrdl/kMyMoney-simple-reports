package com.niranjanrao.dal.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;

@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@MappedSuperclass
public abstract class DataBase {
	Boolean isActive;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID", unique = true, nullable = false)
	Long id;

	public DataBase() {
	}

	@Override
	public String toString() {
		StringBuilder bldr = new StringBuilder();
		bldr.append(super.toString());
		Class<? extends Object> c = this.getClass();
		for (Method m : c.getMethods()) {
			if (m.isAnnotationPresent(Column.class)) {
				bldr.append(m.getName());
				bldr.append("=");
				try {
					Object o = m.invoke(this);
					if (o != null) {
						bldr.append(o);
					} else {
						bldr.append("(null)");
					}

					bldr.append(";");
				} catch (IllegalArgumentException e) {
					// e.printStackTrace();
				} catch (IllegalAccessException e) {
					// e.printStackTrace();
				} catch (InvocationTargetException e) {
					// e.printStackTrace();
				}
			}
		}
		return bldr.toString();
	}
}
