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

package org.apache.hadoop.squashfs.metadata;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.apache.hadoop.squashfs.directory.DirectoryEntry;
import org.apache.hadoop.squashfs.directory.DirectoryHeader;
import org.apache.hadoop.squashfs.inode.BasicDirectoryINode;
import org.apache.hadoop.squashfs.inode.INodeRef;
import org.apache.hadoop.squashfs.superblock.SuperBlock;
import org.apache.hadoop.squashfs.test.MetadataBlockReaderMock;
import org.apache.hadoop.squashfs.test.MetadataTestUtils;

public class MetadataBlockReaderTest {

	@Test
	public void readerShouldBeAbleToLocateDataFromMetadataReference() throws Exception {
		SuperBlock sb = new SuperBlock();

		MetadataReference meta = new MetadataReference(10101, 12345L, (short) 6789, Integer.MAX_VALUE);

		byte[] buf = new byte[8192];
		buf[6789] = (byte) 0xff;

		MetadataBlock block = MetadataTestUtils.block(buf);
		try (MetadataBlockReaderMock br = new MetadataBlockReaderMock(10101, sb, 12345L, block)) {
			MetadataReader mr = br.reader(meta);
			byte result = mr.readByte();
			assertEquals((byte) 0xff, result);
		}
	}

	@Test
	public void rawReaderShouldBeAbleToLocateDataFromLocationAndOffset() throws Exception {
		SuperBlock sb = new SuperBlock();

		byte[] buf = new byte[8192];
		buf[6789] = (byte) 0xff;

		MetadataBlock block = MetadataTestUtils.block(buf);
		try (MetadataBlockReaderMock br = new MetadataBlockReaderMock(10101, sb, 12345L, block)) {
			MetadataReader mr = br.rawReader(10101, 12345L, (short) 6789);
			byte result = mr.readByte();
			assertEquals((byte) 0xff, result);
		}
	}

	@Test
	public void inodeReaderShouldBeAbleToLocateDataFromInodeRef() throws Exception {
		SuperBlock sb = new SuperBlock();
		sb.setInodeTableStart(12345L);

		byte[] buf = new byte[8192];
		buf[1234] = (byte) 0xff;

		MetadataBlock block = MetadataTestUtils.block(buf);
		try (MetadataBlockReaderMock br = new MetadataBlockReaderMock(10101, sb, 66666L, block)) {
			MetadataReader mr = br.inodeReader(10101, new INodeRef(54321, (short) 1234).getRaw());
			byte result = mr.readByte();
			assertEquals((byte) 0xff, result);
		}
	}

	@Test
	public void inodeReaderShouldBeAbleToLocateDataFromDirectoryEntry() throws Exception {
		SuperBlock sb = new SuperBlock();
		sb.setInodeTableStart(12345L);

		byte[] buf = new byte[8192];
		buf[1234] = (byte) 0xff;

		DirectoryHeader dh = new DirectoryHeader() {
			{
				this.startBlock = 54321;
			}
		};
		DirectoryEntry de = new DirectoryEntry() {
			{
				this.header = dh;
				this.offset = (short) 1234;
			}
		};

		MetadataBlock block = MetadataTestUtils.block(buf);
		try (MetadataBlockReaderMock br = new MetadataBlockReaderMock(10101, sb, 66666L, block)) {
			MetadataReader mr = br.inodeReader(10101, de);
			byte result = mr.readByte();
			assertEquals((byte) 0xff, result);
		}
	}

	@Test
	public void directoryReaderShouldBeAbleToLocateDataFromINode() throws Exception {
		SuperBlock sb = new SuperBlock();
		sb.setDirectoryTableStart(12345L);

		byte[] buf = new byte[8192];
		buf[1234] = (byte) 0xff;

		BasicDirectoryINode inode = new BasicDirectoryINode();
		inode.setFileSize(50);
		inode.setStartBlock(54321);
		inode.setOffset((short) 1234);

		MetadataBlock block = MetadataTestUtils.block(buf);
		try (MetadataBlockReaderMock br = new MetadataBlockReaderMock(10101, sb, 66666L, block)) {
			MetadataReader mr = br.directoryReader(10101, inode);
			byte result = mr.readByte();
			assertEquals((byte) 0xff, result);
		}
	}

}
