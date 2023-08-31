/*
 * Copyright (C) 2000 - 2022-2023 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.kernel.cache.service;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.kernel.util.Mutable;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests the {@link ThreadCacheAccessor} single instance and checks it manages only a single cache
 * per thread.
 *
 * @author mmoquillon
 */
class ThreadCacheAccessorTest {
  private final ThreadCacheAccessor accessor = new ThreadCacheAccessor();
  private static final String Object1 = "";
  private static final Object Object2 = new Object();

  @Test
  void clearCaches() {
    final TestPerformer testPerformer = () -> {
      assertThat(itemsCountInCache(), is(0));
      accessor.getCache().add(Object1);
      accessor.getCache().add(Object2);
      assertThat(itemsCountInCache(), is(2));
      accessor.getCache().clear();
      assertThat(itemsCountInCache(), is(0));
    };
    performTestInTwoThreads(testPerformer, true);
    performTestInTwoThreads(testPerformer, false);
  }

  @Test
  void getCachedObject() {
    final TestPerformer testPerformer = () -> {
      assertThat(itemsCountInCache(), is(0));
      String uniqueKey1 = accessor.getCache().add(Object1);
      assertThat(accessor.getCache().get("dummy"), nullValue());
      assertThat(accessor.getCache().get(uniqueKey1), is(Object1));
      assertThat(accessor.getCache().get(uniqueKey1, Object.class), is(Object1));
      assertThat(accessor.getCache().get(uniqueKey1, String.class), is(Object1));
      assertThat(accessor.getCache().get(uniqueKey1, Number.class), nullValue());
    };
    performTestInTwoThreads(testPerformer, true);
    performTestInTwoThreads(testPerformer, false);
  }

  @Test
  @DisplayName("Put an object with a given cache service and get it with another cache service " +
      "has to work")
  void useTwoCacheServicesInTheSameThread() {
    ThreadCacheAccessor service1 = new ThreadCacheAccessor();
    ThreadCacheAccessor service2 = new ThreadCacheAccessor();

    String key = service1.getCache().add(Object2);
    Object actual = service2.getCache().get(key);
    assertThat(actual, is(Object2));
  }

  @Test
  @DisplayName("Put an object with a given cache service in one thread and get it with another " +
      "cache service in " +
      "another thread doesn't work")
  void useTwoCacheServicesInTwoDifferentThreads() throws InterruptedException {
    Mutable<String> key = Mutable.empty();
    Thread t1 = new Thread(() -> {
      ThreadCacheAccessor service1 = new ThreadCacheAccessor();
      String k = service1.getCache().add(Object2);
      key.set(k);
    });

    Mutable<Object> actual = Mutable.empty();
    Thread t2 = new Thread(() -> {
      ThreadCacheAccessor service2 = new ThreadCacheAccessor();
      Object o = service2.getCache().get(key.get());
      actual.set(o);
    });

    t1.start();
    Awaitility.await().pollDelay(10, TimeUnit.MILLISECONDS).until(() -> true);
    t2.start();
    t1.join();
    t2.join();

    assertThat(actual.orElseGet(() -> null), nullValue());
  }

  @Test
  void addIntoCache() {
    final TestPerformer testPerformer = () -> {
      assertThat(itemsCountInCache(), is(0));
      String uniqueKey1 = accessor.getCache().add(Object1);
      String uniqueKey2 = accessor.getCache().add(Object2);
      assertThat(uniqueKey1, notNullValue());
      assertThat(uniqueKey2, notNullValue());
      assertThat(uniqueKey2, not(is(uniqueKey1)));
      assertThat(itemsCountInCache(), is(2));
    };
    performTestInTwoThreads(testPerformer, true);
    performTestInTwoThreads(testPerformer, false);
  }

  @Test
  void putObjectIntoCacheWithDifferentKeys() {
    final TestPerformer testPerformer = () -> {
      assertThat(itemsCountInCache(), is(0));
      accessor.getCache().put("A", Object1);
      accessor.getCache().put("B", Object2);
      assertThat(itemsCountInCache(), is(2));
    };
    performTestInTwoThreads(testPerformer, true);
    performTestInTwoThreads(testPerformer, false);
  }

  @Test
  void putObjectIntoCacheWithIdenticalKey() {
    final TestPerformer testPerformer = () -> {
      assertThat(itemsCountInCache(), is(0));
      accessor.getCache().put("A", Object1);
      accessor.getCache().put("A", Object2);
      assertThat(itemsCountInCache(), is(1));
    };
    performTestInTwoThreads(testPerformer, true);
    performTestInTwoThreads(testPerformer, false);
  }

  @Test
  void removeObjectFromCache() {
    final TestPerformer testPerformer = () -> {
      assertThat(itemsCountInCache(), is(0));
      String uniqueKey1 = accessor.getCache().add(Object1);
      String uniqueKey2 = accessor.getCache().add(Object2);
      assertThat(itemsCountInCache(), is(2));
      accessor.getCache().remove("lkjlkj");
      assertThat(itemsCountInCache(), is(2));
      accessor.getCache().remove(uniqueKey1, Number.class);
      assertThat(itemsCountInCache(), is(2));
      accessor.getCache().remove(uniqueKey1, Object.class);
      assertThat(itemsCountInCache(), is(1));
      accessor.getCache().remove(uniqueKey2);
      assertThat(itemsCountInCache(), is(0));
    };
    performTestInTwoThreads(testPerformer, true);
    performTestInTwoThreads(testPerformer, false);
  }

  private void performTestInTwoThreads(TestPerformer test, boolean delayBetweenTwoThreads) {
    try {
      RunnableTest runnableTest1 = new RunnableTest(test);
      RunnableTest runnableTest2 = new RunnableTest(test);
      Thread t1 = new Thread(runnableTest1);
      Thread t2 = new Thread(runnableTest2);
      t1.start();
      if (delayBetweenTwoThreads) {
        // await 100ms before starting the second thread
        Awaitility.await().pollDelay(100, TimeUnit.MILLISECONDS).until(() -> true);
      }
      t2.start();
      t1.join();
      t2.join();
      assertThat(runnableTest1.isTestPassed(), is(true));
      assertThat(runnableTest2.isTestPassed(), is(true));
    } catch (InterruptedException e) {
      Assertions.fail(e);
    }
  }

  private int itemsCountInCache() {
    return ((ThreadCache) accessor.getCache()).getAll().size();
  }

  private interface TestPerformer {
    void perform();
  }

  private static class RunnableTest implements Runnable {

    private final TestPerformer test;
    private boolean testPassed = false;


    protected RunnableTest(final TestPerformer test) {
      this.test = test;
    }

    @Override
    public void run() {
      test.perform();
      testPassed = true;
    }

    private boolean isTestPassed() {
      return testPassed;
    }
  }
}
