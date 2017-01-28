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

import org.jboss.migration.core.task.ServerMigrationTaskResult;

import java.util.List;

/**
 * @author emmartins
 */
public class CompositeRunnableFactory<P extends Parameters> implements RunnableFactory<P> {

    private final List<RunnableFactory<P>> factories;

    public CompositeRunnableFactory(List<RunnableFactory<P>> factories) {
        this.factories = factories;
    }

    @Override
    public Runnable newInstance(P parameters) {
        return (name, context) -> {
            for (RunnableFactory<P> factory : factories) {
                factory.newInstance(parameters).run(name, context);
            }
            return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
        };
    }
}
