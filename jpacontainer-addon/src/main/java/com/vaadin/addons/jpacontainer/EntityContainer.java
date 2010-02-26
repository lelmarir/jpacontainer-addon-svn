/*
 * JPAContainer
 * Copyright (C) 2010 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer;

import com.vaadin.addons.jpacontainer.filter.StringComparisionFilter;
import com.vaadin.data.Buffered;
import com.vaadin.data.Container;
import com.vaadin.data.Validator.InvalidValueException;

/**
 * A Container for {@link EntityItem}s. The data is provided by a
 * {@link EntityProvider}. Supports sorting, advanced filtering, nested
 * properties and buffering.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface EntityContainer<T> extends Container.Sortable,
		AdvancedFilterable, Container.ItemSetChangeNotifier, Container.Indexed,
		Buffered, Container.Filterable {

	/**
	 * Gets the entity provider that is used for fetching and storing entities.
	 * 
	 * @return the entity provider, or null if this container has not yet been
	 *         properly initialized.
	 */
	public EntityProvider<T> getEntityProvider();

	/**
	 * Sets the entity provider to use for fetching and storing entities.
	 * 
	 * @param entityProvider
	 *            the entity provider to use (must not be null).
	 */
	public void setEntityProvider(EntityProvider<T> entityProvider);

	/**
	 * Gets the class of the entities that are/can be contained in this
	 * container.
	 * 
	 * @return the entity class.
	 */
	public Class<T> getEntityClass();

	/**
	 * Adds the nested property <code>nestedProperty</code> to the set of
	 * properties. An asterisk can be used as a wildcard to indicate all
	 * leaf-properties.
	 * <p>
	 * For example, let's say there is a property named <code>address</code> and
	 * that this property's type in turn has the properties <code>street</code>,
	 * <code>postalCode</code> and <code>city</code>.
	 * <p>
	 * If we want to be able to access the street property directly, we can add
	 * the nested property <code>address.street</code> using this method.
	 * <p>
	 * However, if we want to add all the address properties, we can also use
	 * <code>address.*</code>. This will cause the nested properties
	 * <code>address.street</code>, <code>address.postalCode</code> and
	 * <code>address.city</code> to be added to the set of properties.
	 * <p>
	 * Note, that the wildcard cannot be used in the middle of a chain
	 * of property names. E.g. <code>myprop.*.something</code> is illegal.
	 *
	 * @param nestedProperty
	 *            the nested property to add (must not be null).
	 * @throws UnsupportedOperationException
	 *             if nested properties are not supported by the container.
	 * @throws IllegalArgumentException if <code>nestedProperty</code> is illegal.
	 */
	public void addNestedContainerProperty(String nestedProperty)
			throws UnsupportedOperationException, IllegalArgumentException;

	/**
	 * Adds a new entity to the container. The corresponding {@link EntityItem}
	 * can then be accessed by calling {@link #getItem(java.lang.Object) } using
	 * the entity identifier returned by this method.
	 * <p>
	 * If {@link #isAutoCommit() } is activated, the returned identifier is
	 * always the actual entity ID. Otherwise, the returned identifier may,
	 * depending on the ID generation strategy, be either the actual entity ID
	 * or a temporary ID that is changed to the real ID once the changes have
	 * been committed using {@link #commit() }.
	 * 
	 * @param entity
	 *            the entity to add (must not be null).
	 * @return the identifier of the entity (never null).
	 * @throws UnsupportedOperationException
	 *             if the container does not support adding new entities at all.
	 * @throws IllegalStateException
	 *             if the container supports adding entities, but is currently
	 *             in read only mode.
	 */
	public Object addEntity(T entity) throws UnsupportedOperationException,
			IllegalStateException;

	/**
	 * Creates a new {@link EntityItem} for <code>entity</code> without adding
	 * it to the container. This makes it possible to use the same
	 * {@link com.vaadin.ui.Form} for editing both new entities and existing
	 * entities.
	 * <p>
	 * To add the entity to the container, {@link #addEntity(java.lang.Object) }
	 * should be used.
	 * 
	 * @see EntityItem#getItemId()
	 * @param entity
	 *            the entity for which an item should be created.
	 * @return the entity item (never null).
	 */
	public EntityItem<T> createEntityItem(T entity);

	/**
	 * {@inheritDoc }
	 */
	public EntityItem<T> getItem(Object itemId);

	/**
	 * Returns whether the container is read only or writable.
	 * 
	 * @return true if read only, false if writable.
	 */
	public boolean isReadOnly();

	/**
	 * Changes the read only state of the container, if possible.
	 * 
	 * @param readOnly
	 *            true to make the container read only, false to make it
	 *            writable.
	 * @throws UnsupportedOperationException
	 *             if the read only state cannot be changed.
	 */
	public void setReadOnly(boolean readOnly)
			throws UnsupportedOperationException;

	/**
	 * Alias of {@link Buffered#setWriteThrough(boolean) }.
	 */
	public void setAutoCommit(boolean autoCommit) throws SourceException,
			InvalidValueException;

	/**
	 * Alias of {@link Buffered#isWriteThrough() }.
	 */
	public boolean isAutoCommit();

	/**
	 * {@inheritDoc }
	 * <p>
	 * This method creates a new {@link StringComparisionFilter} for the
	 * specified parameters and applies the filter immediately, regardless of
	 * the state of {@link #isApplyFiltersImmediately() }.
	 * 
	 * @see #addFilter(com.vaadin.addons.jpacontainer.Filter)
	 * @see #applyFilters()
	 */
	public void addContainerFilter(Object propertyId, String filterString,
			boolean ignoreCase, boolean onlyMatchPrefix);

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method does the same as {@link #removeAllFilters() }, but the
	 * container is updated immediately regardless of the state of
	 * {@link #isApplyFiltersImmediately() }.
	 * 
	 * @see #removeAllFilters()
	 */
	public void removeAllContainerFilters();

	/**
	 * {@inheritDoc }
	 * <p>
	 * The container is updated immediately regardless of the state of
	 * {@link #isApplyFiltersImmediately() }.
	 */
	public void removeContainerFilters(Object propertyId);
}
