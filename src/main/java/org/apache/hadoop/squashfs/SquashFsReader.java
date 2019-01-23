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

import org.apache.hadoop.squashfs.data.DataBlockCache;
import org.apache.hadoop.squashfs.directory.DirectoryEntry;
import org.apache.hadoop.squashfs.inode.DirectoryINode;
import org.apache.hadoop.squashfs.inode.INode;
import org.apache.hadoop.squashfs.inode.INodeRef;
import org.apache.hadoop.squashfs.io.MappedFile;
import org.apache.hadoop.squashfs.metadata.MetadataBlockCache;
import org.apache.hadoop.squashfs.metadata.MetadataBlockReader;
import org.apache.hadoop.squashfs.superblock.SuperBlock;
import org.apache.hadoop.squashfs.table.ExportTable;
import org.apache.hadoop.squashfs.table.FragmentTable;
import org.apache.hadoop.squashfs.table.IdTable;

import java.io.Closeable;
import java.io.DataOutput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface SquashFsReader extends Closeable {

  public static SquashFsReader fromFile(int tag, File inputFile)
      throws SquashFsException, IOException {
    return new FileSquashFsReader(tag, inputFile);
  }

  public static SquashFsReader fromFile(
      int tag, File inputFile,
      MetadataBlockCache metadataCache,
      DataBlockCache dataCache,
      DataBlockCache fragmentCache) throws SquashFsException, IOException {

    return new FileSquashFsReader(tag, inputFile, metadataCache, dataCache,
        fragmentCache);
  }

  public static SquashFsReader fromMappedFile(int tag, MappedFile mmap)
      throws SquashFsException, IOException {
    return new MappedSquashFsReader(tag, mmap);
  }

  public static SquashFsReader fromMappedFile(int tag, MappedFile mmap,
      MetadataBlockCache metadataCache,
      DataBlockCache dataCache,
      DataBlockCache fragmentCache) throws SquashFsException, IOException {
    return new MappedSquashFsReader(tag, mmap, metadataCache, dataCache,
        fragmentCache);
  }

  public SuperBlock getSuperBlock();

  public IdTable getIdTable();

  public FragmentTable getFragmentTable();

  public ExportTable getExportTable();

  public MetadataBlockReader getMetaReader();

  public DirectoryINode getRootInode() throws IOException, SquashFsException;

  public INode findInodeByInodeRef(INodeRef ref)
      throws IOException, SquashFsException;

  public INode findInodeByDirectoryEntry(DirectoryEntry entry)
      throws IOException, SquashFsException;

  public INode findInodeByPath(String path)
      throws IOException, SquashFsException, FileNotFoundException;

  public List<DirectoryEntry> getChildren(INode parent)
      throws IOException, SquashFsException;

  public long writeFileStream(INode inode, OutputStream out)
      throws IOException, SquashFsException;

  public long writeFileOut(INode inode, DataOutput out)
      throws IOException, SquashFsException;

  public int read(INode inode, long fileOffset, byte[] buf, int off, int len)
      throws IOException, SquashFsException;

}
