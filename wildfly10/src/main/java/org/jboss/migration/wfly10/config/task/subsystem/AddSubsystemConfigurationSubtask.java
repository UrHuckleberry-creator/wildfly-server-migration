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

package org.jboss.migration.wfly10.config.task.subsystem;

import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;

/**
 * A task which creates a subsystem if its missing from the server's config.
 * @author emmartins
 */
public class AddSubsystemConfigurationSubtask implements SubsystemConfigurationSubtask {

    @Override
    public ServerMigrationTaskName getName(ParentTaskContext parentTaskContext) {
        return new ServerMigrationTaskName.Builder("add-subsystem-config").addAttribute("name", parentTaskContext.getConfigName()).build();
    }

    @Override
    public ServerMigrationTaskResult run(ParentTaskContext parentTaskContext, TaskContext taskContext, TaskEnvironment taskEnvironment) throws Exception {
        final String configName = parentTaskContext.getConfigName();
        if (parentTaskContext.getSubsystemsManagement().getResource(parentTaskContext.getSubsystem()) != null) {
            taskContext.getLogger().infof("Skipped adding subsystem config %s, already exists.", configName);
            return ServerMigrationTaskResult.SKIPPED;
        }
        taskContext.getLogger().debugf("Adding subsystem config %s...", configName);
        addConfiguration(parentTaskContext, taskContext, taskEnvironment);
        taskContext.getLogger().infof("Subsystem config %s added.", configName);
        return ServerMigrationTaskResult.SUCCESS;
    }

    protected void addConfiguration(ParentTaskContext parentTaskContext, TaskContext taskContext, TaskEnvironment taskEnvironment) throws Exception {
        final SubsystemsManagement resourcesManagement = parentTaskContext.getSubsystemsManagement();
        final ModelNode op = Util.createAddOperation(resourcesManagement.getResourcePathAddress(parentTaskContext.getSubsystem()));
        resourcesManagement.getServerConfiguration().executeManagementOperation(op);
    }
}
