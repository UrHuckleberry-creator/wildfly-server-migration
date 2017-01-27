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

package org.jboss.migration.wfly10.config.task.update;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ValueExpression;
import org.jboss.migration.core.task.AbstractServerMigrationTask;
import org.jboss.migration.core.task.ParentServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.env.SkippableByEnvServerMigrationTask;
import org.jboss.migration.wfly10.config.management.InterfaceResource;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupManagement;
import org.jboss.migration.wfly10.config.management.SocketBindingGroupResources;
import org.jboss.migration.wfly10.config.management.SocketBindingResource;
import org.jboss.migration.wfly10.config.management.SocketBindingResources;
import org.jboss.migration.wfly10.config.task.executor.SubtaskExecutorAdapters;
import org.jboss.migration.wfly10.config.task.management.ManageableResourceTask;
import org.jboss.migration.wfly10.config.task.management.ManageableServerConfigurationTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Adds private interface to config, and updates jgroup socket bindings to use it.
 * @author emmartins
 */
public class AddPrivateInterfaceTask extends ManageableServerConfigurationTask {

    private static final String TASK_NAME = "setup-private-interface";

    private AddPrivateInterfaceTask(BaseBuilder builder, Object source, Collection resources) {
        super(builder, source, resources);
    }

    public static class Builder extends ManageableServerConfigurationTask.Builder {
        public Builder() {
            super(new ServerMigrationTaskName.Builder(TASK_NAME).build());
            listener(new AbstractServerMigrationTask.Listener() {
                @Override
                public void started(TaskContext context) {
                    context.getLogger().infof("Private interface setup starting...");
                }
                @Override
                public void done(TaskContext context) {
                    context.getLogger().infof("Private interface setup done.");
                }
            });
            subtask(InterfaceResource.Parent.class, new AddInterface<>());
            subtask(SocketBindingResource.Parent.class, new UpdateSocketBindingGroups<>());
        }
    }

    private static final String INTERFACE_NAME = "private";
    private static final String[] SOCKET_BINDING_NAMES = {"jgroups-mping", "jgroups-tcp", "jgroups-tcp-fd", "jgroups-udp", "jgroups-udp-fd"};

    public static final AddPrivateInterfaceTask INSTANCE = new AddPrivateInterfaceTask();


    static class AddInterface<S> implements ManageableResourceTask.SubtaskExecutor<S, InterfaceResource.Parent> {

        private static final ServerMigrationTaskName SUBTASK_NAME = new ServerMigrationTaskName.Builder("add-interface").build();

        @Override
        public void run(S source, Collection<? extends InterfaceResource.Parent> resources, TaskContext context) throws Exception {
            // subtask to add private interface
            final ServerMigrationTask task = new ServerMigrationTask() {
                @Override
                public ServerMigrationTaskName getName() {
                    return SUBTASK_NAME;
                }

                @Override
                public ServerMigrationTaskResult run(TaskContext context) throws Exception {
                    if (interfaceResources.getResourceNames().contains(INTERFACE_NAME)) {
                        context.getLogger().debugf("Skipping task to add private interface, the configuration already has it.");
                        return ServerMigrationTaskResult.SKIPPED;
                    }
                    boolean addInterface = false;
                    final SocketBindingGroupResources socketBindingGroupResources = interfaceResources.getServerConfiguration().getSocketBindingGroupResources();
                    for (String socketBindingGroupName : socketBindingGroupResources.getResourceNames()) {
                        final SocketBindingGroupManagement socketBindingGroupManagement = socketBindingGroupResources.getSocketBindingGroupManagement(socketBindingGroupName);
                        final Set<String> socketBindings = socketBindingGroupManagement.getSocketBindingsManagement().getResourceNames();
                        for (String jgroupsSocketBinding : SOCKET_BINDING_NAMES) {
                            if (socketBindings.contains(jgroupsSocketBinding)) {
                                addInterface = true;
                                break;
                            }
                        }
                        if (addInterface) {
                            break;
                        }
                    }
                    if (!addInterface) {
                        context.getLogger().debugf("Skipping task to add private interface, the target socket bindings are not present in the configuration.");
                        return ServerMigrationTaskResult.SKIPPED;
                    }
                    final ModelNode addInterfaceOp = Util.createAddOperation(interfaceResources.getResourcePathAddress(INTERFACE_NAME));
                    addInterfaceOp.get(INET_ADDRESS).set(new ValueExpression("${jboss.bind.address.private:127.0.0.1}"));
                    interfaceResources.getServerConfiguration().executeManagementOperation(addInterfaceOp);
                    context.getLogger().infof("Interface %s added.", INTERFACE_NAME);
                    return ServerMigrationTaskResult.SUCCESS;
                }
            };
            context.execute(new SkippableByEnvServerMigrationTask(task, TASK_NAME + "." + SUBTASK_NAME + ".skip"));
        }
    }

