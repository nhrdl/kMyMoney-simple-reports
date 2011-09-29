package com.niranjanrao.dal.data;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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

	public Long getUId() {
		return uid;
	}

	public void setUId(final Long id) {
		this.uid = id;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "UID", unique = true, nullable = false)
	Long uid;

	public DataBase() {
	}

	@Override
	public String toString() {
		final StringBuilder bldr = new StringBuilder();
		bldr.append(super.toString() + ":");
		final Class<? extends Object> c = this.getClass();
		for (final Method m : c.getMethods()) {
			if (m.isAnnotationPresent(Column.class)) {
				bldr.append(m.getName());
				bldr.append("=");
				try {
					final Object o = m.invoke(this);
					if (o != null) {
						bldr.append(o);
					} else {
						bldr.append("(null)");
					}

					bldr.append(";");
				} catch (final IllegalArgumentException e) {
					// e.printStackTrace();
				} catch (final IllegalAccessException e) {
					// e.printStackTrace();
				} catch (final InvocationTargetException e) {
					// e.printStackTrace();
				}
			}
		}

		boolean bIsAccessible;
		for (final Field field : getAllFields(getClass())) {
			if (field.isAnnotationPresent(Column.class)) {
				bIsAccessible = field.isAccessible();
				field.setAccessible(true);
				bldr.append(field.getName());
				bldr.append("=");
				try {
					final Object o = field.get(this);
					if (o != null) {
						bldr.append(o);
					} else {
						bldr.append("(null)");
					}

					bldr.append(";");
					field.setAccessible(bIsAccessible);
				} catch (final IllegalArgumentException e) {
					e.printStackTrace();
				} catch (final IllegalAccessException e) {
					e.printStackTrace();
				}
			}

		}
		return bldr.toString();
	}

	private Field[] getAllFields(final Class<?> klass) {
		final List<Field> fields = new ArrayList<Field>();
		fields.addAll(Arrays.asList(klass.getDeclaredFields()));
		if (klass.getSuperclass() != null) {
			fields.addAll(Arrays.asList(getAllFields(klass.getSuperclass())));
		}
		final Field[] fldArray = fields.toArray(new Field[] {});
		Arrays.sort(fldArray, new Comparator<Field>() {

			@Override
			public int compare(final Field o1, final Field o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		return fldArray;
	}

}
