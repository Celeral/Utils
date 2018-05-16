/*
 * Copyright 2018 Celeral.
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
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Implementation of the StringCodec where object is converted to/from
 * its JSON representation using Jackson's fasterxml library. This mechanism
 * gives greater flexibility to the developer by using the full suite of
 * Jackson's JSON API to express their objects in a form suitable to be
 * included in the textual configuration files.
 *
 * @param <T> type of the object to be converted to JSON string
 */
public class Object2JSON<T> implements StringCodec<T>, Serializable
{
  private ObjectMapper mapper;

  public Object2JSON(ObjectMapper mapper)
  {
    this.mapper = mapper;
  }

  @Override
  public T fromString(String string)
  {
    try {
      JSONWrapper<T> wrapper = mapper.readValue(string, JSONWrapper.class);
      return wrapper.object;
    }
    catch (IOException ex) {
      throw Throwables.throwFormatted(ex, IllegalArgumentException.class,
                                      "Unable to deserialize json {}", string);
    }
  }

  @Override
  public String toString(T pojo)
  {
    JSONWrapper wrapper = new JSONWrapper(pojo);
    try {
      return mapper.writeValueAsString(wrapper);
    }
    catch (JsonProcessingException ex) {
      throw Throwables.throwFormatted(ex, IllegalArgumentException.class,
                                      "Unable to convert {} to JSON string!", pojo);
    }
  }

  @JsonDeserialize(using = JSONWrapperDeserializer.class)
  @JsonAutoDetect(fieldVisibility = Visibility.ANY)
  static class JSONWrapper<T>
  {
    JSONWrapper()
    {
    }

    public JSONWrapper(T o)
    {
      classname = (Class<T>)o.getClass();
      object = o;
    }

    Class<T> classname;
    T object;
  }

  static class JSONWrapperDeserializer extends StdDeserializer<JSONWrapper>
  {
    public JSONWrapperDeserializer()
    {
      this(null);
    }

    public JSONWrapperDeserializer(Class<?> clazz)
    {
      super(clazz);
    }

    @Override
    public JSONWrapper deserialize(JsonParser jp, DeserializationContext context) throws IOException, JsonProcessingException
    {
      JsonNode node = jp.readValueAsTree();
      try {
        return new JSONWrapper<>(jp.getCodec().treeToValue(node.get("object"), context.findClass(node.get("classname").textValue())));
      }
      catch (ClassNotFoundException ex) {
        throw new RuntimeException(ex);
      }
    }
  }
  private static final long serialVersionUID = 201805140217L;
}