    static class UpdateSocketBindingGroups<S> implements ManageableResourceTask.SubtaskExecutor<S, SocketBindingResource.Parent> {

        private static final ServerMigrationTaskName SUBTASK_NAME = new ServerMigrationTaskName.Builder("update-socket-binding-groups").build();

        @Override
        public void executeSubtasks(S source, SocketBindingGroupResources socketBindingGroupResources, TaskContext context) throws Exception {
            final ParentServerMigrationTask.Builder taskBuilder = new ParentServerMigrationTask.Builder(SUBTASK_NAME)
                    .skipTaskPropertyName(TASK_NAME + "." + SUBTASK_NAME + ".skip");
            for (final String socketBindingGroupName : socketBindingGroupResources.getResourceNames()) {
                final ServerMigrationTask subtask = getResourceTask(source, socketBindingGroupResources.getSocketBindingGroupManagement(socketBindingGroupName));
                if (subtask != null) {
                    taskBuilder.subtask(subtask);
                }
            }
            context.execute(taskBuilder.build());
        }

        public ServerMigrationTask getResourceTask(S source, final SocketBindingGroupManagement socketBindingGroupManagement) throws Exception {
            // subtask to update jgroup socket bindings, to use private interface
            final ServerMigrationTaskName subtaskName = new ServerMigrationTaskName.Builder("update-socket-binding-group").addAttribute("name", socketBindingGroupManagement.getSocketBindingGroupName()).build();
            final ServerMigrationTask subtask = new ServerMigrationTask() {
                @Override
                public ServerMigrationTaskName getName() {
                    return subtaskName;
                }

                @Override
                public ServerMigrationTaskResult run(TaskContext context) throws Exception {
                    final List<String> updated = new ArrayList<>();
                    final SocketBindingResources resourceManagement = socketBindingGroupManagement.getSocketBindingsManagement();
                    for (String socketBinding : SOCKET_BINDING_NAMES) {
                        ModelNode config = resourceManagement.getResourceConfiguration(socketBinding);
                        if (config != null) {
                            if (!config.hasDefined(INTERFACE) || !config.get(INTERFACE).asString().equals(INTERFACE_NAME)) {
                                final PathAddress pathAddress = resourceManagement.getResourcePathAddress(socketBinding);
                                final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                                writeAttrOp.get(NAME).set(INTERFACE);
                                writeAttrOp.get(VALUE).set(INTERFACE_NAME);
                                resourceManagement.getServerConfiguration().executeManagementOperation(writeAttrOp);
                                context.getLogger().infof("Socket binding %s interface set to %s", pathAddress.toCLIStyleString(), INTERFACE_NAME);
                                updated.add(socketBinding);
                            }
                        }
                    }
                    if (updated.isEmpty()) {
                        return ServerMigrationTaskResult.SKIPPED;
                    } else {
                        return new ServerMigrationTaskResult.Builder()
                                .success()
                                .addAttribute("updated", updated.toString())
                                .build();
                    }
                }
            };
            return subtask;
        }
    }
}