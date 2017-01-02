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
import java.util.List;

/**
 * An {@link ServerMigrationTask} which delegates to subtask executors.
 * @author emmartins
 */
public class AbstractParentTask<T extends TaskContext> extends AbstractServerMigrationTask {

    protected final boolean succeedOnlyIfHasSuccessfulSubtasks;
    protected final List<SubtaskExecutor<T>> subtasks;
    protected final SubtaskExecutorContextFactory<T> subtaskExecutorContextFactory;

    protected AbstractParentTask(BaseBuilder<T, ?> builder, SubtaskExecutorContextFactory<T> subtaskExecutorContextFactory) {
        super(builder);
        this.subtasks = builder.subtasks;
        this.succeedOnlyIfHasSuccessfulSubtasks = builder.succeedOnlyIfHasSuccessfulSubtasks;
        this.subtaskExecutorContextFactory = subtaskExecutorContextFactory;
    }

    @Override
    protected ServerMigrationTaskResult runTask(TaskContext taskContext) throws Exception {
        runSubtasks(subtaskExecutorContextFactory.getSubtaskExecutorContext(taskContext));
        return (!succeedOnlyIfHasSuccessfulSubtasks || taskContext.hasSucessfulSubtasks()) ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
    }

    protected void runSubtasks(T context) throws Exception {
        for (SubtaskExecutor<T> subtaskExecutor : subtasks) {
            subtaskExecutor.run(context);
        }
    }

    public interface SubtaskExecutor<T extends TaskContext> {
        void run(T context) throws Exception;
    }

    public interface SubtaskExecutorContextFactory<T extends TaskContext> {
        T getSubtaskExecutorContext(TaskContext context) throws Exception;
    }

    /**
     * The parent task builder.
     */
    public static abstract class BaseBuilder<T extends TaskContext, B extends BaseBuilder<T,B>> extends AbstractServerMigrationTask.Builder<B> {

        protected final List<SubtaskExecutor<T>> subtasks;
        protected boolean succeedOnlyIfHasSuccessfulSubtasks = true;

        protected BaseBuilder(ServerMigrationTaskName name) {
            super(name);
            subtasks = new ArrayList<>();
        }

        public B subtask(final ServerMigrationTask subtask) {
            return subtask(new SubtaskExecutor<T>() {
                @Override
                public void run(T context) throws Exception {
                    context.execute(subtask);
                }
            });
        }

        public B subtask(SubtaskExecutor<T> subtask) {
            subtasks.add(subtask);
            return (B) this;
        }

        public B subtask(final AbstractParentTask.BaseBuilder<T, ?> subtask) {
            return subtask(new SubtaskExecutor<T>() {
                @Override
                public void run(T context) throws Exception {
                    context.execute(subtask.build(context));
                }
            });
        }

        public B succeedOnlyIfHasSuccessfulSubtasks() {
            succeedOnlyIfHasSuccessfulSubtasks = true;
            return (B) this;
        }

        public B succeedAlways() {
            succeedOnlyIfHasSuccessfulSubtasks = false;
            return (B) this;
        }

        protected abstract AbstractParentTask<T> build(SubtaskExecutorContextFactory<T> contextFactory);

        protected abstract AbstractParentTask<T> build(T context);
    }

    public static class Builder extends BaseBuilder<TaskContext, Builder> {

        public Builder(ServerMigrationTaskName name) {
            super(name);
        }

        public AbstractParentTask<TaskContext> build() {
            return build(new SubtaskExecutorContextFactory() {
                @Override
                public TaskContext getSubtaskExecutorContext(TaskContext context) throws Exception {
                    return context;
                }
            });
        }

        @Override
        protected AbstractParentTask<TaskContext> build(TaskContext context) {
            return build();
        }

        @Override
        protected AbstractParentTask<TaskContext> build(SubtaskExecutorContextFactory<TaskContext> contextFactory) {
            return build();
        }
    }
}
