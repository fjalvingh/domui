/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.webapp.query;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.webapp.ProgrammerErrorException;
import to.etc.webapp.annotations.GProperty;


/**
 * Represents a <i>selection</i> of data elements from a database. This differs from
 * a QCriteria in that it collects not one persistent class instance per row but multiple
 * items per row, and each item can either be a persistent class or some property or
 * calculated value (max, min, count et al).
 *
 * <p>Even though this type has a generic type parameter representing the base object
 * being queried, the list() method for this object will return a List<Object[]> always.</p>
 *
 * <p>QSelection queries return an array of items for each row, and each element
 * of the array is typed depending on it's source. In addition, QSelection queries
 * expose the ability to handle grouping. QSelection criteria behave as and should
 * be seen as SQL queries in an OO wrapping.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 14, 2009
 */
public class QSelection<T> extends QCriteriaQueryBase<T, QSelection<T>> {
	protected QSelection(@NonNull Class<T> clz) {
		super(clz);
	}

	/**
	 * Create a selection query based on the specified persistent class (public API).
	 * @param <T>	The base type being queried
	 * @param root	The class representing the base type being queried, thanks to the brilliant Java Generics implementation.
	 * @return
	 */
	static public <T> QSelection<T>	create(Class<T> root) {
		return new QSelection<T>(root);
	}

	public void	visit(QNodeVisitor v) throws Exception {
		v.visitSelection(this);
	}


	@NonNull
	public QSelection<T> fetch(@NonNull @GProperty String property) {
		super.fetch(property, QFetchStrategy.EAGER);
		return this;
	}

	@NonNull
	@Override
	public String toString() {
		QQueryRenderer	r	= new QQueryRenderer();
		try {
			visit(r);
		} catch(Exception x) {
			x.printStackTrace();
			return "Invalid query: "+x;
		}
		return r.toString();
	}

	public QSelection<T> testId(@Nullable String testId) {
		setTestId(testId);
		return this;
	}

	/**
	 * Add a simple property selector to the list.
	 */
	protected void addPropertySelection(@NonNull QSelectionFunction f, @NonNull @GProperty String prop, @Nullable String alias) {
		if(prop == null || prop.isEmpty())
			throw new ProgrammerErrorException("The property for a " + f + " selection cannot be null or empty");
		QPropertySelection ps = new QPropertySelection(f, prop);
		addColumn(ps, alias);
	}

	/**
	 * Add a simple property selector to the list.
	 */
	protected <V> void addPropertySelection(@NonNull QSelectionFunction f, @NonNull QField<T, V> property, @Nullable String alias) {
		String prop = property.getName();
		if(prop == null || prop.isEmpty())
			throw new ProgrammerErrorException("The property for a " + f + " selection cannot be null or empty");
		QPropertySelection ps = new QPropertySelection(f, prop);
		addColumn(ps, alias);
	}

	/**
	 * Select a property value from the base property in the result set.
	 * @param property		The property whose literal value is to be selected
	 */
	@NonNull
	public QSelection<T> selectProperty(@NonNull @GProperty final String property) {
		addPropertySelection(QSelectionFunction.PROPERTY, property, null);
		return (QSelection<T>) this;
	}

	@NonNull
	public <V> QSelection<T> selectProperty(@NonNull QField<T, V> property) {
		addPropertySelection(QSelectionFunction.PROPERTY, property, null);
		return (QSelection<T>) this;
	}

