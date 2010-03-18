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
package com.vaadin.addon.jpacontainer.filter;

import com.vaadin.addon.jpacontainer.Filter;

/**
 * Default implementation of {@link JoinFilter}. The QL generated by this filter
 * assumes that the alias of the joined property is the name of the join propery.
 * For example, if the property to be joined is named <code>skills</code>, then the join should
 * look like this: <code>select Person as obj join obj.skills as skills where ...</code>.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class SimpleJoinFilter extends Conjunction implements JoinFilter {

    private String joinProperty;
    private JoinType joinType;

    protected SimpleJoinFilter(String joinProperty, JoinType joinType,
            Filter... filters) {
        super(filters);
        this.joinProperty = joinProperty;
        this.joinType = joinType;
        // TODO Check that join filters are not nested
    }

    public String getJoinProperty() {
        return joinProperty;
    }

    public JoinType getJoinType() {
        return joinType;
    }

	/**
	 * <strong>This implementation does not use <code>propertyIdPreprocessor</code> at all.</strong>
	 * <p>
	 * {@inheritDoc }
	 */
    @Override
    public String toQLString(final PropertyIdPreprocessor propertyIdPreprocessor) {
        return super.toQLString(new PropertyIdPreprocessor() {

            public String process(Object propertyId) {
                StringBuilder sb = new StringBuilder();
                sb.append(getJoinProperty());
                sb.append(".");
                sb.append(propertyId.toString());
                return sb.toString();
            }
        });
    }
}
