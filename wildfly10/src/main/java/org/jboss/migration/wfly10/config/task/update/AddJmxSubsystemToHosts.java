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
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceBuildParameters;
import org.jboss.migration.wfly10.config.task.management.subsystem.AddSubsystemConfigurationSubtaskBuilder;
import org.jboss.migration.wfly10.config.task.management.subsystem.AddSubsystemConfigurationTaskBuilder;
import org.jboss.migration.wfly10.config.task.subsystem.ExtensionNames;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemNames;

/**
 * A task which adds the jmx subsystem to host configs.
 * @author emmartins
 */
public class AddJmxSubsystemToHosts<S> extends AddSubsystemConfigurationTaskBuilder<S> {

    public AddJmxSubsystemToHosts() {
        super(ExtensionNames.JMX, new AddJMXSubsystemConfig<>());
    }

    public static class AddJMXSubsystemConfig<S> extends AddSubsystemConfigurationSubtaskBuilder<S> {

        private static final String EXPOSE_MODEL = "expose-model";
        private static final String RESOLVED = "resolved";
        private static final String EXPRESSION = "expression";
        private static final String REMOTING_CONNECTOR = "remoting-connector";
        private static final String JMX = "jmx";

        protected AddJMXSubsystemConfig() {
            super(SubsystemNames.JMX);
        }

        @Override
        protected void addConfiguration(ResourceBuildParameters<S, SubsystemConfiguration.Parent> params, ServerMigrationTaskName taskName, TaskContext context) {
            super.addConfiguration(params, taskName, context);
            // add jmx subsystem default config
            /*
            <profile>
        <subsystem xmlns="urn:jboss:domain:jmx:1.3">
            <expose-resolved-model/>
            <expose-expression-model/>
            <remoting-connector/>
        </subsystem>
    </profile>
             */
            final ManageableServerConfiguration serverConfiguration = params.getServerConfiguration();
            final PathAddress subsystemPathAddress = params.getResource().getSubsystemConfigurationPathAddress(getSubsystem());
            final ModelNode exposeResolvedModelAddOperation = Util.createAddOperation(subsystemPathAddress.append(PathElement.pathElement(EXPOSE_MODEL, RESOLVED)));
            serverConfiguration.executeManagementOperation(exposeResolvedModelAddOperation);
            final ModelNode exposeExpressionModelAddOperation = Util.createAddOperation(subsystemPathAddress.append(PathElement.pathElement(EXPOSE_MODEL, EXPRESSION)));
            serverConfiguration.executeManagementOperation(exposeExpressionModelAddOperation);
            final ModelNode remotingConnectorAddOperation = Util.createAddOperation(subsystemPathAddress.append(PathElement.pathElement(REMOTING_CONNECTOR, JMX)));
            serverConfiguration.executeManagementOperation(remotingConnectorAddOperation);
        }
    }
}
