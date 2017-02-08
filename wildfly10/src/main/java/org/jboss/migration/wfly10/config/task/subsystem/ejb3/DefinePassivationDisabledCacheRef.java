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
package org.jboss.migration.wfly10.config.task.subsystem.ejb3;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemResourceSubtaskBuilder;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * A task which defines EJB3 subsystem's 'passivation-disabled-cache-ref' attribute .
 * @author emmartins
 */
public class DefinePassivationDisabledCacheRef<S> extends UpdateSubsystemResourceSubtaskBuilder<S> {

    public static final DefinePassivationDisabledCacheRef INSTANCE = new DefinePassivationDisabledCacheRef();

    public static final String TASK_NAME = "setup-default-sfsb-passivation-disabled-cache";

    public DefinePassivationDisabledCacheRef() {
        super(TASK_NAME);
    }

    private static final String DEFAULT_SFSB_CACHE_ATTR_NAME = "default-sfsb-cache";
    private static final String DEFAULT_SFSB_PASSIVATION_DISABLED_CACHE_ATTR_NAME = "default-sfsb-passivation-disabled-cache";

    @Override
    protected ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemResource subsystemResource, TaskContext context, TaskEnvironment taskEnvironment) {
        if (!config.hasDefined(DEFAULT_SFSB_CACHE_ATTR_NAME) || config.hasDefined(DEFAULT_SFSB_PASSIVATION_DISABLED_CACHE_ATTR_NAME)) {
            return ServerMigrationTaskResult.SKIPPED;
        }
        final PathAddress subsystemPathAddress = subsystemResource.getResourcePathAddress();
        final ManageableServerConfiguration configurationManagement = subsystemResource.getServerConfiguration();
        // /subsystem=ejb3:write-attribute(name=default-sfsb-passivation-disabled-cache,value=defaultSFSBCache)
        final String defaultSFSBCache = config.get(DEFAULT_SFSB_CACHE_ATTR_NAME).asString();
        final ModelNode op = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, subsystemPathAddress);
        op.get(NAME).set(DEFAULT_SFSB_PASSIVATION_DISABLED_CACHE_ATTR_NAME);
        op.get(VALUE).set(defaultSFSBCache);
        configurationManagement.executeManagementOperation(op);
        context.getLogger().infof("EJB3 subsystem's 'default-sfsb-passivation-disabled-cache' attribute set to %s.", defaultSFSBCache);
        return ServerMigrationTaskResult.SUCCESS;
    }
}