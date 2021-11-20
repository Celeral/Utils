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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TraverserTest {
  @Test
  public void testEmptyElements() {
    Traverser<String> traverser = new Traverser<>(new String[0], element -> new String[0]);
    Assert.assertFalse("Empty list has elements!", traverser.hasNext());
  }

  @Test
  public void testOneLeaf() {
    final String leaf = "Leaf";
    Traverser<String> traverser = new Traverser<>(new String[] {leaf}, element -> null);
    if (traverser.hasNext()) {
      Assert.assertEquals("Only element", leaf, traverser.next());
    }
    Assert.assertFalse("More than one elements!", traverser.hasNext());
  }

  HashMap<Integer, List<Integer>> map = new HashMap<>();

  public TraverserTest() {
    map.put(1, Collections.singletonList(2));
    map.put(2, Arrays.asList(3, 4));
    map.put(4, Arrays.asList(5, 6, 7, 8));
    map.put(5, Arrays.asList(6, 7, 8, 9, 10));
    map.put(6, Arrays.asList(7, 8, 9, 10));
    map.put(7, Collections.emptyList());
    map.put(8, Arrays.asList(9, 10));
    map.put(9, Collections.singletonList(10));
  }

  @Test
  public void testOnlyLeaves() {
    Traverser<Integer> traverser =
        new Traverser<>(map.keySet().stream().toArray(Integer[]::new), element -> null);
    HashSet<Integer> integers = new HashSet<>(map.size());
    traverser.forEachRemaining(integers::add);
    Assert.assertEquals("Keyset is traversed element set", map.keySet(), integers);
  }

  @Test
  public void testOneChild() {
    Traverser<Integer> traverser =
        new Traverser<>(
            new Integer[] {1, 2, 3},
            element -> {
              if (element.equals(2)) {
                return new Integer[] {4};
              }

              return null;
            });
    HashSet<Integer> integers = new HashSet<>(map.size());
    traverser.forEachRemaining(integers::add);
    Assert.assertEquals("Not more than 4 of them present", 4, integers.size());
    for (int i = 1; i <= 4; i++) {
      Assert.assertTrue("Element " + i, integers.contains(i));
    }
  }

  @Test
  public void test() {

    Traverser<String> traverser =
        new Traverser<String>(
            map.keySet().stream().map(Integer::toUnsignedString).toArray(String[]::new),
            element -> {
              final int lastIndexOf = element.lastIndexOf('.');

              String index;
              if (lastIndexOf == -1) {
                index = element;
              } else {
                index = element.substring(lastIndexOf + 1);
              }

              List<Integer> children = map.get(Integer.valueOf(index));
              if (children == null) {
                return null;
              }

              return children.stream().map(child -> element + '.' + child).toArray(String[]::new);
            });

    ArrayList<String> integers = new ArrayList<>();
    while (traverser.hasNext()) {
      final String next = traverser.next();
      integers.add(next);
    }

    /* this terminating itself is a passing of the test */
  }

  private static final Logger logger = LogManager.getLogger(TraverserTest.class);
}
