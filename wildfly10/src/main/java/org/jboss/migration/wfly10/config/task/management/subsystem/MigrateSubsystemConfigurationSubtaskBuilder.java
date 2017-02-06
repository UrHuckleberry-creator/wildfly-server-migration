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

package org.jboss.migration.wfly10.config.task.management.subsystem;

import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationFailureException;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceLeafTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * The builder for leaf tasks, which add subsystem configs.
 * @author emmartins
 */
public class MigrateSubsystemConfigurationSubtaskBuilder<S> extends ResourceLeafTask.Builder<S, SubsystemConfiguration> {

    private String subsystem;

    public MigrateSubsystemConfigurationSubtaskBuilder(String subsystem) {
        this.subsystem = subsystem;
        nameBuilder(parameters -> new ServerMigrationTaskName.Builder("migrate-subsystem-config").addAttribute("name", parameters.getResource().getResourceAbsoluteName()).build());
        runBuilder(params -> context -> migrateConfiguration(params.getResource(), context));
    }

    protected ServerMigrationTaskResult migrateConfiguration(SubsystemConfiguration subsystemConfiguration, TaskContext taskContext) {
        final String configName = subsystemConfiguration.getResourceAbsoluteName();
        taskContext.getLogger().debugf("Migrating subsystem config %s...", configName);
        final ModelNode op = Util.createEmptyOperation("migrate", subsystemConfiguration.getResourcePathAddress());
        final ModelNode result;
        try {
            result = subsystemConfiguration.getServerConfiguration().getModelControllerClient().execute(op);
        } catch (IOException e) {
            throw new ServerMigrationFailureException("Subsystem config "+configName+" migration failed", e);
        }
        taskContext.getLogger().debugf("Op result: %s", result.asString());
        final String outcome = result.get(OUTCOME).asString();
        if(!SUCCESS.equals(outcome)) {
            throw new ServerMigrationFailureException("Subsystem config "+configName+" migration failed: "+result.get("migration-error").asString());
        } else {
            final ServerMigrationTaskResult.Builder resultBuilder = new ServerMigrationTaskResult.Builder().success();
            final List<String> migrateWarnings = new ArrayList<>();
            if (result.get(RESULT).hasDefined("migration-warnings")) {
                for (ModelNode modelNode : result.get(RESULT).get("migration-warnings").asList()) {
                    migrateWarnings.add(modelNode.asString());
                }
            }
            processWarnings(migrateWarnings, subsystemConfiguration, taskContext);
            if (migrateWarnings.isEmpty()) {
                taskContext.getLogger().infof("Subsystem config %s migrated.", configName);
            } else {
                taskContext.getLogger().infof("Subsystem config %s migrated with warnings: %s", configName, migrateWarnings);
                resultBuilder.addAttribute("migration-warnings", migrateWarnings);
            }
            // FIXME tmp workaround for legacy subsystems which do not remove itself
            if (subsystemConfiguration.getResourceConfiguration() != null) {
                // remove itself after migration
                subsystemConfiguration.getParentResource().removeResource(SubsystemConfiguration.RESOURCE_TYPE, subsystemConfiguration.getResourceName());
                taskContext.getLogger().debugf("Subsystem config %s removed after migration.", configName);
            }
            return resultBuilder.build();
        }
    }

    protected void processWarnings(List<String> migrateWarnings, SubsystemConfiguration subsystemConfiguration, TaskContext taskContext) {
    }

    public String getSubsystem() {
        return subsystem;
    }
}
