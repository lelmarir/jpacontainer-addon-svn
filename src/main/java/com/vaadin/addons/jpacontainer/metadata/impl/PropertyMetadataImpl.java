/*
 * JPAContainer
 * Copyright (C) 2009 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer.metadata.impl;

import com.vaadin.addons.jpacontainer.metadata.PropertyMetadata;
import com.vaadin.addons.jpacontainer.metadata.PropertyMetadata.AccessType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Default implementation of {@link PropertyMetadata}.
 * 
 * @author Petter Holmström (IT Mill)
 */
public final class PropertyMetadataImpl implements PropertyMetadata {

    private final String name;

    private final Class<?> type;

    private final boolean embedded;

    private final boolean reference;

    private final boolean collection;

    private final AccessType accessType;

    final Field field;

    final Method getter;

    final Method setter;

    PropertyMetadataImpl(String name, Class<?> type, boolean embedded,
            boolean reference, boolean collection, Field field, Method getter,
            Method setter) {
        assert name != null : "name must not be null";
        assert type != null : "type must not be null";
        assert (field != null && getter == null && setter == null) || (field
                == null && getter != null && setter != null) :
                "field or getter/setter must be specified";

        this.name = name;
        this.type = type;
        this.embedded = embedded;
        this.reference = reference;
        this.collection = collection;
        this.field = field;
        this.getter = getter;
        this.setter = setter;
        if (field != null) {
            this.accessType = AccessType.FIELD;
        } else {
            this.accessType = AccessType.METHOD;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public boolean isEmbedded() {
        return embedded;
    }

    @Override
    public boolean isReference() {
        return reference;
    }

    @Override
    public boolean isCollection() {
        return collection;
    }

    @Override
    public AccessType getAccessType() {
        return accessType;
    }

    @Override
    public Annotation[] getAnnotations() {
        if (field != null) {
            return field.getAnnotations();
        } else {
            return getter.getAnnotations();
        }
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        for (Annotation a : getAnnotations()) {
            if (a.annotationType() == annotationClass) {
                return annotationClass.cast(a);
            }
        }
        return null;
    }
}