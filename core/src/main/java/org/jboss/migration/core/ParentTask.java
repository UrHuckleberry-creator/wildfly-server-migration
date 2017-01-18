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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link ServerMigrationTask} which delegates to subtask executors.
 * @author emmartins
 */
public abstract class ParentTask extends AbstractServerMigrationTask {

    protected final boolean succeedIfHasSuccessfulSubtasks;

    protected ParentTask(BaseBuilder builder) {
        super(builder);
        this.succeedIfHasSuccessfulSubtasks = builder.succeedIfHasSuccessfulSubtasks;
    }

    @Override
    protected ServerMigrationTaskResult runTask(TaskContext taskContext) throws Exception {
        runSubtasks(taskContext);
        return (!succeedIfHasSuccessfulSubtasks || taskContext.hasSucessfulSubtasks()) ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
    }

    protected abstract void runSubtasks(TaskContext context) throws Exception;

    /**
     *
     */
    public interface Subtasks {
        void run(TaskContext context) throws Exception;
    }

    /**
     * The parent task extensible builder.
     */
    protected static abstract class BaseBuilder<B extends BaseBuilder<B>> extends AbstractServerMigrationTask.Builder<B> {

        protected boolean succeedIfHasSuccessfulSubtasks = true;

        protected BaseBuilder(ServerMigrationTaskName name) {
            super(name);
        }

        public B succeedIfHasSuccessfulSubtasks() {
            succeedIfHasSuccessfulSubtasks = true;
            return (B) this;
        }

        public B succeedAlways() {
            succeedIfHasSuccessfulSubtasks = false;
            return (B) this;
        }

        public abstract B subtask(ServerMigrationTask subtask);

        public abstract B subtask(Subtasks subtasks);
    }

    /**
     *
     */
    public static class Builder extends BaseBuilder<Builder> {

        private static final Subtasks[] EMPTY = {};
        protected final List<Subtasks> subtasks;

        public Builder(ServerMigrationTaskName name) {
            super(name);
            this.subtasks = new ArrayList<>();
        }

        @Override
        public Builder subtask(final ServerMigrationTask subtask) {
            return subtask(new Subtasks() {
                @Override
                public void run(TaskContext context) throws Exception {
                    context.execute(subtask);
                }
            });
        }

        public Builder subtask(Subtasks subtasks) {
            this.subtasks.add(subtasks);
            return this;
        }

        public ParentTask build() {
            final Subtasks[] subtasks = this.subtasks.toArray(EMPTY);
            return new ParentTask(this) {
                @Override
                protected void runSubtasks(TaskContext context) throws Exception {
                    for (Subtasks subtask : subtasks) {
                        subtask.run(context);
                    }
                }
            };
        }
    }
}