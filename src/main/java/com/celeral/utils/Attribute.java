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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.celeral.utils.StringCodec.Boolean2String;
import com.celeral.utils.StringCodec.Enum2String;
import com.celeral.utils.StringCodec.Integer2String;
import com.celeral.utils.StringCodec.Long2String;
import com.celeral.utils.StringCodec.Short2String;
import com.celeral.utils.StringCodec.String2String;

/**
 * Attribute is a normalized key type for storing heterogeneous key/value
 * pairs in a collection without losing the static type information.
 *
 * @param <T> type of the value which can be stored against the attribute.
 *
 */
public class Attribute<T> implements Serializable
{
  public final T defaultValue;
  public final String name;
  public final StringCodec<T> codec;

  private Attribute()
  {
    this(null, null, null);
  }

  public Attribute(StringCodec<T> codec)
  {
    this(null, null, codec);
  }

  public Attribute(T defaultValue)
  {
    this(null, defaultValue, null);
  }

  public Attribute(T defaultValue, StringCodec<T> codec)
  {
    this(null, defaultValue, codec);
  }

  private Attribute(String name, T defaultValue, StringCodec<T> codec)
  {
    this.name = name;
    this.defaultValue = defaultValue;
    this.codec = codec;
  }

  @Override
  public int hashCode()
  {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    @SuppressWarnings(value = "unchecked")
    final Attribute<T> other = (Attribute<T>)obj;
    return !((this.name == null) ? (other.name != null) : !this.name.equals(other.name));
  }

  public String getName()
  {
    return "attr" + name.substring(name.lastIndexOf('.'));
  }

  public String getSimpleName()
  {
    return name.substring(name.lastIndexOf('.') + 1);
  }

  @Override
  public String toString()
  {
    return "Attribute{" + "defaultValue=" + defaultValue + ", name=" + name + ", codec=" + codec + '}';
  }

  private static final long serialVersionUID = 201310111904L;

  /**
   * Parameterized and scoped context attribute map that supports serialization.
   * Derived from io.netty.util.AttributeMap
   */
  public static interface AttributeMap extends Cloneable
  {
    /**
     * Return the attribute value for the given key. If the map does not have an
     * entry for the key, null is returned.
     *
     * @param <T>
     * @param key
     *
     * @return AttributeValue
     */
    <T> T get(Attribute<T> key);

    /**
     * Return true if value for an attribute is set. It returns false if the
     * value for the attribute was never set.
     *
     * @param key attribute key
     *
     * @return true if the value for the attribute was set, false otherwise.
     */
    boolean contains(Attribute<?> key);

    /**
     * Assign value for a particular attributes.
     *
     * @param <T>   Type of the value
     * @param key   Attribute which is being assigned the value
     * @param value Value which is being assigned.
     *
     * @return Previous value against the attribute or null if it was not assigned.
     */
    <T> T put(Attribute<T> key, T value);

    Set<Map.Entry<Attribute<?>, Object>> entrySet();

    /**
     * Clone the current map.
     *
     * @return a shallow copy of this AtrributeMap.
     *
     * @throws java.lang.CloneNotSupportedException
     */
    AttributeMap clone() throws CloneNotSupportedException;

    /**
     * DefaultAttributeMap is the default implementation of AttributeMap. It's backed by a map internally.
     */
    public static class DefaultAttributeMap implements AttributeMap, Serializable
    {
      private HashMap<Attribute<?>, Object> map;

      public DefaultAttributeMap()
      {
        this(new HashMap<Attribute<?>, Object>());
      }

      private DefaultAttributeMap(HashMap<Attribute<?>, Object> map)
      {
        super();
        this.map = map;
      }

      @Override
      @SuppressWarnings(value = "unchecked")
      public <T> T get(Attribute<T> key)
      {
        return (T)map.get(key);
      }

      @Override
      public String toString()
      {
        return this.map.toString();
      }

      @Override
      @SuppressWarnings("unchecked")
      public DefaultAttributeMap clone() throws CloneNotSupportedException
      {
        DefaultAttributeMap clone = (DefaultAttributeMap)super.clone();
        clone.map = (HashMap< Attribute< ?>, Object>)map.clone();
        return clone;
      }

      @Override
      @SuppressWarnings(value = "unchecked")
      public <T> T put(Attribute<T> key, T value)
      {
        return (T)map.put(key, value);
      }

      @Override
      public Set<Entry<Attribute<?>, Object>> entrySet()
      {
        return map.entrySet();
      }

      @Override
      public boolean equals(Object o)
      {
        if (this == o) {
          return true;
        }
        if (!(o instanceof DefaultAttributeMap)) {
          return false;
        }
        DefaultAttributeMap that = (DefaultAttributeMap)o;
        return !(map != null ? !map.equals(that.map) : that.map != null);
      }

