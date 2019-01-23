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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

public class BasicFifoINodeTest {

	BasicFifoINode inode;

	@Before
	public void setUp() {
		inode = new BasicFifoINode();
		inode.setNlink(2);
	}

	@Test
	public void getNameShouldReturnCorrectValue() {
		assertEquals("basic-fifo-inode", inode.getName());
	}

	@Test
	public void getInodeTypeShouldReturnCorrectValue() {
		assertSame(INodeType.BASIC_FIFO, inode.getInodeType());
	}

	@Test
	public void simplifyShouldReturnSelf() {
		assertSame(inode, inode.simplify());
	}

	@Test
	public void staticSimplifyMethodShouldReturnOriginalIfBasic() {
		assertSame(inode, BasicFifoINode.simplify(inode));
	}

	@Test
	public void staticSimplifyMethodShouldReturnOriginalIfExtendedAttributesPresent() {
		FifoINode inode2 = new ExtendedFifoINode();
		inode2.setNlink(2);
		inode2.setXattrIndex(3);
		assertSame(inode2, BasicFifoINode.simplify(inode2));
	}

	@Test
	public void staticSimplifyMethodShouldReturnBasicIfExtendedAttributesNotPresent() {
		FifoINode inode2 = new ExtendedFifoINode();
		inode2.setNlink(2);
		inode2.setXattrIndex(-1);

		FifoINode result = BasicFifoINode.simplify(inode2);
		assertSame("wrong class", BasicFifoINode.class, result.getClass());
		assertSame("wrong nlink count", 2, result.getNlink());
	}

	@Test
	public void toStringShouldNotFail() {
		System.out.println(inode.toString());
	}

}
