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

package org.jboss.migration.wfly10.config.task.module;

import org.jboss.migration.core.ServerMigrationFailureException;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.jboss.ModuleIdentifier;
import org.jboss.migration.core.jboss.ModulesMigrationTask;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemNames;

import javax.xml.stream.XMLStreamReader;
import java.io.IOException;

/**
 * Finds global modules referenced by EE subsystem configs.
 * @author emmartins
 */
public class EEGlobalModulesFinder implements ConfigurationModulesMigrationTaskFactory.ModulesFinder {
    @Override
    public String getElementLocalName() {
        return "module";
    }

    @Override
    public void processElement(XMLStreamReader reader, ModulesMigrationTask.ModuleMigrator moduleMigrator, TaskContext context) throws ServerMigrationFailureException {
        final String namespaceURI = reader.getNamespaceURI();
        if (namespaceURI == null || !namespaceURI.startsWith("urn:jboss:domain:"+ SubsystemNames.EE)) {
            return;
        }
        final String name = reader.getAttributeValue(null, "name");
        String slot = reader.getAttributeValue(null, "slot");
        if (slot == null) {
            slot = "main";
        }
        moduleMigrator.migrateModule(ModuleIdentifier.create(name, slot), "EE Subsystem's Global Module", context);
    }
}
