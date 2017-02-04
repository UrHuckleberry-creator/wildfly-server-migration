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

package org.jboss.migration.wfly10.config.task.management.configuration;

import org.jboss.migration.core.task.component.CompositeTaskBuilder;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelector;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelectors;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceCompositeSubtasksBuilder;
import org.jboss.migration.wfly10.config.task.management.resource.ServerConfigurationToResourceParametersMapper;
import org.jboss.migration.wfly10.config.task.management.resources.ResourcesCompositeSubtasksBuilder;
import org.jboss.migration.wfly10.config.task.management.resources.ServerConfigurationToResourcesParametersMapper;

/**
 * @author emmartins
 */
public interface ServerConfigurationCompositeTaskBuilder<S, T extends ServerConfigurationCompositeTaskBuilder<S, T>> extends CompositeTaskBuilder<ServerConfigurationBuildParameters<S>, T>, ServerConfigurationComponentTaskBuilder<S, T> {

    default <R1 extends ManageableResource> T subtasks(Class<? extends R1> resourceType, ResourcesCompositeSubtasksBuilder<S, R1, ?> builder) {
        return subtasks(ManageableResourceSelectors.selectResources(resourceType), builder);
    }

    default <R1 extends ManageableResource> T subtasks(Class<? extends R1> resourceType, String resourceName, ResourcesCompositeSubtasksBuilder<S, R1, ?> builder) {
        return subtasks(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
    }

    default <R1 extends ManageableResource> T subtasks(ManageableResourceSelector<? extends R1> resourceSelector, ResourcesCompositeSubtasksBuilder<S, R1, ?> builder) {
        return subtasks(new ServerConfigurationToResourcesParametersMapper<>(resourceSelector), builder);
    }

    // --

    default <R1 extends ManageableResource> T subtasks(Class<? extends R1> resourceType, ResourceCompositeSubtasksBuilder<S, R1, ?> builder) {
        return subtasks(ManageableResourceSelectors.selectResources(resourceType), builder);
    }

    default <R1 extends ManageableResource> T subtasks(Class<? extends R1> resourceType, String resourceName, ResourceCompositeSubtasksBuilder<S, R1, ?> builder) {
        return subtasks(ManageableResourceSelectors.selectResources(resourceType, resourceName), builder);
    }

    default <R1 extends ManageableResource> T subtasks(ManageableResourceSelector<? extends R1> resourceSelector, ResourceCompositeSubtasksBuilder<S, R1, ?> builder) {
        return subtasks(new ServerConfigurationToResourceParametersMapper<>(resourceSelector), builder);
    }

}
