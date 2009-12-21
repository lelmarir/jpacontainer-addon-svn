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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer.util;

import com.vaadin.addons.jpacontainer.JPAContainer;
import com.vaadin.addons.jpacontainer.metadata.ClassMetadata;
import com.vaadin.addons.jpacontainer.metadata.PersistentPropertyMetadata;
import com.vaadin.addons.jpacontainer.metadata.PropertyMetadata;
import java.beans.Introspector;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Helper class to make it easier to work with nested properties. Intended
 * to be used by {@link JPAContainer}. This class is not part of the public API
 * and hence should not be used directly by client applications.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public final class PropertyList<T> implements Serializable {

    private ClassMetadata<T> metadata;
    private Set<String> propertyNames = new HashSet<String>();
    private Set<String> persistentPropertyNames = new HashSet<String>();

    /**
     * Creates a new <code>PropertyList</code> for the specified metadata.
     * Initially, all the properties of <code>metadata</code> will be added
     * to the list.
     *
     * @param metadata the class metadata (must not be null).
     */
    public PropertyList(ClassMetadata<T> metadata) {
        assert metadata != null : "metadata must not be null";
        this.metadata = metadata;
        for (PropertyMetadata pm : metadata.getProperties()) {
            propertyNames.add(pm.getName());
            if (pm instanceof PersistentPropertyMetadata) {
                persistentPropertyNames.add(pm.getName());
            }
        }
    }

    /**
     * Adds the nested property <code>propertyName</code> to the set of properties.
     * An asterisk can be used as a wildcard to indicate all leaf-properties.
     * <p>
     * For example, let's say there is a property named <code>address</code> and that this property's type
     * in turn has the properties <code>street</code>, <code>postalCode</code> and <code>city</code>.
     * <p>
     * If we want to be able to access the street property directly, we can add
     * the nested property <code>address.street</code> using this method. The method will
     * figure out whether the nested property is persistent (can be used in queries) or transient
     * (can only be used to display data).
     * <p>
     * However, if we want to add all the address properties, we can also use <code>address.*</code>.
     * This will cause the nested properties <code>address.street</code>, <code>address.postalCode</code>
     * and <code>address.city</code> to be added to the set of properties.
     *
     * @param propertyName the nested property to add (must not be null).
     * @throws IllegalArgumentException if <code>propertyName</code> was invalid.
     */
    public void addNestedProperty(String propertyName) throws
            IllegalArgumentException {
        assert propertyName != null : "propertyName must not be null";

        if (propertyName.indexOf('.') == -1) {
            throw new IllegalArgumentException(propertyName + " is not nested");
        }

        if (propertyName.endsWith("*")) {
            // We add a whole bunch of properties
            String parentPropertyName = propertyName.substring(0, propertyName.
                    length() - 2);
            NestedProperty parentProperty = getNestedProperty(parentPropertyName);
            if (parentProperty.getMetadata() != null) {
                // The parent property is persistent and contains metadataq
                for (PropertyMetadata pm : parentProperty.getMetadata().
                        getProperties()) {
                    String newName = parentPropertyName + "." + pm.getName();
                    if (pm instanceof PersistentPropertyMetadata) {
                        persistentPropertyNames.add(newName);
                    }
                    propertyNames.add(newName);
                }
            } else {
                // The parent property is transient or is a simple property that does not contain any nestable properties
                for (Method m : parentProperty.getType().getMethods()) {
                    if (m.getName().startsWith("get") && !Modifier.isStatic(
                            m.getModifiers()) && m.getReturnType() != Void.TYPE && m.
                            getDeclaringClass() != Object.class) {
                        String newName = parentPropertyName + "." + Introspector.
                                decapitalize(m.getName().substring(
                                3));
                        propertyNames.add(newName);
                    }
                }
            }
        } else {
            // We add a single property
            NestedProperty np = getNestedProperty(propertyName);
            if (np.getKind() == NestedPropertyKind.PERSISTENT) {
                propertyNames.add(propertyName);
                persistentPropertyNames.add(propertyName);
            } else {
                // Transient property
                propertyNames.add(propertyName);
            }
        }
    }

    private static enum NestedPropertyKind {

        PERSISTENT,
        TRANSIENT
    }

    private static class NestedProperty implements Serializable {

        final NestedProperty parent;
        private final String name;
        final ClassMetadata<? extends Object> parentClassMetadata;
        final Method propertyGetterMethod;
        final Method propertySetterMethod;

        NestedProperty(String name,
                ClassMetadata<? extends Object> parentClassMetadata) {
            this.name = name;
            this.parentClassMetadata = parentClassMetadata;
            this.parent = null;
            this.propertyGetterMethod = null;
            this.propertySetterMethod = null;
        }

        NestedProperty(String name, Method propertyGetterMethod,
                Method propertySetterMethod) {
            this.name = name;
            this.parentClassMetadata = null;
            this.parent = null;
            this.propertyGetterMethod = propertyGetterMethod;
            this.propertySetterMethod = propertySetterMethod;
        }

        NestedProperty(String name,
                ClassMetadata<? extends Object> parentClassMetadata,
                NestedProperty parent) {
            this.name = name;
            this.parentClassMetadata = parentClassMetadata;
            this.parent = parent;
            this.propertyGetterMethod = null;
            this.propertySetterMethod = null;
        }

        NestedProperty(String name, Method propertyGetterMethod,
                Method propertySetterMethod,
                NestedProperty parent) {
            this.name = name;
            this.parentClassMetadata = null;
            this.parent = parent;
            this.propertyGetterMethod = propertyGetterMethod;
            this.propertySetterMethod = propertySetterMethod;
        }

        Object getValue(Object object) throws IllegalArgumentException {
            /*
             * First, we backtrack until we reach the root property and retrieve
             * its value. Then, we move back up the recursive invokation chain,
             * retrieving the property values until the intended value
             * has been returned.
             */
            if (parent != null) {
                object = parent.getValue(object);
            }
            if (object == null) {
                return null;
            }
            if (parentClassMetadata != null) {
                return ((ClassMetadata<Object>) parentClassMetadata).
                        getPropertyValue(object, name);
            } else {
                try {
                    return propertyGetterMethod.invoke(object);
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                            "Could not access property", e);
                }
            }
        }

        void setValue(Object object, Object value) throws
                IllegalArgumentException, IllegalStateException {
            /*
             * Algorithm similar to getValue()
             */
            if (parent != null) {
                object = parent.getValue(object);
            }
            if (object == null) {
                throw new IllegalStateException(
                        "A null value was found somewhere in the chain of nested properties");
            }
            if (parentClassMetadata != null) {
                ((ClassMetadata<Object>) parentClassMetadata).setPropertyValue(
                        object, name, value);
            } else {
                try {
                    propertySetterMethod.invoke(object, value);
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                            "Could not set property value", e);
                }
            }
        }

        String getName() {
            if (parent == null) {
                return name;
            } else {
                return parent.getName() + "." + name;
            }
        }

        Class<?> getType() {
            if (parentClassMetadata != null) {
                return parentClassMetadata.getProperty(name).getType();
            } else {
                return propertyGetterMethod.getReturnType();
            }
        }

        ClassMetadata<?> getMetadata() {
            if (parentClassMetadata != null) {
                PropertyMetadata pm = parentClassMetadata.getProperty(name);
                if (pm instanceof PersistentPropertyMetadata) {
                    return ((PersistentPropertyMetadata) pm).getTypeMetadata();
                }
            }
            return null;
        }

        NestedPropertyKind getKind() {
            if (parentClassMetadata != null && parentClassMetadata.getProperty(
                    name) instanceof PersistentPropertyMetadata) {
                return NestedPropertyKind.PERSISTENT;
            } else {
                return NestedPropertyKind.TRANSIENT;
            }
        }
    }
    private Map<String, NestedProperty> nestedPropertyMap = new HashMap<String, NestedProperty>();

    private NestedProperty getNestedProperty(String propertyName) throws
            IllegalArgumentException {
        if (nestedPropertyMap.containsKey(propertyName)) {
            return nestedPropertyMap.get(propertyName);
        } else {
            if (propertyName.indexOf('.') != -1) {
                // Try with the parent
                int offset = propertyName.lastIndexOf('.');
                String parentName = propertyName.substring(0, offset);
                String name = propertyName.substring(offset + 1);
                NestedProperty parentProperty = getNestedProperty(parentName);
                NestedProperty property;
                if (parentProperty.getMetadata() != null) {
                    PropertyMetadata pm = parentProperty.getMetadata().
                            getProperty(name);
                    if (pm == null) {
                        throw new IllegalArgumentException(
                                "Invalid property name");
                    } else {
                        property = new NestedProperty(pm.getName(), parentProperty.
                                getMetadata(), parentProperty);
                    }
                } else {
                    Method getter = getGetterMethod(name,
                            parentProperty.getType());
                    if (getter == null) {
                        throw new IllegalArgumentException(
                                "Invalid property name");
                    } else {
                        property = new NestedProperty(name, getter, getSetterMethod(
                                name, parentProperty.getType(), getter.
                                getReturnType()), parentProperty);
                    }
                }
                nestedPropertyMap.put(propertyName, property);
                return property;
            } else {
                // There are no more parent properties
                PropertyMetadata pm = metadata.getProperty(propertyName);
                if (pm == null) {
                    throw new IllegalArgumentException("Invalid property name");
                } else {
                    NestedProperty property = new NestedProperty(pm.getName(),
                            metadata);
                    nestedPropertyMap.put(propertyName, property);
                    return property;
                }
            }
        }
    }

    private Method getGetterMethod(String prop, Class<?> parent) {
        String propertyName = prop.substring(0, 1).toUpperCase() + prop.
                substring(1);
        try {
            Method m = parent.getMethod("get" + propertyName);
            if (m.getReturnType() != Void.TYPE) {
                return m;
            } else {
                return null;
            }
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private Method getSetterMethod(String prop, Class<?> parent,
            Class<?> propertyType) {
        String propertyName = prop.substring(0, 1).toUpperCase() + prop.
                substring(1);
        try {
            Method m = parent.getMethod("set" + propertyName, propertyType);
            if (m.getReturnType() == Void.TYPE) {
                return m;
            } else {
                return null;
            }
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Removes <code>propertyName</code> from the set of properties.
     *
     * @param propertyName the property name to remove, must not be null.
     */
    public void removeProperty(String propertyName) {
        assert propertyName != null : "propertyName must not be null";
        propertyNames.remove(propertyName);
        persistentPropertyNames.remove(propertyName);
        // Do not remove from map of nested properties in case the property
        // is referenced by other nested properties.
    }

    /**
     * Gets the set of all property names.
     * @return an unmodifiable set of property names (never null).
     */
    public Set<String> getPropertyNames() {
        return Collections.unmodifiableSet(propertyNames);
    }

    /**
     * Gets the set of persistent property names. This set is a subset
     * of {@link #getPropertyNames() }.
     * 
     * @return an unmodifiable set of property names (never null).
     */
    public Set<String> getPersistentPropertyNames() {
        return Collections.unmodifiableSet(persistentPropertyNames);
    }

    /**
     * Gets the value of <code>propertyName</code> from the instance <code>object</code>.
     * The property name may be nested, but must be in the {@link #getPropertyNames() } set.
     * <p>
     * When using nested properties and one of the properties in the chain is null,
     * this method will return null without throwing any exceptions.
     *
     * @param object the object that the property value is fetched from (must not be null).
     * @param propertyName the property name (must not be null).
     * @return the property value.
     * @throws IllegalArgumentException if the property name was illegal.
     */
    public Object getPropertyValue(T object, String propertyName) throws
            IllegalArgumentException {
        if (!getPropertyNames().contains(propertyName)) {
            throw new IllegalArgumentException(
                    "Illegal property name: " + propertyName);
        }
        if (propertyName.indexOf('.') == -1) {
            // Simple property
            return metadata.getPropertyValue(object, propertyName);
        } else {
            // Nested property
            NestedProperty np = getNestedProperty(propertyName);
            return np.getValue(object);
        }
    }

    /**
     * Sets the value of <code>propertyName</code> to <code>propertyValue</code>.
     * The property name may be nested, but must be in the {@link #getPropertyNames() } set.
     *
     * @param object the object to which the property is set (must not be null).
     * @param propertyName the property name (must not be null).
     * @param propertyValue the property value to set.
     * @throws IllegalArgumentException if the property name was illegal.
     * @throws IllegalStateException if one of the properties in the chain of nested properties was null.
     */
    public void setPropertyValue(T object, String propertyName,
            Object propertyValue) throws IllegalArgumentException,
            IllegalStateException {
        if (!getPropertyNames().contains(propertyName)) {
            throw new IllegalArgumentException(
                    "Illegal property name: " + propertyName);
        }
        if (propertyName.indexOf('.') == -1) {
            // Simple property
            metadata.setPropertyValue(object, propertyName, propertyValue);
        } else {
            // Nested property
            NestedProperty np = getNestedProperty(propertyName);
            np.setValue(object, propertyValue);
        }
    }
}
