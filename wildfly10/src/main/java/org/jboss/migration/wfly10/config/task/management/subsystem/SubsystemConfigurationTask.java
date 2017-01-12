package org.jboss.migration.wfly10.config.task.management.subsystem;

import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.AbstractServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;
import org.jboss.migration.wfly10.config.task.subsystem.EnvironmentProperties;

import java.io.IOException;

/**
 * @author emmartins
 */
public class SubsystemConfigurationTask<S> extends SubsystemsConfigurationTask<S> {

    protected SubsystemConfigurationTask(BaseBuilder<S, ?> builder, S source, SubsystemsManagement... resourceManagements) {
        super(builder, source, resourceManagements);
    }

    public interface Context<S> {
        String getExtensionModule();
        ManageableServerConfiguration getServerConfiguration();
        S getServerConfigurationSource();
        String getSubsystemName();
        ModelNode getSubsystemConfiguration() throws IOException;
        String getSubsystemConfigurationName();
        PathAddress getSubsystemConfigurationPathAddress();
        SubsystemsManagement getSubsystemsConfiguration();
        void removeSubsystemConfiguration() throws IOException;
    }

    public interface Subtask<S> {
        ServerMigrationTaskName getName(Context<S> parentContext);
        ServerMigrationTaskResult run(Context<S> parentContext, TaskContext taskContext, TaskEnvironment taskEnvironment) throws Exception;
    }

    protected static abstract class BaseBuilder<S, B extends BaseBuilder<S, B>> extends SubsystemsConfigurationTask.BaseBuilder<S, B> {
        private final String extension;
        private final String subsystem;

        protected BaseBuilder(String extension, String subsystem, ServerMigrationTaskName taskName) {
            super(taskName);
            this.extension = extension;
            this.subsystem = subsystem;
        }

        public B subtask(final Subtask<S> subtask) {
            return subtask(new SubsystemsConfigurationSubtasks<S>() {
                @Override
                public void executeSubtasks(S source, SubsystemsManagement resourceManagement, TaskContext context) throws Exception {
                    final Context<S> parentContext = new ContextImpl<>(extension, resourceManagement, source, subsystem);
                    final ServerMigrationTaskName subtaskName = subtask.getName(parentContext);
                    if (subtaskName != null) {
                        final TaskEnvironment taskEnvironment = new TaskEnvironment(context.getServerMigrationContext().getMigrationEnvironment(), EnvironmentProperties.getSubsystemSubtaskPropertiesPrefix(subsystem, subtaskName.getName()));
                        final AbstractServerMigrationTask.Builder builder = new AbstractServerMigrationTask.Builder(subtaskName)
                                .skipper(new AbstractServerMigrationTask.Skipper() {
                                    @Override
                                    public boolean isSkipped(TaskContext context) {
                                        return taskEnvironment.isSkippedByEnvironment();
                                    }
                                });
                        final ServerMigrationTask task = new AbstractServerMigrationTask(builder) {
                            @Override
                            protected ServerMigrationTaskResult runTask(TaskContext taskContext) throws Exception {
                                return subtask.run(parentContext, taskContext, taskEnvironment);
                            }
                        };
                        context.execute(task);
                    }
                }
            });
        }
    }

    public static class Builder<S> extends BaseBuilder<S, Builder<S>> {
        public Builder(String extension, String subsystem, ServerMigrationTaskName taskName) {
            super(extension, subsystem, taskName);
        }
        @Override
        public ServerMigrationTask build(S source, SubsystemsManagement... resourceManagements) {
            return new SubsystemConfigurationTask<>(this, source, resourceManagements);
        }
    }

    private static class ContextImpl<S> implements Context<S> {

        final String extensionModule;
        final SubsystemsManagement resourceConfigurations;
        final S serverConfigurationSource;
        final String subsystemName;

        ContextImpl(String extension, SubsystemsManagement resourceConfigurations, S serverConfigurationSource, String subsystem) {
            this.extensionModule = extension;
            this.resourceConfigurations = resourceConfigurations;
            this.serverConfigurationSource = serverConfigurationSource;
            this.subsystemName = subsystem;
        }

        @Override
        public String getExtensionModule() {
            return extensionModule;
        }

        @Override
        public ManageableServerConfiguration getServerConfiguration() {
            return resourceConfigurations.getServerConfiguration();
        }

        @Override
        public S getServerConfigurationSource() {
            return serverConfigurationSource;
        }

        @Override
        public ModelNode getSubsystemConfiguration() throws IOException {
            return resourceConfigurations.getResource(subsystemName);
        }

        @Override
        public String getSubsystemConfigurationName() {
            return getSubsystemConfigurationPathAddress().toCLIStyleString();
        }

        @Override
        public PathAddress getSubsystemConfigurationPathAddress() {
            return getSubsystemsConfiguration().getResourcePathAddress(subsystemName);
        }

        @Override
        public SubsystemsManagement getSubsystemsConfiguration() {
            return resourceConfigurations;
        }

        @Override
        public String getSubsystemName() {
            return subsystemName;
        }

        @Override
        public void removeSubsystemConfiguration() throws IOException {
            getSubsystemsConfiguration().removeResource(getSubsystemName());
        }
    }
}