      @Override
      public int hashCode()
      {
        return map != null ? map.hashCode() : 0;
      }

      @Override
      public boolean contains(Attribute<?> key)
      {
        return map.containsKey(key);
      }

      private void writeObject(java.io.ObjectOutputStream out) throws IOException
      {
        out.writeInt(map.size());
        for (Entry<Attribute<?>, Object> entry : map.entrySet()) {
          @SuppressWarnings("unchecked")
          Attribute<Object> key = (Attribute<Object>)entry.getKey();
          out.writeObject(key);

          Object value = entry.getValue();
          if (value instanceof Serializable || value == null) {
            out.writeBoolean(true);
            out.writeObject(value);
          }
          else if (key.codec == null) {
            throw new IllegalArgumentException("Cannot serialize value " + value + " stored against key " + key + " in attributes");
          }
          else {
            out.writeBoolean(false);
            out.writeUTF(key.codec.toString(value));
          }
        }
      }

      private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
      {
        int size = in.readInt();

        map = new HashMap<>(size);
        while (size-- > 0) {
          @SuppressWarnings("unchecked")
          Attribute<?> key = (Attribute<?>)in.readObject();
          Object value = in.readBoolean() ? in.readObject() : key.codec.fromString(in.readUTF());
          map.put(key, value);
        }

      }

      private static final long serialVersionUID = 201306051022L;
    }

    /**
     * This class inspects and initializes the attributes with their field names so that they can be used
     * from properties files.
     *
     * Engine uses it internally to initialize the Interfaces that may have Attributes defined in them.
     */
    public static class AttributeInitializer
    {
      static final HashMap<Class<?>, Set<Attribute<Object>>> map = new HashMap<>();

      public static Map<Attribute<Object>, Object> getAllAttributes(Context context, Class<?> clazz)
      {
        try {
          Map<Attribute<Object>, Object> result = new HashMap<>();

          for (Field f : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) && Attribute.class.isAssignableFrom(f.getType())) {
              @SuppressWarnings(value = "unchecked")
              Attribute<Object> attribute = (Attribute<Object>)f.get(null);
              result.put(attribute, context.getValue(attribute));
            }
          }

          return result;
        }
        catch (SecurityException | IllegalArgumentException | IllegalAccessException ex) {
          throw Throwables.wrapIfChecked(ex);
        }
      }

      public static Set<Attribute<Object>> getAttributes(Class<?> clazz)
      {
        return map.get(clazz);
      }

      /**
       * Initialize the static attributes defined in the class.
       *
       * @param clazz class whose static attributes need to be initialized.
       *
       * @return 0 if the clazz was already initialized, identity hash code of the clazz otherwise.
       */
      /* both for Enum2String */
      @SuppressWarnings(value = {"unchecked", "rawtypes"})
      public static long initialize(final Class<?> clazz)
      {
        if (map.containsKey(clazz)) {
          return 0;
        }

        try {
          Set<Attribute<Object>> set = new HashSet<>();

          for (Field f : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) && Attribute.class.isAssignableFrom(f.getType())) {
              Attribute<Object> attribute = (Attribute<Object>)f.get(null);
              if (attribute.name == null) {
                Field nameField = Attribute.class.getDeclaredField("name");
                nameField.setAccessible(true);
                nameField.set(attribute, clazz.getCanonicalName() + '.' + f.getName());
                nameField.setAccessible(false);
              }
              /* Handle trivial cases here even though this may spoil API users. */
              if (attribute.codec == null) {
                StringCodec<?> codec = null;
                if (attribute.defaultValue != null) {
                  Class<?> klass = attribute.defaultValue.getClass();
                  if (klass == String.class) {
                    codec = new String2String();
                  }
                  else if (klass == Integer.class) {
                    codec = new Integer2String();
                  }
                  else if (klass == Long.class) {
                    codec = new Long2String();
                  }
                  else if (klass == Short.class) {
                    codec = new Short2String();
                  }
                  else if (klass == Boolean.class) {
                    codec = new Boolean2String();
                  }
                  else if (Enum.class.isAssignableFrom(klass)) {
                    codec = new Enum2String(klass);
                  }
                }
                if (codec != null) {
                  Field codecField = Attribute.class.getDeclaredField("codec");
                  codecField.setAccessible(true);
                  codecField.set(attribute, codec);
                  codecField.setAccessible(false);
                }
              }
              set.add(attribute);
            }
          }
          map.put(clazz, set);

          return (long)clazz.getModifiers() << 32 | clazz.hashCode();
        }
        catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException ex) {
          throw Throwables.wrapIfChecked(ex);
        }
      }

    }

  }

}
