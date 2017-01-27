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

package org.jboss.migration.core.task.component;

/**
 * @author emmartins
 */
public class BasicComponentTask extends ComponentTask<BasicComponentTask> {

    private BasicComponentTask(Builder builder) {
        super(builder);
    }

    @Override
    protected BasicComponentTask getThis() {
        return this;
    }

    public static class Builder extends ComponentTask.Builder<BasicComponentTask, Builder> {

        public Builder(NameFactory<BasicComponentTask> nameFactory, ComponentTaskRunnable<BasicComponentTask> runnable) {
            super(nameFactory, runnable);
        }

        public Builder(Builder other) {
            super(other);
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public Builder clone() {
            return new Builder(this);
        }

        @Override
        public BasicComponentTask build() {
            return new BasicComponentTask(this);
        }
    }
}