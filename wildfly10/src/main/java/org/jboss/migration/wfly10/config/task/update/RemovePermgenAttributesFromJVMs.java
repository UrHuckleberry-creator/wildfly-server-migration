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

package org.jboss.migration.wfly10.config.task.update;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.task.AbstractServerMigrationTask;
import org.jboss.migration.core.task.ParentServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.config.management.HostConfiguration;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.JvmResources;
import org.jboss.migration.wfly10.config.management.ServerGroupResources;
import org.jboss.migration.wfly10.config.task.executor.JVMsManagementSubtaskExecutor;
import org.jboss.migration.wfly10.config.task.factory.DomainConfigurationTaskFactory;
import org.jboss.migration.wfly10.config.task.factory.HostConfigurationTaskFactory;

/**
 * Removes permgen from JVM Configs.
 * @author emmartins
 */
public class RemovePermgenAttributesFromJVMs<S> implements HostConfigurationTaskFactory<S>, DomainConfigurationTaskFactory<S> {

    public static final RemovePermgenAttributesFromJVMs INSTANCE  = new RemovePermgenAttributesFromJVMs();

    private static final String TASK_NAME_NAME = "remove-permgen-attributes-from-jvms";
    private static final ServerMigrationTaskName TASK_NAME = new ServerMigrationTaskName.Builder(TASK_NAME_NAME).build();

    private RemovePermgenAttributesFromJVMs() {
    }

    @Override
    public ServerMigrationTask getTask(S source, HostConfiguration configuration) throws Exception {
        return getTask(getSubtasks(source, configuration.getJvmResources()));
    }

    protected ServerMigrationTask getTask(ParentServerMigrationTask.SubtaskExecutor subtaskExecutor) throws Exception {
        return new ParentServerMigrationTask.Builder(TASK_NAME)
                .subtask(subtaskExecutor)
                .listener(new AbstractServerMigrationTask.Listener() {
                    @Override
                    public void started(TaskContext context) {
                        context.getLogger().infof("Removal of permgen attributes from JVM configs starting...");
                    }
                    @Override
                    public void done(TaskContext context) {
                        context.getLogger().infof("Removal of permgen attributes from JVM configs done.");
                    }
                })
                .build();
    }

    protected ParentServerMigrationTask.SubtaskExecutor getSubtasks(final S source, final JvmResources JvmResources) throws Exception {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(final TaskContext context) throws Exception {
                JVMsSubtaskExecutor.INSTANCE.executeSubtasks(source, JvmResources, context);
            }
        };
    }

    @Override
    public ServerMigrationTask getTask(S source, HostControllerConfiguration configuration) throws Exception {
        return getTask(getSubtasks(source, configuration.getServerGroupResources()));
    }

    protected ParentServerMigrationTask.SubtaskExecutor getSubtasks(final S source, final ServerGroupResources serverGroupResources) throws Exception {
        return new ParentServerMigrationTask.SubtaskExecutor() {
            @Override
            public void executeSubtasks(final TaskContext context) throws Exception {
                for (String serverGroupName : serverGroupResources.getResourceNames()) {
                    getSubtasks(source, serverGroupResources.getResource(serverGroupName).getJvmResources()).executeSubtasks(context);
                }
            }
        };
    }

    public static class JVMsSubtaskExecutor<S> implements JVMsManagementSubtaskExecutor<S> {

        public static final JVMsSubtaskExecutor INSTANCE  = new JVMsSubtaskExecutor();

        private static final String SUBTASK_NAME_NAME = "remove-permgen-attributes-from-jvm";

        private JVMsSubtaskExecutor() {
        }

        @Override
        public void executeSubtasks(S source, final JvmResources resourceManagement, TaskContext context) throws Exception {
            for (final String resourceName : resourceManagement.getResourceNames()) {
                final ServerMigrationTaskName taskName = new ServerMigrationTaskName.Builder(SUBTASK_NAME_NAME)
                        .addAttribute("resource", resourceManagement.getResourcePathAddress(resourceName).toCLIStyleString())
                        .build();
                final ServerMigrationTask subtask = new ServerMigrationTask() {
                    @Override
                    public ServerMigrationTaskName getName() {
                        return taskName;
                    }
                    @Override
                    public ServerMigrationTaskResult run(TaskContext context) throws Exception {
                        final ModelNode config = resourceManagement.getResourceConfiguration(resourceName);
                        final PathAddress pathAddress = resourceManagement.getResourcePathAddress(resourceName);
                        boolean updated = false;
                        if (config.hasDefined("permgen-size")) {
                            final ModelNode op = Util.getUndefineAttributeOperation(pathAddress, "permgen-size");
                            resourceManagement.getServerConfiguration().executeManagementOperation(op);
                            updated = true;
                        }
                        if (config.hasDefined("max-permgen-size")) {
                            final ModelNode op = Util.getUndefineAttributeOperation(pathAddress, "max-permgen-size");
                            resourceManagement.getServerConfiguration().executeManagementOperation(op);
                            updated = true;
                        }
                        if (!updated) {
                            return ServerMigrationTaskResult.SKIPPED;
                        }
                        context.getLogger().infof("Permgen removed from JVM %s", pathAddress.toCLIStyleString());
                        return ServerMigrationTaskResult.SUCCESS;
                    }
                };
                context.execute(subtask);
            }
        }
    }
}