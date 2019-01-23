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

package org.apache.hadoop.squashfs.superblock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import org.apache.hadoop.squashfs.SquashFsException;

public class CompressionIdTest {

	@Test
	public void valueShouldReturnIncrementingValues() {
		CompressionId[] values = CompressionId.values();

		for (int i = 0; i < values.length; i++) {
			assertEquals(String.format("Wrong value for %s", values[i]), (short) i, values[i].value());
		}
	}

	@Test
	public void fromValueShouldReturnCorrectItem() throws Exception {
		CompressionId[] values = CompressionId.values();

		for (int i = 0; i < values.length; i++) {
			assertSame(values[i], CompressionId.fromValue((short) i));
		}
	}

	@Test(expected = SquashFsException.class)
	public void fromValueShouldThrowExceptionOnInvalidValue() throws Exception {
		CompressionId.fromValue((short) CompressionId.values().length);
	}
}
