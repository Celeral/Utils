/*
 * Copyright 2019 Celeral.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;

import static org.junit.Assert.*;

public class StreamUtilsTest
{

  @Test
  public void readFully() throws IOException, NoSuchAlgorithmException
  {
    MessageDigest digest = MessageDigest.getInstance("MD5");

    for (int size : new int[]{500, 512, 1000, 1024}) {
      try (InputStream txt = StreamUtilsTest.class.getResourceAsStream(size + ".txt");
           InputStream md5 = StreamUtils.class.getResourceAsStream(size + ".md5")) {
        byte[] txtBytes = StreamUtils.readFully(txt);
        assertEquals("Count of read bytes", size, txtBytes.length);

        byte[] md5Bytes = StreamUtils.readFully(md5);

        byte[] hash = digest.digest(txtBytes);
        StringBuilder sb = new StringBuilder(2*hash.length);
        for(byte b : hash){
          sb.append(String.format("%02x", b&0xff));
        }

        assertArrayEquals("MD5 checksum of contents", md5Bytes, sb.toString().getBytes());

        digest.reset();
      }
    }
  }
}