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

package org.apache.hadoop.squashfs.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.squashfs.SquashFsException;
import org.apache.hadoop.squashfs.table.FragmentTable;
import org.apache.hadoop.squashfs.table.FragmentTableEntry;

public class InMemoryFragmentTable extends FragmentTable {

	private final List<FragmentTableEntry> entries;

	public InMemoryFragmentTable(List<FragmentTableEntry> entries) {
		this.entries = entries;
	}

	public InMemoryFragmentTable(FragmentTableEntry... entries) {
		this(Arrays.asList(entries));
	}

	@Override
	public int getFragmentCount() {
		return entries.size();
	}

	@Override
	public boolean isAvailable() {
		return !entries.isEmpty();
	}

	@Override
	public FragmentTableEntry getEntry(int id) throws IOException, SquashFsException {
		return entries.get(id);
	}

}
