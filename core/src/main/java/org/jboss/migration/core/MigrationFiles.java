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

import org.jboss.migration.core.logger.ServerMigrationLogger;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

/**
 * The migration files.
 * @author emmartins
 */
public class MigrationFiles {

    private static final CopyOption[] COPY_OPTIONS = {
            StandardCopyOption.REPLACE_EXISTING
    };

    private final Map<Path, Path> copiedFiles;

    MigrationFiles() {
        this.copiedFiles = new HashMap<>();
    }

    /**
     * Copy a file.
     * @param source the file's path
     * @param target the file copy's path
     * @throws IllegalArgumentException if the source's file does not exists
     * @throws IllegalStateException if the target's path was used in a previous file copy, with a different source
     * @throws IOException if the file copy failed
     */
    public synchronized void copy(final Path source, final Path target) throws IllegalArgumentException, IOException {
        // check source file exists
        if (!Files.exists(source)) {
            throw ServerMigrationLogger.ROOT_LOGGER.sourceFileDoesNotExists(source);
        }
        final Path existentCopySource = copiedFiles.get(target);
        if (existentCopySource != null) {
            ServerMigrationLogger.ROOT_LOGGER.debugf("Skipping previously copied file %s", source);
            return;
        } else {
            if (Files.exists(target)) {
                Files.walkFileTree(target, new BackupVisitor(target));
            }
            Files.createDirectories(target.getParent());
            Files.walkFileTree(source, new CopyVisitor(source, target, copiedFiles));
        }
    }

    static class BackupVisitor extends SimpleFileVisitor<Path> {

        private final Path source;
        private final Path backup;

        BackupVisitor(Path source) {
            this.source = source;
            this.backup = source.resolveSibling(source.getFileName().toString()+".beforeMigration");
        }

        @Override
        public FileVisitResult preVisitDirectory(Path sourcePath, BasicFileAttributes attrs) throws IOException {
            final Path backupPath = getBackupPath(sourcePath);
            Files.move(sourcePath, backupPath, COPY_OPTIONS);
            return Files.newDirectoryStream(backupPath).iterator().hasNext() ? SKIP_SUBTREE : CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path sourcePath, BasicFileAttributes attrs) throws IOException {
            final Path backupPath = getBackupPath(sourcePath);
            Files.move(sourcePath, backupPath, COPY_OPTIONS);
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path sourcePath, IOException exc) throws IOException {
            final Path backupPath = getBackupPath(sourcePath);
            FileTime time = Files.getLastModifiedTime(sourcePath);
            Files.setLastModifiedTime(backupPath, time);
            return CONTINUE;
        }

        private Path getBackupPath(Path sourcePath) {
            return backup.resolve(source.relativize(sourcePath));
        }
    }

    static class CopyVisitor extends SimpleFileVisitor<Path> {

        private final Map<Path, Path> copiedFiles;
        private final Path source;
        private final Path target;

        CopyVisitor(Path source, Path target, Map<Path, Path> copiedFiles) {
            this.source = source;
            this.target = target;
            this.copiedFiles = copiedFiles;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return copy(dir);
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            return copy(file);
        }

        private Path getTargetPath(Path sourceFile) {
            return target.resolve(source.relativize(sourceFile));
        }

        private synchronized FileVisitResult copy(Path sourcePath) throws IOException {
            final Path targetPath = getTargetPath(sourcePath);
            final Path previousSourcePath = copiedFiles.put(targetPath, sourcePath);
            if (previousSourcePath != null) {
                if (previousSourcePath.equals(sourcePath)) {
                    return CONTINUE;
                } else {
                    throw new IOException("Target "+targetPath+" previously copied and sources mismatch: new = "+sourcePath+", previous = "+previousSourcePath);
                }
            }
            ServerMigrationLogger.ROOT_LOGGER.tracef("Copying file %s", targetPath);
            Files.copy(sourcePath, targetPath, COPY_OPTIONS);
            ServerMigrationLogger.ROOT_LOGGER.debugf("File %s copied to %s.", sourcePath, targetPath);
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            final Path targetDir = getTargetPath(dir);
            FileTime time = Files.getLastModifiedTime(dir);
            Files.setLastModifiedTime(targetDir, time);
            return CONTINUE;
        }
    }
}