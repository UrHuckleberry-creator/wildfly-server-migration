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

package org.jboss.migration.wfly10.config.task.management;

import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;

import java.util.Collection;

/**
 * @author emmartins
 */
public class StandaloneServerConfigurationTask<S> extends ManageableServerConfigurationTask<S, StandaloneServerConfiguration> {

    protected StandaloneServerConfigurationTask(BaseBuilder<S, StandaloneServerConfiguration, ?> builder, S source, Collection<? extends StandaloneServerConfiguration> resources) {
        super(builder, source, resources);
    }

    public interface SubtaskExecutor<S> extends ManageableServerConfigurationTask.SubtaskExecutor<S, StandaloneServerConfiguration> {
    }

    public static class Builder<S> extends ManageableServerConfigurationTask.BaseBuilder<S, StandaloneServerConfiguration, Builder<S>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        @Override
        public ServerMigrationTask build(S source, Collection<? extends StandaloneServerConfiguration> resources) {
            return new StandaloneServerConfigurationTask<>(this, source, resources);
        }
    }
}
