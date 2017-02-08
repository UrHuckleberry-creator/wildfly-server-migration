/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.migration.core;

import org.jboss.migration.core.env.MigrationEnvironment;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskExecution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The data collected from the server migration.
 * @author emmartins
 */
public class MigrationData {

    private final Server source;
    private final Server target;
    private final TaskExecution rootTask;
    private final List<TaskExecution> tasks;
    private final MigrationEnvironment migrationEnvironment;

    MigrationData(Server source, Server target, TaskExecution rootTask, MigrationEnvironment migrationEnvironment) {
        this.source = source;
        this.target = target;
        this.rootTask = rootTask;
        this.migrationEnvironment = migrationEnvironment;
        this.tasks = initTasks();
    }

    /**
     * Retrieves the migration source server.
     * @return the migration source server.
     */
    public Server getSource() {
        return source;
    }

    /**
     * Retrieves the migration target server.
     * @return the migration target server.
     */
    public Server getTarget() {
        return target;
    }

    /**
     * Retrieves the root task execution.
     * @return the root task execution
     */
    public TaskExecution getRootTask() {
        return rootTask;
    }

    /**
     * Retrieves the server migration environment.
     * @return the server migration environment
     */
    public MigrationEnvironment getServerMigrationEnvironment() {
        return rootTask.getServerMigrationContext().getMigrationEnvironment();
    }

    /**
     * Retrieves the migration environment.
     * @return the migration environment
     */
    public MigrationEnvironment getMigrationEnvironment() {
        return migrationEnvironment;
    }

    /**
     * Retrieves all tasks.
     * @return all tasks
     */
    public List<TaskExecution> getTasks() {
        return tasks;
    }

    /**
     * Retrieves the number of tasks with the specified status result.
     * @param status the status result
     * @return the number of tasks with the specified status result
     */
    public int getTaskCount(ServerMigrationTaskResult.Status status) {
        int count = 0;
        for (TaskExecution task : tasks) {
            if (task.getResult().getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    private List<TaskExecution> initTasks() {
        final List<TaskExecution> results = new ArrayList<>();
        results.add(getRootTask());
        addSubTasks(getRootTask(), results);
        return Collections.unmodifiableList(results);
    }

    private void addSubTasks(TaskExecution task, List<TaskExecution> tasks) {
        final List<TaskExecution> subtasks = task.getSubtasks();
        if (subtasks != null) {
            for (TaskExecution subtask : subtasks) {
                tasks.add(subtask);
                addSubTasks(subtask, tasks);
            }
        }
    }
}
