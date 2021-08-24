/*
 * Copyright © 2021 Celeral.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.celeral.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/** Utility functions to operate on the input/output streams.F* */
public class StreamUtils {
  /**
   * Reads all the bytes from the input stream until the stream's end is reached. This function does
   * not close the stream as it does not open it as well.
   *
   * @param stream input stream from where bytes are needed to be read
   * @return array of bytes read
   * @throws IOException propagates the exceptions encountered while reading the stream
   */
  public static byte[] readFully(InputStream stream) throws IOException {
    int totalOffset = 0;
    ArrayList<byte[]> buffers = new ArrayList<>(1);

    int available = stream.available();
    if (available == 0) {
      available = 512;
    }

    byte[] buffer = new byte[available];
    int offset = 0;

    int read;
    while ((read = stream.read(buffer, offset, available)) != -1) {
      if (read == available) {
        totalOffset += buffer.length;
        read = stream.read();
        if (read == -1) {
          break;
        } else {
          buffers.add(buffer);
          int newLength = 1 << (31 - Integer.numberOfLeadingZeros(totalOffset));
          buffer = new byte[newLength];
          buffer[0] = (byte) read;
          available = newLength - 1;
          offset = 1;
        }
      } else {
        offset += read;
        available -= read;
      }
    }

    byte[] newBuffer;
    if (buffer.length == totalOffset) {
      if (buffers.isEmpty()) {
        return buffer;
      }

      newBuffer = buffer;
    } else {
      newBuffer = new byte[totalOffset];
    }

    System.arraycopy(buffer, 0, newBuffer, totalOffset - offset, offset);

    offset = 0;
    for (byte[] bytes : buffers) {
      System.arraycopy(bytes, 0, newBuffer, offset, bytes.length);
      offset += bytes.length;
    }

    return newBuffer;
  }

  /**
   * Reads all the bytes from the input stream until either the array is full or the stream's end is
   * reached. This function does not close the stream as it does not open it as well.
   *
   * @param stream input stream from where bytes are needed to be read
   * @param bytes array to read the bytes into
   * @return count of bytes read
   * @throws IOException propagates the exceptions encountered while reading the stream
   */
  public static int readFully(InputStream stream, final byte[] bytes) throws IOException {
    int offset = stream.read(bytes);
    if (offset == -1) {
      return 0;
    }

    return offset + readFully(stream, bytes, offset, bytes.length - offset);
  }

  /**
   * Reads all the bytes from the input stream until either the requested number of bytes are read
   * or the stream's end is reached. This function does not close the stream as it does not open it
   * as well.
   *
   * @param stream input stream from where bytes are needed to be read
   * @param bytes array to read the bytes into
   * @param offset location where the read bytes are written starting from
   * @param length count of bytes to read if available
   * @return count of bytes read
   * @throws IOException propagates the exceptions encountered while reading the stream
   */
  public static int readFully(InputStream stream, final byte[] bytes, int offset, int length)
      throws IOException {
    int totalRead = 0;

    while (totalRead < length) {
      int read = stream.read(bytes, offset, length - totalRead);
      if (read == -1) {
        break;
      }

      totalRead += read;
    }

    return totalRead;
  }
}
