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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Assert;
import org.junit.Test;

/** This tests the Object2JSON StringCodec */
public class Object2JSONTest {
  @JsonAutoDetect(fieldVisibility = Visibility.ANY)
  public static class MyClass {
    String string;
    int integer;

    @Override
    public int hashCode() {
      int hash = 7;
      hash = 41 * hash + Objects.hashCode(this.string);
      hash = 41 * hash + this.integer;
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
      final MyClass other = (MyClass) obj;
      if (this.integer != other.integer) {
        return false;
      }
      return Objects.equals(this.string, other.string);
    }
  }

  Object2JSON<MyClass> o2j = new Object2JSON<>(new ObjectMapper());

  @Test
  public void testCodec() throws JsonProcessingException {
    MyClass myclass = getObject();

    String json = o2j.toString(myclass);
    MyClass fromJson = o2j.fromString(json);

    Assert.assertEquals("JSON Codec serialization/deserialization", myclass, fromJson);
  }

  private MyClass getObject() {
    MyClass myclass = new MyClass();
    myclass.string = "json";
    myclass.integer = 42;
    return myclass;
  }

  @Test
  public void testSerializationOfCodec() throws IOException, ClassNotFoundException {
    MyClass myclass = getObject();
    String json = o2j.toString(myclass);

    byte[] bytes;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(o2j);
      oos.flush();
      bytes = baos.toByteArray();
    }

    try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais)) {
      @SuppressWarnings("unchecked")
      Object2JSON<MyClass> o = (Object2JSON<MyClass>) ois.readObject();
      o2j = o;
    }

    MyClass fromJson = o2j.fromString(json);
    Assert.assertEquals(
        "JSON Codec serialization/deserialization after serialization", myclass, fromJson);
  }
}
