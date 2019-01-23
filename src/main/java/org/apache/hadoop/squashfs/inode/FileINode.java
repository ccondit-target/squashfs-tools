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

package org.apache.hadoop.squashfs.inode;

public interface FileINode extends INode {

  public static final int FRAGMENT_BLOCK_INDEX_NONE = 0xffff_ffff;

  public long getBlocksStart();

  public void setBlocksStart(long blocksStart);

  public int getFragmentBlockIndex();

  public void setFragmentBlockIndex(int fragmentBlockIndex);

  public int getFragmentOffset();

  public void setFragmentOffset(int fragmentOffset);

  public long getFileSize();

  public void setFileSize(long fileSize);

  public int[] getBlockSizes();

  public void setBlockSizes(int[] blockSizes);

  public long getSparse();

  public void setSparse(long sparse);

  public int getNlink();

  public void setNlink(int nlink);

  public int getXattrIndex();

  public void setXattrIndex(int xattrIndex);

  public boolean isSparseBlockPresent();

  public boolean isFragmentPresent();

  public boolean isXattrPresent();

  public FileINode simplify();

}
