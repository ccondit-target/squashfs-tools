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

package org.apache.hadoop.squashfs.table;

import org.apache.hadoop.squashfs.SquashFsException;
import org.apache.hadoop.squashfs.io.ByteBufferDataInput;
import org.apache.hadoop.squashfs.io.MappedFile;
import org.apache.hadoop.squashfs.superblock.SuperBlock;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MappedFileTableReader implements TableReader {

  private final MappedFile mmap;
  private final SuperBlock sb;

  public MappedFileTableReader(MappedFile mmap)
      throws IOException, SquashFsException {
    this.mmap = mmap;
    this.sb = SuperBlock.read(new ByteBufferDataInput(mmap.from(0L)));
  }

  public MappedFileTableReader(MappedFile mmap, SuperBlock sb) {

    this.mmap = mmap;
    this.sb = sb;
  }

  @Override
  public SuperBlock getSuperBlock() {
    return sb;
  }

  @Override
  public ByteBuffer read(long fileOffset, int length) throws EOFException {
    ByteBuffer src = mmap.from(fileOffset);
    if (src.remaining() < length) {
      throw new EOFException();
    }
    return src.order(ByteOrder.LITTLE_ENDIAN);
  }

  @Override
  public void close() {
  }

}
