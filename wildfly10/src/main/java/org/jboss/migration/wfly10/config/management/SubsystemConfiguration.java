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

package org.jboss.migration.wfly10.config.management;

import org.jboss.as.controller.PathAddress;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * A manageable Subsystem configuration.
 * @author emmartins
 */
public interface SubsystemConfiguration extends ManageableResource {

    Type<SubsystemConfiguration> RESOURCE_TYPE = new Type<>(SubsystemConfiguration.class);

    @Override
    default Type<SubsystemConfiguration> getResourceType() {
        return RESOURCE_TYPE;
    }

    /**
     * A {@link ManageableResource} which has {@link SubsystemConfiguration} children.
     */
    interface Parent extends ManageableResource {
        SubsystemConfiguration getSubsystemConfiguration(String resourceName) throws IOException;
        List<SubsystemConfiguration> getSubsystemConfigurations() throws IOException;
        Set<String> getSubsystemConfigurationNames() throws IOException;
        PathAddress getSubsystemConfigurationPathAddress(String resourceName);
        void removeSubsystemConfiguration(String resourceName) throws IOException;
    }
}
