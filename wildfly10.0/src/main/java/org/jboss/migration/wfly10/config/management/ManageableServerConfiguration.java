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

package org.jboss.migration.wfly10.config.management;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.jboss.AbsolutePathResolver;
import org.jboss.migration.core.jboss.TargetJBossServer;

import java.nio.file.Path;

/**
 * @author emmartins
 */
public interface ManageableServerConfiguration extends AbsolutePathResolver, ManageableResource, ExtensionResource.Parent, InterfaceResource.Parent, PathResource.Parent, SocketBindingGroupResource.Parent, SystemPropertyResource.Parent {

    void start();
    void stop();
    boolean isStarted();
    ModelNode executeManagementOperation(ModelNode operation) throws ManagementOperationException;
    TargetJBossServer getServer();
    Path resolvePath(String path) throws ManagementOperationException;
    ModelControllerClient getModelControllerClient();
    Path getConfigurationDir();
    Path getDataDir();
    Path getContentDir();

    default ManageableServerConfigurationType getConfigurationType() {
        return (ManageableServerConfigurationType) getResourceType();
    }

    @Override
    default Path resolveNamedPath(String string) {
        PathResource pathResource = getPathResource(string);
        if (pathResource != null) {
            return resolvePath(pathResource.getResourceConfiguration());
        } else {
            return getServer().resolveNamedPath(string);
        }
    }
}