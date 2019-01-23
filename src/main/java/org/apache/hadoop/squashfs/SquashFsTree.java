/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.squashfs;

import org.apache.hadoop.squashfs.inode.INodeType;
import org.apache.hadoop.squashfs.metadata.MetadataBlockRef;
import org.apache.hadoop.squashfs.metadata.MetadataWriter;
import org.apache.hadoop.squashfs.table.ExportTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SquashFsTree {

  private final Map<String, SquashFsEntry> map = new HashMap<>();

  private final AtomicInteger inodeAssignments = new AtomicInteger(0);
  private final Map<Integer, Set<SquashFsEntry>> inodeToEntry = new HashMap<>();
  private final MetadataWriter inodeWriter = new MetadataWriter();
  private final MetadataWriter dirWriter = new MetadataWriter();
  private final Map<Integer, MetadataBlockRef> visitedInodes = new TreeMap<>();

  private final SquashFsEntry root = new SquashFsEntry();
  private MetadataBlockRef rootInodeRef;

  SquashFsTree() {

  }

  void add(SquashFsEntry squashFsEntry) {
    SquashFsEntry prev = map.get(squashFsEntry.name);
    if (prev == null || (prev.synthetic && !squashFsEntry.synthetic)) {
      map.put(squashFsEntry.name, squashFsEntry);
    }
  }

  public SquashFsEntry getRoot() {
    return root;
  }

  void build() throws SquashFsException, IOException {
    for (Map.Entry<String, SquashFsEntry> squashFsEntry : map.entrySet()) {
      String name = squashFsEntry.getKey();
      String parent = name;
      while ((parent = parentName(parent)) != null) {
        SquashFsEntry p = map.get(parent);
        if (p == null || p.type != INodeType.BASIC_DIRECTORY) {
          throw new IllegalArgumentException(
              String.format("Parent '%s' not found for entry '%s'", parent,
                  name));
        }
      }

      String hardLinkTarget = squashFsEntry.getValue().hardlinkTarget;
      if (hardLinkTarget != null && !map.containsKey(hardLinkTarget)) {
        throw new IllegalArgumentException(
            String.format("Hardlink target '%s' not found for entry '%s'",
                hardLinkTarget, name));
      }

      // assign parent
      parent = parentName(name);
      if (parent == null) {
        root.children.add(squashFsEntry.getValue());
        squashFsEntry.getValue().parent = root;
      } else {
        SquashFsEntry parentEntry = map.get(parent);
        parentEntry.children.add(squashFsEntry.getValue());
        squashFsEntry.getValue().parent = parentEntry;
      }
    }

    // walk tree, sort entries and assign inodes
    root.sortChildren();

    root.assignInodes(map, inodeAssignments);
    root.assignHardlinkInodes(map, inodeToEntry);

    root.updateDirectoryLinkCounts();
    root.updateHardlinkInodeCounts(inodeToEntry);

    root.createInodes();
    root.createHardlinkInodes();

    rootInodeRef = root.writeMetadata(inodeWriter, dirWriter, visitedInodes);

    // make sure all inodes were visited
    if (visitedInodes.size() != root.inode.getInodeNumber()) {
      throw new SquashFsException(
          String.format("BUG: Visited inode count %d != actual inode count %d",
              visitedInodes.size(), root.inode.getInodeNumber()));
    }

    // make sure all inode numbers exist, from 1 to n
    List<Integer> allInodes =
        visitedInodes.keySet().stream().collect(Collectors.toList());
    if (allInodes.get(0).intValue() != 1) {
      throw new SquashFsException(
          String.format("BUG: First inode number %d != 1",
              allInodes.get(0).intValue()));
    }
    if (allInodes.get(allInodes.size() - 1).intValue() != allInodes.size()) {
      throw new SquashFsException(
          String.format("BUG: Last inode number %d != %d",
              allInodes.get(allInodes.size() - 1).intValue(),
              allInodes.size()));
    }
  }

  int getInodeCount() {
    return visitedInodes.size();
  }

  List<MetadataBlockRef> saveExportTable(MetadataWriter writer)
      throws IOException {

    List<MetadataBlockRef> exportRefs = new ArrayList<>();

    int index = 0;
    for (Map.Entry<Integer, MetadataBlockRef> entry : visitedInodes
        .entrySet()) {
      if (index % ExportTable.ENTRIES_PER_BLOCK == 0) {
        exportRefs.add(writer.getCurrentReference());
      }
      MetadataBlockRef metaRef = entry.getValue();

      long inodeRef = (((long) (metaRef.getLocation() & 0xffffffffL)) << 16) |
          (((long) metaRef.getOffset()) & 0xffffL);

      writer.writeLong(inodeRef);
      index++;
    }

    return exportRefs;
  }

  MetadataBlockRef getRootInodeRef() {
    return rootInodeRef;
  }

  MetadataWriter getINodeWriter() {
    return inodeWriter;
  }

  MetadataWriter getDirWriter() {
    return dirWriter;
  }

  private String parentName(String name) {
    int slash = name.lastIndexOf('/');
    if (slash == 0) {
      return null;
    }
    return name.substring(0, slash);
  }

}
