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
package org.getopt.util.hash;

/**
 * This is a very fast, non-cryptographic hash suitable for general hash-based lookup. See
 * http://murmurhash.googlepages.com/ for more details.
 *
 * <p>The C version of MurmurHash 2.0 found at that site was ported to Java by Andrzej Bialecki (ab
 * at getopt org).
 *
 * <p>Customized for Celeral's use for hash of byte array slice by Chetan Narsude (chetan at apache
 * org).
 *
 * @since 1.0.0
 */
public class MurmurHash {
  public static int hash(byte[] data, int seed) {
    return hash(data, seed, 0, data.length);
  }

  public static int hash(byte[] data, int seed, int offset, int length) {
    int m = 0x5bd1e995;
    int r = 24;

    int h = seed ^ length;

    int len_4 = length >> 2;

    for (int i = 0; i < len_4; i++) {
      int i_4 = offset + (i << 2);
      int k = data[i_4 + 3];
      k <<= 8;
      k |= (data[i_4 + 2] & 0xff);
      k <<= 8;
      k |= (data[i_4 + 1] & 0xff);
      k <<= 8;
      k |= (data[i_4 + 0] & 0xff);
      k *= m;
      k ^= k >>> r;
      k *= m;
      h *= m;
      h ^= k;
    }

    int len_m = len_4 << 2;
    int left = length - len_m;

    if (left != 0) {
      length += offset;
      if (left >= 3) {
        h ^= (int) data[length - 3] << 16;
      }
      if (left >= 2) {
        h ^= (int) data[length - 2] << 8;
      }
      if (left >= 1) {
        h ^= (int) data[length - 1];
      }

      h *= m;
    }

    h ^= h >>> 13;
    h *= m;
    h ^= h >>> 15;

    return h;
  }
}
