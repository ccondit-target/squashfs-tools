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

package org.apache.hadoop.squashfs.tools;

import org.apache.hadoop.squashfs.MappedSquashFsReader;
import org.apache.hadoop.squashfs.SquashFsReader;
import org.apache.hadoop.squashfs.directory.DirectoryEntry;
import org.apache.hadoop.squashfs.inode.DirectoryINode;
import org.apache.hadoop.squashfs.inode.FileINode;
import org.apache.hadoop.squashfs.inode.INode;
import org.apache.hadoop.squashfs.io.MappedFile;
import org.apache.hadoop.squashfs.metadata.MetadataReader;
import org.apache.hadoop.squashfs.util.BinUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class SquashFsck {

  public static void usage() {
    System.err.printf("Usage: %s [options...] <squashfs-file>%n",
        SquashFsck.class.getSimpleName());
    System.err.println();
    System.err.println("    -m,--mapped     Use mmap() for I/O");
    System.err.println("    -t,--tree       Dump tree");
    System.err.println("    -f,--files      Read all files (implies --tree)");
    System.err.println("       --metadata   Dump metadata ");
    System.err.println("                       <file-offset> <block-offset>");
    System.err.println();
    System.exit(1);
  }

  private static SquashFsReader createReader(File file, boolean mapped)
      throws IOException {
    if (mapped) {
      System.out.println("Using memory-mapped reader");
      System.out.println();
      try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
        try (FileChannel channel = raf.getChannel()) {
          MappedFile mmap = MappedFile.mmap(channel,
              MappedSquashFsReader.PREFERRED_MAP_SIZE,
              MappedSquashFsReader.PREFERRED_WINDOW_SIZE);

          return SquashFsReader.fromMappedFile(0, mmap);
        }
      }
    } else {
      System.out.println("Using file reader");
      System.out.println();
      return SquashFsReader.fromFile(0, file);
    }
  }

  private static void dumpTree(SquashFsReader reader, boolean readFiles)
      throws IOException {
    System.out.println("Directory tree:");
    System.out.println();
    DirectoryINode root = reader.getRootInode();
    dumpSubtree(reader, true, "/", root, readFiles);
  }

  private static void dumpFileContent(SquashFsReader reader, FileINode inode)
      throws IOException {
    long fileSize = inode.getFileSize();
    long readSize;
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      reader.writeFileStream(inode, bos);
      byte[] content = bos.toByteArray();
      readSize = content.length;
    }
    System.out.printf("  %d bytes, %d read%n", fileSize, readSize);
  }

  private static void dumpSubtree(SquashFsReader reader, boolean root,
      String path, DirectoryINode inode,
      boolean readFiles)
      throws IOException {

    if (root) {
      System.out.printf("/ (%d)%n", inode.getInodeNumber());
    }

    for (DirectoryEntry entry : reader.getChildren(inode)) {
      INode childInode = reader.findInodeByDirectoryEntry(entry);
      System.out.printf("%s%s%s (%d)%n",
          path, entry.getNameAsString(),
          childInode.getInodeType().directory() ? "/" : "",
          childInode.getInodeNumber());

      if (readFiles && childInode.getInodeType().file()) {
        dumpFileContent(reader, (FileINode) childInode);
      }
    }

    for (DirectoryEntry entry : reader.getChildren(inode)) {
      INode childInode = reader.findInodeByDirectoryEntry(entry);
      if (childInode.getInodeType().directory()) {
        dumpSubtree(reader, false, String.format("%s%s/",
            path, entry.getNameAsString()), (DirectoryINode) childInode,
            readFiles);
      }
    }
  }

  private static void dumpMetadataBlock(
      SquashFsReader reader, long metaFileOffset, int metaBlockOffset)
      throws IOException {

    System.out.println();
    System.out.printf("Dumping block at file offset %d, block offset %d%n",
        metaFileOffset, metaBlockOffset);
    System.out.println();

    MetadataReader mr = reader.getMetaReader()
        .rawReader(0, metaFileOffset, (short) metaBlockOffset);
    mr.isEof(); // make sure block is read
    byte[] buf = new byte[mr.available()];
    mr.readFully(buf);

    StringBuilder sb = new StringBuilder();
    BinUtils.dumpBin(sb, 0, "data", buf, 0, buf.length, 32, 2);
    System.out.println(sb.toString());
  }

  public static void main(String[] args) throws Exception {

    boolean mapped = false;
    boolean tree = false;
    boolean files = false;
    boolean metadata = false;
    long metaFileOffset = 0L;
    int metaBlockOffset = 0;

    String squashfs = null;
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      switch (arg) {
      case "-m":
      case "--mapped":
        mapped = true;
        break;
      case "-t":
      case "--tree":
        tree = true;
        break;
      case "-f":
      case "--files":
        files = true;
        break;
      case "--metadata":
        metadata = true;
        if (i + 2 >= args.length) {
          usage();
        }
        metaFileOffset = Long.parseLong(args[++i], 10);
        metaBlockOffset = Integer.parseInt(args[++i], 10);
        break;
      default:
        if (squashfs != null) {
          usage();
        }
        squashfs = arg;
      }
    }
    if (squashfs == null) {
      usage();
    }

    try (SquashFsReader reader = createReader(new File(squashfs), mapped)) {
      System.out.println(reader.getSuperBlock());
      System.out.println();
      System.out.println(reader.getIdTable());
      System.out.println();
      System.out.println(reader.getFragmentTable());
      System.out.println();
      System.out.println(reader.getExportTable());
      System.out.println();

      if (tree || files) {
        dumpTree(reader, files);
      }

      if (metadata) {
        dumpMetadataBlock(reader, metaFileOffset, metaBlockOffset);
      }
    }
  }

}
