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

package org.jboss.migration.wfly10.config.task.management.resources;

import org.jboss.migration.core.task.component.BuildParameters;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelector;
import org.jboss.migration.wfly10.config.management.ManageableResourceSelectors;

import java.util.Collection;
import java.util.Collections;

/**
 * @author emmartins
 */
public class ManageableResourcesToResourcesParametersMapper<S, T extends ManageableResource, R extends ManageableResource> implements BuildParameters.Mapper<ManageableResourcesBuildParameters<S, T>, ManageableResourcesBuildParameters<S, R>> {

    private final ManageableResourceSelector<? extends R> resourceSelector;

    public ManageableResourcesToResourcesParametersMapper(ManageableResourceSelector<? extends R> resourceSelector) {
        this.resourceSelector = resourceSelector;
    }

    public ManageableResourcesToResourcesParametersMapper(Class<? extends R> resourceType) {
        this(ManageableResourceSelectors.selectResources(resourceType));
    }

    public ManageableResourcesToResourcesParametersMapper(Class<? extends R> resourceType, String resourceName) {
        this(ManageableResourceSelectors.selectResources(resourceType, resourceName));
    }

    @Override
    public Collection<ManageableResourcesBuildParameters<S, R>> apply(ManageableResourcesBuildParameters<S, T> tParams) {
        return Collections.singleton(new ManageableResourcesBuildParametersImpl<S, R>(tParams.getSource(), tParams.getServerConfiguration(), resourceSelector.fromResources(tParams.getResources())));
    }
}
