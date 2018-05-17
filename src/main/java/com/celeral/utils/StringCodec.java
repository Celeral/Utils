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

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * This interface is essentially serializer/deserializer interface which works with String as
 * the serialized type. When initializing the attributes from the properties file, attribute
 * values represented as Strings are needed to be converted to POJO. This class facilitates the
 * conversion from and to String for attribute values.
 *
 * @param <T> Type of the object which can be converted to/from String.
 */
public interface StringCodec<T>
{
  /**
   * Given a string representation (typically from properties file) for an object , create object from it.
   *
   * @param string Type of the POJO which is created from String representation.
   *
   * @return POJO obtained as a result of deserialization
   */
  T fromString(String string);

  /**
   * Given a POJO, serialize it to a String object (typically to be stored in properties file).
   *
   * @param pojo The object which needs to be serialized.
   *
   * @return Serialized representation of pojo..
   */
  String toString(T pojo);

  public class String2String implements StringCodec<String>, Serializable
  {
    @Override
    public String fromString(String string)
    {
      return string;
    }

    @Override
    public String toString(String pojo)
    {
      return pojo;
    }

    private static final long serialVersionUID = 201310141156L;
  }

  public class Integer2String implements StringCodec<Integer>, Serializable
  {
    @Override
    public Integer fromString(String string)
    {
      return Integer.valueOf(string);
    }

    @Override
    public String toString(Integer pojo)
    {
      return String.valueOf(pojo);
    }

    private static final long serialVersionUID = 201310141157L;
  }

  public class Short2String implements StringCodec<Short>, Serializable
  {
    @Override
    public Short fromString(String string)
    {
      return Short.valueOf(string);
    }

    @Override
    public String toString(Short pojo)
    {
      return String.valueOf(pojo);
    }

    private static final long serialVersionUID = 201310141157L;
  }

  public class Long2String implements StringCodec<Long>, Serializable
  {
    @Override
    public Long fromString(String string)
    {
      return Long.valueOf(string);
    }

    @Override
    public String toString(Long pojo)
    {
      return String.valueOf(pojo);
    }

    private static final long serialVersionUID = 201310141158L;
  }

  public class Boolean2String implements StringCodec<Boolean>, Serializable
  {
    @Override
    public Boolean fromString(String string)
    {
      return Boolean.valueOf(string);
    }

    @Override
    public String toString(Boolean pojo)
    {
      return String.valueOf(pojo);
    }

    private static final long serialVersionUID = 201310141159L;
  }

  public class URI2String implements StringCodec<URI>, Serializable
  {
    @Override
    public URI fromString(String string)
    {
      try {
        return new URI(string);
      }
      catch (URISyntaxException ex) {
        throw new RuntimeException(ex);
      }
    }

    @Override
    public String toString(URI uri)
    {
      return uri.toString();
    }

    private static final long serialVersionUID = 201706270412L;
  }

  /**
   * The attributes which represent arbitrary objects for which the schema cannot be
   * standardized, we allow them to be represented as &lt;ClassName&gt;:&lt;Constructor_String&gt;:&lt;Property_String&gt;
   * representation. This allows us to instantiate the class by invoking its constructor
   * which takes &lt;String&gt; as argument. If only the &lt;ClassName&gt; is specified,
   * then just the class is instantiated using default constructor. If colon is 
   * specified then class is instantiated using constructor with string as an argument.
   * If properties are specified then properties will be set on the object.
   * The properties are defined in property=value format separated by colon(:)
   *
   * @param <T> Type of the object which is converted to/from String
   */
  public class Object2String<T> implements StringCodec<T>, Serializable
  {
    public final String separator;

    public Object2String()
    {
      separator = ":";
    }

    public Object2String(String separator)
    {
      this.separator = separator;
    }

    public static Class<?> loadClass(String classname, ClassLoader... loaders) throws ClassNotFoundException
    {
      if (loaders != null) {
        for (ClassLoader loader : loaders) {
          try {
            return loader.loadClass(classname);
          }
          catch (ClassNotFoundException ex) {
            /* we ignore */
          }
        }
      }

      return Class.forName(classname);
    }

    @Override
    @SuppressWarnings({"UseSpecificCatch", "BroadCatchBlock", "TooBroadCatch"})
    public T fromString(String string)
    {
      String[] parts = string.split(separator, 2);

      try {
        @SuppressWarnings("unchecked")
        Class<? extends T> clazz = (Class<? extends T>)loadClass(parts[0], Thread.currentThread().getContextClassLoader());
        if (parts.length == 1) {
          return clazz.newInstance();
        }

        return clazz.getConstructor(String.class).newInstance(parts[1]);
      }
      catch (Throwable cause) {
        throw Throwables.wrapIfChecked(cause);
      }
    }

