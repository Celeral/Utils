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
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.Test;

import org.apache.commons.beanutils.BeanUtils;

/** This tests the Object2String codec */
public class Object2StringTest {
  public static class TestBean {
    private int intVal;
    private String stringVal;
    private long longVal;

    public TestBean() {
      intVal = -1;
      stringVal = "constructor";
      longVal = -1;
    }

    @SuppressWarnings("LeakingThisInConstructor")
    public TestBean(String string) {
      this();
      if (string.isEmpty()) {
        return;
      }

      Properties props = new Properties();
      try {
        props.load(new StringReader(string.substring(1, string.length() - 1).replace(", ", "\n")));
      } catch (IOException ex) {
        throw new RuntimeException("Error while loading properties from stringified map", ex);
      }

      HashMap<String, String> map = new HashMap<>();
      for (Map.Entry<Object, Object> e : props.entrySet()) {
        map.put((String) e.getKey(), (String) e.getValue());
      }

      try {
        BeanUtils.populate(this, map);
      } catch (IllegalAccessException | InvocationTargetException ex) {
        throw new RuntimeException("Could not populate properties to bean", ex);
      }
    }

    public int getIntVal() {
      return intVal;
    }

    public void setIntVal(int intVal) {
      this.intVal = intVal;
    }

    public String getStringVal() {
      return stringVal;
    }

    public void setStringVal(String stringVal) {
      this.stringVal = stringVal;
    }

    public long getLongVal() {
      return longVal;
    }

    public void setLongVal(long longVal) {
      this.longVal = longVal;
    }

    @Override
    public String toString() {

      TestBean defaultTB = new TestBean();
      try {
        Map<String, String> describe = BeanUtils.describe(this);
        for (Iterator<Entry<String, String>> it = describe.entrySet().iterator(); it.hasNext(); ) {
          Entry<String, String> entry = it.next();
          switch (entry.getKey()) {
            case "intVal":
              if (String.valueOf(defaultTB.intVal).equals(entry.getValue())) {
                it.remove();
              }
              break;

            case "longVal":
              if (String.valueOf(defaultTB.longVal).equals(entry.getValue())) {
                it.remove();
              }
              break;

            case "stringVal":
              if (Objects.equals(defaultTB.stringVal, entry.getValue())) {
                it.remove();
              }
              break;

            case "class":
              it.remove();
              break;
          }
        }

        if (describe.isEmpty()) {
          return "";
        }

        return describe.toString();
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
        throw new RuntimeException("Could not collect properties from bean", ex);
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      TestBean testBean = (TestBean) o;

      if (intVal != testBean.intVal) {
        return false;
      }
      if (longVal != testBean.longVal) {
        return false;
      }

      return !(stringVal != null
          ? !stringVal.equals(testBean.stringVal)
          : testBean.stringVal != null);
    }

    @Override
    public int hashCode() {
      int result = intVal;
      result = 31 * result + (stringVal != null ? stringVal.hashCode() : 0);
      result = 31 * result + (int) (longVal ^ (longVal >>> 32));
      return result;
    }
  }

  StringCodec.Object2String<TestBean> codec = new StringCodec.Object2String<>();

  @Test
  public void testBeanCodecWithoutConstructorWithoutProperty() throws ClassNotFoundException {
    TestBean expected = new TestBean();
    String bean = codec.toString(expected);
    TestBean actual = codec.fromString(bean);
    Assert.assertEquals("validating the bean", expected, actual);
  }

  @Test
  public void testBeanCodecWithConstructorSet() throws ClassNotFoundException {
    String bean = TestBean.class.getName() + ":testVal";
    TestBean obj = codec.fromString(bean);
    Assert.assertEquals("validating the bean", obj, new TestBean("testVal"));
  }

  @Test
  public void testBeanCodecWithConstructorPropertySet() throws ClassNotFoundException {
    TestBean expectedBean = new TestBean();
    expectedBean.intVal = 10;
    expectedBean.stringVal = "strVal";
    TestBean obj = codec.fromString(codec.toString(expectedBean));

    Assert.assertEquals("validating the bean", obj, expectedBean);
  }

  @Test
  public void testBeanCodecWithConstructorSetEmptyProperties() throws ClassNotFoundException {
    String bean = TestBean.class.getName() + ":testVal:";
    TestBean obj = codec.fromString(bean);
    Assert.assertEquals("validating the bean", obj, new TestBean("testVal"));
  }

  @Test
  public void testBeanCodecOnlyEmptyConstructor() throws ClassNotFoundException {
    String bean = TestBean.class.getName() + ":";
    TestBean obj = codec.fromString(bean);
    Assert.assertEquals("validating the bean", obj, new TestBean());
  }

  @Test
  public void testBeanCodecWithProperty() throws ClassNotFoundException {
    TestBean expectedBean = new TestBean("");
    expectedBean.intVal = 1;
    TestBean obj = codec.fromString(codec.toString(expectedBean));
    Assert.assertEquals("validating the bean", obj, expectedBean);
  }

  @Test
  public void testBeanCodecWithAllProperties() throws ClassNotFoundException {
    TestBean expectedBean = new TestBean("testStr");
    expectedBean.intVal = 1;
    expectedBean.longVal = 10;
    TestBean obj = codec.fromString(codec.toString(expectedBean));
    Assert.assertEquals("validating the bean", obj, expectedBean);
  }

  @Test
  public void testBeanWithWrongClassName() {
    String beanClass = TestBean.class.getName() + "1";
    String bean = beanClass + "::intVal=1";
    try {
      codec.fromString(bean);
      Assert.fail();
    } catch (ClassNotFoundException e) {
      Assert.assertEquals("exception message", beanClass, e.getMessage());
    }
  }

  @Test
  public void testBeanFailure() throws ClassNotFoundException {
    TestBean expectedBean = new TestBean("hello");
    expectedBean.intVal = 1;
    expectedBean.longVal = 10;
    TestBean obj = codec.fromString(codec.toString(expectedBean));
    Assert.assertEquals("validating the bean", obj, expectedBean);
  }

  public static class RegexMatcher extends BaseMatcher<String> {
    private final String regex;

    public RegexMatcher(String regex) {
      this.regex = regex;
    }

    @Override
    public boolean matches(Object o) {
      return ((String) o).matches(regex);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("matches regex=" + regex);
    }

    public static RegexMatcher matches(String regex) {
      return new RegexMatcher(regex);
    }
  }
}
