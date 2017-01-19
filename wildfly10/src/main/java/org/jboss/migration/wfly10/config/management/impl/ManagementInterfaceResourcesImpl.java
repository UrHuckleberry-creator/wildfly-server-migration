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
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.ManagementInterfaceResource;
import org.jboss.migration.wfly10.config.management.ManagementInterfaceResources;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MANAGEMENT_INTERFACE;

/**
 * @author emmartins
 */
public class ManagementInterfaceResourcesImpl extends ManageableResourcesImpl<ManagementInterfaceResource> implements ManagementInterfaceResources {
    public ManagementInterfaceResourcesImpl(PathAddress parentPathAddress, ManageableServerConfiguration serverConfiguration) {
        super(ManagementInterfaceResource.TYPE, parentPathAddress, MANAGEMENT_INTERFACE, serverConfiguration);
    }

    @Override
    public ManagementInterfaceResource getResourceInstance(String resourceName) {
        return new ManagementInterfaceResourceImpl(resourceName, getResourcePathAddress(resourceName), getServerConfiguration());
    }
}