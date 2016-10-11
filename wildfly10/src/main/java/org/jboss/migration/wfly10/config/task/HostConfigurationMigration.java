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

package org.jboss.migration.wfly10.config.task;

import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.impl.EmbeddedHostControllerConfiguration;

/**
 * Host config migration.
 * @author emmartins
 */
public class HostConfigurationMigration<S> extends ServerConfigurationMigration<S, HostControllerConfiguration> {

    public static final String HOST = "host";

    protected HostConfigurationMigration(Builder builder) {
        super(builder);
    }

    public static class Builder<S> extends ServerConfigurationMigration.Builder<Builder, S, HostControllerConfiguration> {

        public Builder(XMLConfigurationProvider<S> xmlConfigurationProvider) {
            super(HOST, xmlConfigurationProvider);
            manageableConfigurationProvider(new EmbeddedHostControllerConfiguration.HostConfigFileMigrationFactory());
        }

        @Override
        public HostConfigurationMigration<S> build() {
            return new HostConfigurationMigration<>(this);
        }

        public Builder<S> addHostsMigration(final HostsMigration<S> hostsMigration) {
            return addManageableConfigurationSubtaskFactory(new ManageableConfigurationSubtaskFactory<S, HostControllerConfiguration>() {
                @Override
                public ServerMigrationTask getManageableConfigurationSubtask(S source, HostControllerConfiguration configuration) throws Exception {
                    return hostsMigration.getTask(source, configuration.getHostsManagement());
                }
            });
        }

        public Builder<S> addHostsMigration(final HostMigration<S> hostMigration) {
            return addHostsMigration(new HostsMigration.Builder<S>().addHostMigration(hostMigration).build());
        }
    }
}
