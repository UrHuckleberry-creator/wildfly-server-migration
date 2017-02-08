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

package org.jboss.migration.eap6.to.eap7.tasks;

import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemConfigurations;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemNames;
import org.jboss.migration.wfly10.config.task.subsystem.ejb3.AddInfinispanPassivationStoreAndDistributableCache;
import org.jboss.migration.wfly10.config.task.subsystem.ejb3.DefinePassivationDisabledCacheRef;
import org.jboss.migration.wfly10.config.task.subsystem.ejb3.RefHttpRemotingConnectorInEJB3Remote;

/**
 * @author emmartins
 */
public class EAP6_4ToEAP7_0UpdateEJB3Subsystem<S> extends UpdateSubsystemConfigurations<S> {
    public EAP6_4ToEAP7_0UpdateEJB3Subsystem() {
        super(SubsystemNames.EJB3,
                new RefHttpRemotingConnectorInEJB3Remote<>(),
                new DefinePassivationDisabledCacheRef<>(),
                new AddInfinispanPassivationStoreAndDistributableCache<>());
    }
}
