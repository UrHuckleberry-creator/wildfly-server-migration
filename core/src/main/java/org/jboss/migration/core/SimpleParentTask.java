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

package org.jboss.migration.core;

/**
 * A basic parent task.
 * @author emmartins
 */
public class SimpleParentTask extends ParentTask<TaskContext> {

    public SimpleParentTask(BaseBuilder<TaskContext, ?> builder, SubtaskExecutorContextFactory<TaskContext> subtaskExecutorContextFactory) {
        super(builder, subtaskExecutorContextFactory);
    }

    public static class Builder extends BaseBuilder<TaskContext, Builder> {

        public Builder(ServerMigrationTaskName name) {
            super(name);
        }

        public SimpleParentTask build() {
            return build(new SubtaskExecutorContextFactory() {
                @Override
                public TaskContext getSubtaskExecutorContext(TaskContext context) throws Exception {
                    return context;
                }
            });
        }

        @Override
        protected SimpleParentTask build(TaskContext context) {
            return build();
        }

        @Override
        protected SimpleParentTask build(SubtaskExecutorContextFactory<TaskContext> contextFactory) {
            return build();
        }
    }
}
