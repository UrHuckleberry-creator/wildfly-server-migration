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

package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.migration.wfly10.WildFlyServer10;
import org.jboss.migration.wfly10.config.management.*;
import org.jboss.migration.wfly10.config.task.ServerConfigurationMigration;
import org.wildfly.core.embedded.EmbeddedProcessFactory;
import org.wildfly.core.embedded.EmbeddedProcessStartException;
import org.wildfly.core.embedded.HostController;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author emmartins
 */
public class EmbeddedHostControllerConfiguration extends AbstractManageableServerConfiguration implements HostControllerConfiguration {

    private final String domainConfig;
    private final String hostConfig;
    private HostController hostController;
    private final DeploymentsManagement deploymentsManagement;
    private final ExtensionsManagement extensionsManagement;
    private final InterfacesManagement interfacesManagement;
    private final HostsManagement hostsManagement;
    private final ProfilesManagement profilesManagement;
    private final ServerGroupsManagement serverGroupsManagement;
    private final SocketBindingGroupsManagement socketBindingGroupsManagement;
    private final SystemPropertiesManagement systemPropertiesManagement;

    protected EmbeddedHostControllerConfiguration(String domainConfig, String hostConfig, WildFlyServer10 server) {
        super(server);
        this.domainConfig = domainConfig;
        this.extensionsManagement = new ExtensionsManagementImpl(null, this) {
            @Override
            public Set<String> getSubsystems() throws IOException {
                Set<String> subsystems = new HashSet<>();
                for (String profile : getProfilesManagement().getResourceNames()) {
                    subsystems.addAll(profilesManagement.getProfileManagement(profile).getSubsystemsManagement().getResourceNames());
                }
                return subsystems;
            }
        };
        this.hostConfig = hostConfig;
        this.deploymentsManagement = new DeploymentsManagementImpl(null, this);
        this.hostsManagement = new HostsManagementImpl(null, this);
        this.profilesManagement = new ProfilesManagementImpl(null, this);
        this.serverGroupsManagement = new ServerGroupsManagementImpl(null, this);
        this.interfacesManagement = new InterfacesManagementImpl(null, this);
        this.socketBindingGroupsManagement = new SocketBindingGroupsManagementImpl(null, this);
        this.systemPropertiesManagement = new SystemPropertiesManagementImpl(null, this);
    }

    @Override
    protected ModelControllerClient startConfiguration() {

        final List<String> cmds = new ArrayList<>();
        if (domainConfig != null) {
            cmds.add("--domain-config="+ domainConfig);
        }
        if (hostConfig != null) {
            cmds.add("--host-config="+ hostConfig);
        }
        cmds.add("--admin-only");
        final String[] systemPackages = {"org.jboss.logmanager"};
        hostController = EmbeddedProcessFactory.createHostController(getServer().getBaseDir().toString(), null, systemPackages, cmds.toArray(new String[cmds.size()]));
        try {
            hostController.start();
        } catch (EmbeddedProcessStartException e) {
            throw new RuntimeException(e);
        }
        return hostController.getModelControllerClient();
    }

    @Override
    protected void stopConfiguration() {
        if (hostConfig == null) {
            writeConfiguration();
        }
        hostController.stop();
        hostController = null;
    }

    public DeploymentsManagement getDeploymentsManagement() {
        return deploymentsManagement;
    }

    @Override
    public ExtensionsManagement getExtensionsManagement() {
        return extensionsManagement;
    }

    public HostsManagement getHostsManagement() {
        return hostsManagement;
    }

    @Override
    public InterfacesManagement getInterfacesManagement() {
        return interfacesManagement;
    }

    @Override
    public ProfilesManagement getProfilesManagement() {
        return profilesManagement;
    }

    @Override
    public SocketBindingGroupsManagement getSocketBindingGroupsManagement() {
        return socketBindingGroupsManagement;
    }

    @Override
    public SystemPropertiesManagement getSystemPropertiesManagement() {
        return systemPropertiesManagement;
    }

    @Override
    public ServerGroupsManagement getServerGroupsManagement() {
        return serverGroupsManagement;
    }

    public static class DomainConfigFileMigrationFactory implements ServerConfigurationMigration.ManageableConfigurationProvider {
        @Override
        public HostControllerConfiguration getManageableConfiguration(Path configFile, WildFlyServer10 server) {
            return new EmbeddedHostControllerConfiguration(configFile.getFileName().toString(), null, server);
        }
    }

    public static class HostConfigFileMigrationFactory implements ServerConfigurationMigration.ManageableConfigurationProvider {
        @Override
        public HostControllerConfiguration getManageableConfiguration(Path configFile, WildFlyServer10 server) {
            return new EmbeddedHostControllerConfiguration(null, configFile.getFileName().toString(), server);
        }
    }

    @Override
    public <C extends ManageableNode> List<C> findChildren(Select<C> select) throws IOException {
            List<C> result = super.findChildren(select);
            /*
            DeploymentsManagement deploymentsManagement;
    private final ExtensionsManagement extensionsManagement;
    private final InterfacesManagement interfacesManagement;
    private final HostsManagement hostsManagement;
    private final ProfilesManagement profilesManagement;
    private final ServerGroupsManagement serverGroupsManagement;
    private final SocketBindingGroupsManagement socketBindingGroupsManagement;
    private final SystemPropertiesManagement systemPropertiesManagement;
             */


            if (select.getType().isInstance(ManageableResource.class)) {
                if (select.getType() == ServerGroupManagement.class) {
                    for (String s : (Set<String>) serverGroupsManagement.getResourceNames()) {
                        ServerGroupManagement serverGroupManagement = serverGroupsManagement.getServerGroupManagement(s);
                        C c = (C) serverGroupManagement;
                        if (select.test(c)) {
                            result.add(c);
                        }
                    }
                } else if (select.getType() == ProfileManagement.class) {
                    for (String s : (Set<String>) profilesManagement.getResourceNames()) {
                        ProfileManagement profileManagement = profilesManagement.getProfileManagement(s);
                        C c = (C) profileManagement;
                        if (select.test(c)) {
                            result.add(c);
                        }
                    }
                }
            } else if (select.getType().isInstance(ManageableResources.class)) {
                if (select.getType() == DeploymentsManagement.class) {
                    C c = (C) deploymentsManagement;
                    if (select.test(c)) {
                        result.add(c);
                    }
                } else if (select.getType() == HostsManagement.class) {
                    C c = (C) hostsManagement;
                    if (select.test(c)) {
                        result.add(c);
                    }
                } else if (select.getType() == SubsystemsManagement.class) {
                    for (String p : getProfilesManagement().getResourceNames()) {
                        ProfileManagement profileManagement = getProfilesManagement().getProfileManagement(p);
                        C c = (C) profileManagement.getSubsystemsManagement();
                        if (select.test(c)) {
                            result.add(c);
                        }
                    }
                } else if (select.getType() == JVMsManagement.class) {
                    for (String p : getServerGroupsManagement().getResourceNames()) {
                        ServerGroupManagement serverGroupManagement = getServerGroupsManagement().;
                        C c = (C) profileManagement.getSubsystemsManagement();
                        if (select.test(c)) {
                            result.add(c);
                        }
                    }
                }
            }
            return result;
    }
}
