/*
 * Copyright 2016 Red Hat, Inc.
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
import org.jboss.migration.wfly10.config.management.InterfaceResource;
import org.jboss.migration.wfly10.config.management.InterfaceResources;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INTERFACE;

/**
 * @author emmartins
 */
public class InterfaceResourcesImpl extends ManageableResourcesImpl<InterfaceResource> implements InterfaceResources {
    public InterfaceResourcesImpl(PathAddress parentPathAddress, ManageableServerConfiguration configurationManagement) {
        super(InterfaceResource.TYPE, parentPathAddress, INTERFACE, configurationManagement);
    }
    @Override
    public InterfaceResource getResourceInstance(String resourceName) {
        return new InterfaceResourceImpl(resourceName, getResourcePathAddress(resourceName), getServerConfiguration());
    }
}