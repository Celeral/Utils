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

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

import org.junit.Test;

/** */
public class SerializableObjectTest {
  public static final String filename = "target/" + SerializableObjectTest.class.getName() + ".bin";

  public static class SerializableOperator<T> extends SerializableObject {
    public final transient Integer x = 10;
    private int i;

    public void setI(int i) {
      this.i = i;
    }

    public int getI() {
      return i;
    }

    @Override
    public int hashCode() {
      int hash = 3;
      return hash;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final SerializableOperator<?> other = (SerializableOperator<?>) obj;
      if (this.i != other.i) {
        return false;
      }
      return Objects.equals(this.x, other.x);
    }

    @Override
    public String toString() {
      return "SerializableOperator{" + "x=" + x + ", i=" + i + '}';
    }

    private static final long serialVersionUID = 201404140854L;
  }

  @Test
  public void testReadResolve() throws Exception {
    SerializableOperator<Object> pre = new SerializableOperator<>();
    pre.setI(10);

    try (FileOutputStream fos = new FileOutputStream(filename);
        ObjectOutputStream oos = new ObjectOutputStream(fos)) {
      oos.writeObject(pre);
    }

    Object post;
    try (FileInputStream fis = new FileInputStream(filename);
        ObjectInputStream ois = new ObjectInputStream(fis)) {
      post = ois.readObject();
    }

    assertEquals("Serialized Deserialized Objects", pre, post);
  }
}
