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

package org.jboss.migration.core.task.component2;

import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author emmartins
 */
public abstract class ComponentTask implements ServerMigrationTask {

    private final ServerMigrationTaskName name;
    private final TaskRunnable taskRunnable;

    protected ComponentTask(ServerMigrationTaskName name, TaskRunnable taskRunnable) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(taskRunnable);
        this.name = name;
        this.taskRunnable = taskRunnable;
    }

    @Override
    public ServerMigrationTaskName getName() {
        return name;
    }

    @Override
    public ServerMigrationTaskResult run(TaskContext context) {
        return taskRunnable.run(context);
    }

    public interface Builder<P extends BuildParameters, T extends Builder<P, T>> {

        default T name(ServerMigrationTaskName name) {
            return name(parameters -> name);
        }

        T name(TaskNameBuilder<? super P> builder);

        /*
        default <Q extends Params> T name(ParamsMapper<P, Q> paramsConverter, NameFactory<? super Q> qNameFactory) {
            final NameFactory<P> pNameFactory = params -> qNameFactory.newInstance(paramsConverter.apply(params));
            return name(pNameFactory);
        }
    */
        default T skipPolicy(TaskSkipPolicy skipPolicy) {
            return skipPolicy((parameters, name) -> skipPolicy);
        }

        T skipPolicy(TaskSkipPolicy.Builder<? super P> builder);

        /*
        default <Q extends Params> T skipPolicy(ParamsMapper<P, Q> paramsConverter, SkipPolicyBuilder<? super Q> q) {
            final SkipPolicyBuilder<P> p = (params, name, context) -> q.isSkipped(paramsConverter.apply(params), name, context);
            return skipPolicy(p);
        }
    */
        default T beforeRun(BeforeTaskRun beforeRun) {
            return beforeRun((parameters, name) -> beforeRun);
        }

        T beforeRun(BeforeTaskRun.Builder<? super P> builder);

    /*
    default <Q extends Params> T beforeRun(ParamsMapper<P, Q> paramsConverter, BeforeTaskRun<? super Q> q) {
        final BeforeTaskRun<P> p = (params, name, context) -> q.beforeRun(paramsConverter.apply(params), name, context);
        return beforeRun(p);
    }*/

        default T run(TaskRunnable runnable) {
            return run((params, name) -> runnable);
        }

        T run(TaskRunnable.Builder<? super P> builder);

        default <Q extends BuildParameters> T run(BuildParameters.Mapper<P, Q> parametersMapper, TaskRunnable.Builder<? super Q> q) {
            return run(TaskRunnable.Adapters.of(parametersMapper, q));
        }

        /*
        default <Q extends Params> T run(ParamsMapper<P, Q> paramsConverter, RunnableFactory<? super Q> q) {
            final RunnableFactory<P> p = params -> q.newInstance(paramsConverter.apply(params));
            return run(p);
        }
    */
        default T afterRun(AfterTaskRun afterRun) {
            return afterRun((parameters, name) -> afterRun);
        }

        T afterRun(AfterTaskRun.Builder<? super P> builder);

        default <P1 extends BuildParameters> TaskRunnable.Builder<P1> toRunnableBuilder(BuildParameters.Mapper<P1, P> mapper) {
            final Builder<P, ?> builder = this;
            return (p1, taskName) -> {
                final Set<P> pSet = new HashSet<>();
                for (P p : mapper.apply(p1)) {
                    pSet.add(p);
                }
                return context -> {
                    for(P p : pSet) {
                        context.execute(builder.build(p));
                    }
                    return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
                };
            };
        }
    /*
    default <Q extends Params> T afterRun(ParamsMapper<P, Q> paramsConverter, AfterRun<? super Q> q) {
        final AfterRun<P> p = (params, name, context) -> paramsConverter.apply(params)q.afterRun(paramsConverter.apply(params), name, context);
        return afterRun(p);
    }
    */

        T clone();

        <P1 extends P> ServerMigrationTask build(P1 params);
    }

    protected static abstract class AbstractBuilder <P extends BuildParameters, T extends AbstractBuilder<P, T>> implements Builder<P, T> {

        private TaskNameBuilder<? super P> taskNameBuilder;
        private TaskSkipPolicy.Builder<? super P> skipPolicyBuilder = TaskSkipPolicy.Builders.skipIfDefaultSkipPropertyIsSet();
        private BeforeTaskRun.Builder<? super P> beforeRunBuilder;
        private AfterTaskRun.Builder<? super P> afterRunBuilder;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(AbstractBuilder<P, ?> other) {
            Objects.requireNonNull(other);
            this.taskNameBuilder = other.taskNameBuilder;
            this.skipPolicyBuilder = other.skipPolicyBuilder;
            this.beforeRunBuilder = other.beforeRunBuilder;
            this.afterRunBuilder = other.afterRunBuilder;
        }

        @Override
        public T name(TaskNameBuilder<? super P> builder) {
            this.taskNameBuilder = builder;
            return getThis();
        }

        @Override
        public T skipPolicy(TaskSkipPolicy.Builder<? super P> builder) {
            this.skipPolicyBuilder = builder;
            return getThis();
        }

        @Override
        public T beforeRun(BeforeTaskRun.Builder<? super P> builder) {
            this.beforeRunBuilder = builder;
            return getThis();
        }

        @Override
        public T afterRun(AfterTaskRun.Builder<? super P> builder) {
            this.afterRunBuilder = builder;
            return getThis();
        }

        protected ServerMigrationTaskName buildName(P parameters) {
            Objects.requireNonNull(taskNameBuilder);
            return taskNameBuilder.build(parameters);
        }

        protected TaskRunnable buildRunnable(P parameters, ServerMigrationTaskName taskName) {
        /*
        final TaskRunnable.Builder<? super P> runnableBuilder = getRunnableBuilder();
        Objects.requireNonNull(runnableBuilder);
        final TaskSkipPolicy.Builder<? super P> skipPolicyBuilder = this.skipPolicyBuilder;
        final BeforeTaskRun.Builder<? super P> beforeRunBuilder = this.beforeRunBuilder;
        final AfterTaskRun.Builder<? super P> afterRunBuilder = this.afterRunBuilder;
        */
            final TaskRunnable.Builder<? super P> runnableBuilder = getRunnableBuilder();
            Objects.requireNonNull(runnableBuilder);
            final TaskRunnable runnable = runnableBuilder.build(parameters, taskName);
            final TaskSkipPolicy skipPolicy = skipPolicyBuilder != null ? skipPolicyBuilder.build(parameters, taskName) : null;
            final BeforeTaskRun beforeRun = beforeRunBuilder != null ? beforeRunBuilder.build(parameters, taskName) : null;
            final AfterTaskRun afterRun = afterRunBuilder != null ? afterRunBuilder.build(parameters, taskName) : null;
            return context -> {
                if (skipPolicy != null && skipPolicy.isSkipped(context)) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                if (beforeRun != null) {
                    beforeRun.beforeRun(context);
                }
                final ServerMigrationTaskResult result = runnable.run(context);
                if (afterRun != null) {
                    afterRun.afterRun(context);
                }
                return result;
            };
        }

        @Override
        public <P1 extends P> ServerMigrationTask build(P1 params) {
            final ServerMigrationTaskName taskName = buildName(params);
            return buildTask(taskName, buildRunnable(params, taskName));
        }

        protected abstract T getThis();
        protected abstract TaskRunnable.Builder<? super P> getRunnableBuilder();
        protected abstract ServerMigrationTask buildTask(ServerMigrationTaskName name, TaskRunnable taskRunnable);
    }
}
