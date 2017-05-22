/*
 * Copyright 2016 Red Hat, Inc.
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
package org.jboss.migration.core.jboss;

import org.jboss.migration.core.Server;
import org.jboss.migration.core.logger.ServerMigrationLogger;

import java.util.ServiceLoader;

/**
 * Target's server migrations trough a service loader.
 * @author emmartins
 */
public class ServiceLoaderTargetJBossServerMigrations<T extends TargetJBossServerMigrationProvider> implements TargetJBossServerMigrations {

    private final ServiceLoader<T> serviceLoader;

    public ServiceLoaderTargetJBossServerMigrations(ServiceLoader<T> serviceLoader) {
        this.serviceLoader = serviceLoader;
    }

    @Override
    public TargetJBossServerMigration getMigrationFrom(Server sourceServer) {
        ServerMigrationLogger.ROOT_LOGGER.debugf("Retrieving server migration for source %s", sourceServer.getClass());
        for (TargetJBossServerMigrationProvider serverMigrationProvider : serviceLoader) {
            if (serverMigrationProvider.getSourceType().isInstance(sourceServer)) {
                ServerMigrationLogger.ROOT_LOGGER.debugf("Found server migration for source %s: %s", sourceServer.getClass(), serverMigrationProvider.getClass());
                return serverMigrationProvider.getServerMigration();
            }
        }
        ServerMigrationLogger.ROOT_LOGGER.debugf("Failed to retrieve server migration for source %s", sourceServer.getClass());
        return null;
    }
}
