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

package org.apache.hadoop.squashfs.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SizeTrackingInputStream extends FilterInputStream {
  private long byteCount = 0L;
  private long mark = -1L;

  public SizeTrackingInputStream(InputStream in) {
    super(in);
  }

  public long getByteCount() {
    return byteCount;
  }

  @Override
  public int read() throws IOException {
    int b = in.read();
    if (b >= 0) {
      byteCount++;
    }
    return b;
  }

  @Override
  public int read(byte[] b) throws IOException {
    int c = in.read(b);
    if (c > 0) {
      byteCount += c;
    }
    return c;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    int c = in.read(b, off, len);
    if (c > 0) {
      byteCount += c;
    }
    return c;
  }

  @Override
  public long skip(long n) throws IOException {
    long c = skip(n);
    byteCount += c;
    return c;
  }

  @Override
  public synchronized void mark(int readlimit) {
    in.mark(readlimit);
    mark = byteCount;
  }

  @Override
  public synchronized void reset() throws IOException {
    in.reset();
    if (mark >= 0L) {
      byteCount = mark;
    }
  }

}
