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
 * @author emmartins
 */
public interface SocketBindingResource extends ManageableResource {

    Type<SocketBindingResource> RESOURCE_TYPE = new Type<>(SocketBindingResource.class);

    @Override
    default Type<SocketBindingResource> getResourceType() {
        return RESOURCE_TYPE;
    }

    interface Parent extends ManageableResource {
        SocketBindingResource getSocketBindingResource(String resourceName) throws IOException;
        List<SocketBindingResource> getSocketBindingResources() throws IOException;
        Set<String> getSocketBindingResourceNames() throws IOException;
        PathAddress getSocketBindingResourcePathAddress(String resourceName);
        void removeSocketBindingResource(String resourceName) throws IOException;
    }
}
