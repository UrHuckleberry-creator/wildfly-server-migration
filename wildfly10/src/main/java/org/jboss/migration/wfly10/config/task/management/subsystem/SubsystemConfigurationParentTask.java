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

import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.management.ManageableResourceTask;

import java.util.Collection;

/**
 * @author emmartins
 */
public class SubsystemConfigurationParentTask<S> extends ManageableResourceTask<S, SubsystemConfiguration.Parent> {

    protected SubsystemConfigurationParentTask(ManageableResourceTask.BaseBuilder<S, SubsystemConfiguration.Parent, ?> builder, S source, Collection<? extends SubsystemConfiguration.Parent> manageableResources) {
        super(builder, source, manageableResources);
    }

    public static abstract class BaseBuilder<S, B extends BaseBuilder<S, B>> extends ManageableResourceTask.BaseBuilder<S, SubsystemConfiguration.Parent, B> {
        public BaseBuilder(ServerMigrationTaskName taskName) {
            super(taskName);
        }
    }

    public static class Builder<S> extends BaseBuilder<S, Builder<S>> {
        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        @Override
        public ServerMigrationTask build(S source, Collection<? extends SubsystemConfiguration.Parent> resources) {
            return new SubsystemConfigurationParentTask<>(this, source, resources);
        }
    }
}