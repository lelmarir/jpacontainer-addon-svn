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
package com.vaadin.addons.jpacontainer.filter;

/**
 * Filter that includes all items for which the filtered property is empty.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class IsEmptyFilter extends AbstractPropertyFilter {

	private static final long serialVersionUID = -421332437947826260L;

	protected IsEmptyFilter(Object propertyId) {
		super(propertyId);
	}

	public String toQLString(PropertyIdPreprocessor propertyIdPreprocessor) {
		return String.format("(%s is empty)", propertyIdPreprocessor
				.process(getPropertyId()));
	}
}
