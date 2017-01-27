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

package org.jboss.migration.wfly10.config.task.management.socketbinding;

import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.wfly10.config.management.SocketBindingResource;
import org.jboss.migration.wfly10.config.task.management.ManageableResourceLeafTask;

/**
 * @author emmartins
 */
public class SocketBindingResourceLeafTask<S> extends ManageableResourceLeafTask<S, SocketBindingResource> {

    private SocketBindingResourceLeafTask(Builder<S> builder, S source, SocketBindingResource resource) {
        super(builder, source, resource);
    }

    public interface Runnable<S> extends ManageableResourceLeafTask.Runnable<S, SocketBindingResource> {
    }

    public static class Builder<S> extends ManageableResourceLeafTask.Builder<S, SocketBindingResource> {

        public Builder(ServerMigrationTaskName name, Runnable<S> runnable) {
            super(name, runnable);
        }

        @Override
        public ServerMigrationTask build(S source, SocketBindingResource resource) {
            return new SocketBindingResourceLeafTask<>(this, source, resource);
        }
    }

}
