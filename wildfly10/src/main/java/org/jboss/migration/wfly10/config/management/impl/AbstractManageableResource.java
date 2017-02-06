/*
 * Copyright 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.ManagementOperationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author emmartins
 */
public abstract class AbstractManageableResource<P extends ManageableResource> implements ManageableResource {

    private final Map<Type, Factory> childResourceFactories = new HashMap<>();

    private final String resourceName;
    private final PathAddress pathAddress;
    private final P parent;
    private final ManageableServerConfiguration serverConfiguration;

    protected AbstractManageableResource(String resourceName, PathAddress pathAddress, P parent) {
        this.resourceName = resourceName;
        this.pathAddress = pathAddress != null ? pathAddress : PathAddress.EMPTY_ADDRESS;
        this.parent = parent;
        this.serverConfiguration = parent != null ? parent.getServerConfiguration() : (ManageableServerConfiguration) this;
    }

    @Override
    public String getResourceName() {
        return resourceName;
    }

    @Override
    public PathAddress getResourcePathAddress() {
        return pathAddress;
    }

    @Override
    public P getParentResource() {
        return parent;
    }

    @Override
    public ManageableServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }

    @Override
    public ModelNode getResourceConfiguration() {
        final PathAddress address = getResourcePathAddress();
        final ModelNode op = Util.createEmptyOperation(READ_RESOURCE_OPERATION, address);
        op.get(RECURSIVE).set(true);
        final ModelNode result = serverConfiguration.executeManagementOperation(op);
        return result.get(RESULT);
    }

    protected void addChildResourceFactory(Factory childResourceFactory) {
        childResourceFactories.put(childResourceFactory.getResourceType(), childResourceFactory);
    }

    protected <T extends ManageableResource> Factory<T, ?> getChildResourceFactory(Type<T> resourceType) {
        return childResourceFactories.get(resourceType);
    }

    protected <T extends ManageableResource> List<Factory<T, ?>> getChildResourceFactories(Class<T> resourceType) {
        return childResourceFactories.values().stream().filter(factory -> factory.getResourceType().getType().isInstance(resourceType)).collect(toList());
    }

    protected <T extends ManageableResource> List<Factory<?, ?>> getDescendantResourceFactories(Type<T> resourceType) {
        return childResourceFactories.values().stream().filter(factory -> factory.getResourceType().getDescendantTypes().contains(resourceType)).collect(toList());
    }

    protected <T extends ManageableResource> List<Factory<?, ?>> getDescendantResourceFactories(Class<T> resourceType) {
        return childResourceFactories.values().stream().filter(factory -> {
            final Set<Type<?>> descendantTypes = factory.getResourceType().getDescendantTypes();
            for (Type<?> t : descendantTypes) {
                if (t.getType().isInstance(resourceType)) {
                    return true;
                }
            }
            return false;
        }).collect(toList());
    }

    @Override
    public <T extends ManageableResource> T getChildResource(Type<T> resourceType, String resourceName) {
        final Factory<T, ?> factory = getChildResourceFactory(resourceType);
        return factory != null ? factory.getResource(resourceName) : null;
    }

    @Override
    public <T extends ManageableResource> List<T> getChildResources(Type<T> resourceType) {
        final Factory<T, ?> factory = getChildResourceFactory(resourceType);
        return factory != null ? factory.getResources() : null;
    }

    @Override
    public <T extends ManageableResource> List<T> getChildResources(Class<T> resourceType) {
        return getChildResources(resourceType, null);
    }

    @Override
    public <T extends ManageableResource> List<T> getChildResources(Class<T> resourceType, String resourceName) {
        final List<Factory<T, ?>> factories = getChildResourceFactories(resourceType);
        if (factories.isEmpty()) {
            return Collections.emptyList();
        } else {
            final List<T> result = new ArrayList<T>();
            for (Factory<T, ?> factory : factories) {
                if (resourceName != null) {
                    final T t = factory.getResource(resourceName);
                    if (t != null) {
                        result.add(t);
                    }
                } else {
                    result.addAll(factory.getResources());
                }
            }
            return result;
        }
    }

    @Override
    public Set<Type<?>> getChildResourceTypes() {
        return Collections.unmodifiableSet(childResourceFactories.keySet());
    }

    @Override
    public Set<String> getChildResourceNames(Type<?> resourceType) {
        final Factory factory = getChildResourceFactory(resourceType);
        return factory != null ? factory.getResourceNames() : null;
    }

    @Override
    public <T extends ManageableResource> PathAddress getChildResourcePathAddress(Type<T> resourceType, String resourceName) {
        final Factory<T, ?> factory = getChildResourceFactory(resourceType);
        return factory != null ? factory.getResourcePathAddress(resourceName) : null;
    }

    @Override
    public void removeResource(Type<?> resourceType, String resourceName) {
        final Factory<?, ?> factory = getChildResourceFactory(resourceType);
        if (factory != null) {
            factory.removeResource(resourceName);
        }
    }

    @Override
    public void remove() throws ManagementOperationException {
        final PathAddress address = getResourcePathAddress();
        final ModelNode op = Util.createRemoveOperation(address);
        serverConfiguration.executeManagementOperation(op);
    }

    @Override
    public <T extends ManageableResource> Set<T> findResources(Type<T> resourceType) {
        return findResources(resourceType, null);
    }

    @Override
    public <T extends ManageableResource> Set<T> findResources(Type<T> resourceType, String resourceName) {
        final Set<T> result = new HashSet<>();
        // this
        if (resourceType.equals(getResourceType()) && (resourceName == null || resourceName.equals(getResourceName()))) {
            result.add((T) this);
        }
        // children
        /*
        final Factory<T> childFactory = getChildResourceFactory(resourceType);
        if (childFactory != null) {
            if (resourceName != null) {
                final T child = childFactory.getResource(resourceName);
                if (child != null) {
                    result.add(child);
                }
            } else {
                result.addAll(childFactory.getResources());
            }
        }*/
        // descendants
        for(Factory<?, ?> descendantFactory : getDescendantResourceFactories(resourceType)) {
            for (ManageableResource child : descendantFactory.getResources()) {
                result.addAll(child.findResources(resourceType, resourceName));
            }
        }
        return result;
    }

    @Override
    public <T extends ManageableResource> Set<T> findResources(Class<T> resourceType) {
        return findResources(resourceType, null);
    }

    @Override
    public <T extends ManageableResource> Set<T> findResources(Class<T> resourceType, String resourceName) {
        final Set<T> result = new HashSet<>();
        // this
        if (getClass().isInstance(resourceType) && (resourceName == null || resourceName.equals(getResourceName()))) {
            result.add((T) this);
        }
        /*
        // children
        for(Factory<T> childFactory : getChildResourceFactories(resourceType)) {
            if (resourceName != null) {
                final T child = childFactory.getResource(resourceName);
                if (child != null) {
                    result.add(child);
                }
            } else {
                result.addAll(childFactory.getResources());
            }
        }
        */
        // descendants
        for(Factory<?, ?> descendantFactory : getDescendantResourceFactories(resourceType)) {
            for (ManageableResource child : descendantFactory.getResources()) {
                result.addAll(child.findResources(resourceType, resourceName));
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractManageableResource that = (AbstractManageableResource) o;

        if (!pathAddress.equals(that.pathAddress)) return false;
        return serverConfiguration.equals(that.serverConfiguration);
    }

    @Override
    public int hashCode() {
        return pathAddress.hashCode();
    }

    protected static abstract class Factory<T extends ManageableResource, P extends ManageableResource> {

        protected final P parentResource;
        protected final ManageableServerConfiguration serverConfiguration;

        protected final PathAddress pathAddressBase;
        protected final String pathElementKey;
        protected final Type<T> resourceType;

        public Factory(Type<T> resourceType, PathAddress pathAddressBase, String pathElementKey, P parentResource) {
            this.resourceType = resourceType;
            this.pathAddressBase = pathAddressBase;
            this.pathElementKey = pathElementKey;
            this.parentResource = parentResource;
            this.serverConfiguration = parentResource.getServerConfiguration();
        }

        public PathAddress getResourcePathAddress(String resourceName) {
            return pathAddressBase.append(pathElementKey, resourceName);
        }

        public Set<String> getResourceNames() {
            try {
                final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_NAMES_OPERATION, pathAddressBase);
                op.get(CHILD_TYPE).set(pathElementKey);
                final ModelNode opResult = serverConfiguration.executeManagementOperation(op);
                Set<String> result = new HashSet<>();
                for (ModelNode resultNode : opResult.get(RESULT).asList()) {
                    result.add(resultNode.asString());
                }
                return result;
            } catch (ManagementOperationException e) {
                try {
                    final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_TYPES_OPERATION, pathAddressBase);
                    final ModelNode opResult = serverConfiguration.executeManagementOperation(op);
                    boolean childrenTypeFound = false;
                    for (ModelNode resultNode : opResult.get(RESULT).asList()) {
                        if (pathElementKey.equals(resultNode.asString())) {
                            childrenTypeFound = true;
                            break;
                        }
                    }
                    if (!childrenTypeFound) {
                        return Collections.emptySet();
                    }
                } catch (Throwable t) {
                    // ignore
                }
                throw e;
            }
        }

        public ModelNode getResourceConfiguration(String name) {
            if (!getResourceNames().contains(name)) {
                return null;
            }
            final PathAddress address = getResourcePathAddress(name);
            final ModelNode op = Util.createEmptyOperation(READ_RESOURCE_OPERATION, address);
            op.get(RECURSIVE).set(true);
            final ModelNode result = serverConfiguration.executeManagementOperation(op);
            return result.get(RESULT);
        }

        public void removeResource(String resourceName) {
            final PathAddress address = getResourcePathAddress(resourceName);
            final ModelNode op = Util.createRemoveOperation(address);
            serverConfiguration.executeManagementOperation(op);
        }

        public Type<T> getResourceType() {
            return resourceType;
        }

        public T getResource(String resourceName) {
            return getResourceNames().contains(resourceName) ? newResourceInstance(resourceName) : null;
        }

        public List<T> getResources() {
            final Set<String> resourceNames = getResourceNames();
            if (resourceNames.isEmpty()) {
                return Collections.emptyList();
            } else {
                final List<T> result = new ArrayList<>();
                for (String resourceName : resourceNames) {
                    result.add(getResource(resourceName));
                }
                return result;
            }
        }

        protected abstract T newResourceInstance(String resourceName);
    }
}
