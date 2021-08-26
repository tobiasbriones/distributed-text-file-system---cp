/*
 * Copyright (c) 2021 Tobias Briones. All rights reserved.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 * This file is part of Course Project at UNAH-MM545: Distributed Text File
 * System.
 *
 * This source code is licensed under the BSD-3-Clause License found in the
 * LICENSE file in the root directory of this source tree or at
 * https://opensource.org/licenses/BSD-3-Clause.
 */

package com.github.tobiasbriones.cp.rmifilesystem.model.io.node;

import com.github.tobiasbriones.cp.rmifilesystem.model.io.File;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Describes the file system which consists of a root node, the node statuses
 * regarding the remote counterpart of each node and a local config regarding
 * the local counterpart of each node. With this object, a node can be seen as a
 * local node or a remote node as it integrates the domain node data structure
 * and the contract between a local and remote file system.
 *
 * @author Tobias Briones
 */
public final class FileSystem implements Serializable {
    @Serial
    private static final long serialVersionUID = -2650303079113294871L;
    private static final int INITIAL_FILES_CAPACITY = 25;
    private final DirectoryNode root;
    private final Map<File, Status> statuses;

    public record Status(File file, boolean isInvalid) implements Serializable {}

    public record LastUpdateStatus(File file, ZonedDateTime time) implements Serializable {
        public static final ZoneId zoneId = ZoneId.of("US/Eastern");

        public static LastUpdateStatus of(File file) {
            final ZonedDateTime time = ZonedDateTime.ofInstant(Instant.now(), zoneId);
            return new LastUpdateStatus(file, time);
        }

        public boolean isInvalid(LastUpdateStatus other) {
            return time.compareTo(other.time()) > 0;
        }
    }

    public FileSystem(DirectoryNode root) {
        this.root = root;
        statuses = new HashMap<>(INITIAL_FILES_CAPACITY);
    }

    public DirectoryNode getRoot() {
        return root;
    }

    public Optional<Status> getStatus(File file) {
        return Optional.ofNullable(statuses.get(file));
    }

    public void updateStatuses(Function<? super File, Status> mapper) {
        root.traverse(node -> {
            if (node instanceof FileNode fileNode) {
                final Status status = mapper.apply(fileNode.commonFile());

                updateStatus(status);
            }
        });
    }

    public void updateStatus(Status status) {
        statuses.put(status.file(), status);
    }
}