	/**
	 * Select a property value from the base property in the result set.
	 *
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 */
	@NonNull
	public QSelection<T> selectProperty(@NonNull @GProperty final String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.PROPERTY, property, alias);
		return (QSelection<T>) this;
	}

	@NonNull
	public <V> QSelection<T> selectProperty(@NonNull QField<T, V> property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.PROPERTY, property, alias);
		return (QSelection<T>) this;
	}

	/**
	 * Select the max of a property in the set. (QSelection<T>) this will cause a group by.
	 *
	 * @param property		The property whose literal value is to be selected
	 */
	@NonNull
	public QSelection<T> max(@NonNull @GProperty String property) {
		addPropertySelection(QSelectionFunction.MAX, property, null);
		return (QSelection<T>) this;
	}

	@NonNull
	public <V> QSelection<T> max(@NonNull QField<T, V> property) {
		addPropertySelection(QSelectionFunction.MAX, property, null);
		return (QSelection<T>) this;
	}

	/**
	 * Select the max of a property in the set. (QSelection<T>) this will cause a group by.
	 *
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 */
	@NonNull
	public QSelection<T> max(@NonNull @GProperty String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.MAX, property, alias);
		return (QSelection<T>) this;
	}

	@NonNull
	public <V> QSelection<T> max(@NonNull QField<T, V> property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.MAX, property, alias);
		return (QSelection<T>) this;
	}

	/**
	 * Select the minimal value of a property in the set. (QSelection<T>) this will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 */
	@NonNull
	public QSelection<T> min(@NonNull @GProperty final String property) {
		addPropertySelection(QSelectionFunction.MIN, property, null);
		return (QSelection<T>) this;
	}

	@NonNull
	public <V> QSelection<T> min(@NonNull QField<T, V> property) {
		addPropertySelection(QSelectionFunction.MIN, property, null);
		return (QSelection<T>) this;
	}

	/**
	 * Select the minimal value of a property in the set. (QSelection<T>) this will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 */
	@NonNull
	public QSelection<T> min(@NonNull @GProperty String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.MIN, property, alias);
		return (QSelection<T>) this;
	}

	@NonNull
	public <V> QSelection<T> min(@NonNull QField<T, V> property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.MIN, property, alias);
		return (QSelection<T>) this;
	}

	/**
	 * Select the average value of a property in the set. (QSelection<T>) this will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 */
	@NonNull
	public QSelection<T> avg(@NonNull @GProperty String property) {
		addPropertySelection(QSelectionFunction.AVG, property, null);
		return (QSelection<T>) this;
	}

	@NonNull
	public <V> QSelection<T> avg(@NonNull @GProperty QField<T, V> property) {
		addPropertySelection(QSelectionFunction.AVG, property, null);
		return (QSelection<T>) this;
	}

	/**
	 * Select the average value of a property in the set. (QSelection<T>) this will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 */
	@NonNull
	public QSelection<T> avg(@NonNull @GProperty final String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.AVG, property, alias);
		return (QSelection<T>) this;
	}

	@NonNull
	public <V> QSelection<T> avg(@NonNull @GProperty QField<T, V> property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.AVG, property, alias);
		return (QSelection<T>) this;
	}

	/**
	 * Select the sum of a property in the set. (QSelection<T>) this will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 */
	@NonNull
	public QSelection<T> sum(@NonNull @GProperty String property) {
		addPropertySelection(QSelectionFunction.SUM, property, null);
		return (QSelection<T>) this;
	}

	@NonNull
	public <V> QSelection<T> sum(@NonNull QField<T, V> property) {
		addPropertySelection(QSelectionFunction.SUM, property, null);
		return (QSelection<T>) this;
	}

	/**
	 * Select the sum of a property in the set. (QSelection<T>) this will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 */
	@NonNull
	public QSelection<T> sum(@NonNull @GProperty String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.SUM, property, alias);
		return (QSelection<T>) this;
	}

	@NonNull
	public <V> QSelection<T> sum(@NonNull QField<T, V> property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.SUM, property, alias);
		return (QSelection<T>) this;
	}

	/**
	 * Select a count over the result set.
	 * @param property		The property whose literal value is to be selected
	 */
	@NonNull
	public QSelection<T> count(@NonNull @GProperty String property) {
		addPropertySelection(QSelectionFunction.COUNT, property, null);
		return (QSelection<T>) this;
	}

	@NonNull
	public <V> QSelection<T> count(@NonNull QField<T, V> property) {
		addPropertySelection(QSelectionFunction.COUNT, property, null);
		return (QSelection<T>) this;
	}

	/**
	 * Select a count over the result set.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 */
	@NonNull
	public QSelection<T> count(@NonNull @GProperty String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.COUNT, property, alias);
		return (QSelection<T>) this;
	}

	@NonNull
	public <V> QSelection<T> count(@NonNull QField<T, V> property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.COUNT, property, alias);
		return (QSelection<T>) this;
	}

	/**
	 * Select a count of the distinct values over the result set.
	 * @param property		The property whose literal value is to be selected
	 */
	@NonNull
	public QSelection<T> countDistinct(@NonNull @GProperty String property) {
		addPropertySelection(QSelectionFunction.COUNT_DISTINCT, property, null);
		return (QSelection<T>) this;
	}

	@NonNull
	public <V> QSelection<T> countDistinct(@NonNull QField<T, V> property) {
		addPropertySelection(QSelectionFunction.COUNT_DISTINCT, property, null);
		return (QSelection<T>) this;
	}

	/**
	 * Select a count of the distinct values over the result set.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 */
	@NonNull
	public QSelection<T> countDistinct(@NonNull @GProperty final String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.COUNT_DISTINCT, property, alias);
		return (QSelection<T>) this;
	}

	@NonNull
	public <V> QSelection<T> countDistinct(@NonNull QField<T, V> property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.COUNT_DISTINCT, property, alias);
		return (QSelection<T>) this;
	}

	/**
	 * Select of the distinct values over the result set.
	 * @param property		The property whose literal value is to be selected
	 */
	@NonNull
	public QSelection<T> distinct(@NonNull @GProperty String property) {
		addPropertySelection(QSelectionFunction.DISTINCT, property, null);
		return (QSelection<T>) this;
	}

	@NonNull
	public <V> QSelection<T> distinct(@NonNull QField<T, V> property) {
		addPropertySelection(QSelectionFunction.DISTINCT, property, null);
		return (QSelection<T>) this;
	}

	/**
	 * Select of the distinct values over the result set.
	 * @param property		The property whose literal value is to be selected
	 */
	@NonNull
	public QSelection<T> distinct(@NonNull @GProperty final String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.DISTINCT, property, alias);
		return (QSelection<T>) this;
	}

	@NonNull
	public <V> QSelection<T> distinct(@NonNull QField<T, V> property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.DISTINCT, property, alias);
		return (QSelection<T>) this;
	}


}