    @Override
    public String toString(T pojo)
    {
      String arg = pojo.toString();
      if (arg == null) {
        return pojo.getClass().getName();
      }

      return pojo.getClass().getName() + separator + arg;
    }

    private static final long serialVersionUID = 201311141853L;
  }

  public class Path2String implements StringCodec<Path>, Serializable
  {
    @Override
    public Path fromString(String string)
    {
      try {
        return Paths.get(new URI(string));
      }
      catch (URISyntaxException ex) {
        throw new RuntimeException(ex);
      }
    }

    @Override
    public String toString(Path pojo)
    {
      return pojo.toUri().toString();
    }

    private static final long serialVersionUID = 201707110528L;
  }

  public class Map2String<K, V> implements StringCodec<Map<K, V>>, Serializable
  {
    private final StringCodec<K> keyCodec;
    private final StringCodec<V> valueCodec;
    private final String separator;
    private final String equal;

    public Map2String(String separator, String equal, StringCodec<K> keyCodec, StringCodec<V> valueCodec)
    {
      this.equal = equal;
      this.separator = separator;
      this.keyCodec = keyCodec;
      this.valueCodec = valueCodec;
    }

    @Override
    public Map<K, V> fromString(String string)
    {
      if (string == null) {
        return null;
      }

      if (string.isEmpty()) {
        return new HashMap<>();
      }

      String[] parts = string.split(separator);
      HashMap<K, V> map = new HashMap<>();
      for (String part : parts) {
        String[] kvpair = part.split(equal, 2);
        map.put(keyCodec.fromString(kvpair[0]), valueCodec.fromString(kvpair[1]));
      }

      return map;
    }

    @Override
    public String toString(Map<K, V> map)
    {
      if (map == null) {
        return null;
      }

      if (map.isEmpty()) {
        return "";
      }
      String[] parts = new String[map.size()];
      int i = 0;
      for (Map.Entry<K, V> entry : map.entrySet()) {
        parts[i++] = keyCodec.toString(entry.getKey()) + equal + valueCodec.toString(entry.getValue());
      }
      return StringUtils.join(parts, separator);
    }

    private static final long serialVersionUID = 201402272053L;
  }

  public class Collection2String<T> implements StringCodec<Collection<T>>, Serializable
  {
    private final String separator;
    private final StringCodec<T> codec;

    public Collection2String(String separator, StringCodec<T> codec)
    {
      this.separator = separator;
      this.codec = codec;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<T> fromString(String string)
    {
      if (string == null) {
        return null;
      }

      if (string.isEmpty()) {
        return Collections.EMPTY_LIST;
      }

      String[] parts = string.split(separator);
      ArrayList<T> arrayList = new ArrayList<>(parts.length);
      for (String part : parts) {
        arrayList.add(codec.fromString(part));
      }

      return arrayList;
    }

    @Override
    public String toString(Collection<T> pojo)
    {
      if (pojo == null) {
        return null;
      }

      if (pojo.isEmpty()) {
        return "";
      }

      String[] parts = new String[pojo.size()];

      int i = 0;
      for (T o : pojo) {
        parts[i++] = codec.toString(o);
      }

      return StringUtils.join(parts, separator);
    }

    private static final long serialVersionUID = 201401091806L;
  }

  public class Enum2String<T extends Enum<T>> implements StringCodec<T>, Serializable
  {
    private final Class<T> clazz;

    public Enum2String(Class<T> clazz)
    {
      this.clazz = clazz;
    }

    @Override
    public T fromString(String string)
    {
      return string == null ? null : Enum.valueOf(clazz, string);
    }

    @Override
    public String toString(T pojo)
    {
      return pojo == null ? null : pojo.name();
    }

    private static final long serialVersionUID = 201310181757L;
  }

  public class Class2String<T> implements StringCodec<Class<? extends T>>, Serializable
  {
    @Override
    public Class<? extends T> fromString(String string)
    {
      try {
        @SuppressWarnings({"rawtypes", "unchecked"})
        Class<? extends T> clazz = (Class)Thread.currentThread().getContextClassLoader().loadClass(string);
        return clazz;
      }
      catch (ClassNotFoundException cause) {
        throw new RuntimeException(cause);
      }
    }

    @Override
    public String toString(Class<? extends T> clazz)
    {
      return clazz.getCanonicalName();
    }

    private static final long serialVersionUID = 201312082053L;
  }
}
