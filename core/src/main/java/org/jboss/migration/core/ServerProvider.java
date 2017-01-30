/*
 * Copyright 2015 Red Hat, Inc.
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

import java.nio.file.Path;

/**
 * The provider for a {@link Server}.
 * @author emmartins
 */
public interface ServerProvider {

    /**
     * Retrieves a server from its base directory.
     * @param migrationName the migration server's name
     * @param baseDir the server's base directory.
     * @param migrationEnvironment
     * @return null if the specified base directory is not the base directory of the provider's server.
     * @throws ServerMigrationFailureException if the server failed to retrieve.
     */
    Server getServer(String migrationName, Path baseDir, MigrationEnvironment migrationEnvironment) throws ServerMigrationFailureException;

    /**
     * Retrieves the provider's name.
     * @return the provider's name
     */
    String getName();
}
