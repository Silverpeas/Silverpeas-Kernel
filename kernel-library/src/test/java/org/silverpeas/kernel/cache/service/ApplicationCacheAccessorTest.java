/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.kernel.cache.model.Cache;
import org.silverpeas.kernel.cache.model.ExternalCache;
import org.silverpeas.kernel.util.Mutable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests on the {@link ApplicationCacheAccessor} using under the hood a single
 * {@link InMemoryCache} service.getCache().
 * @author mmoquillon
 */
class ApplicationCacheAccessorTest {

  private static final ApplicationCacheAccessor accessor = ApplicationCacheAccessor.getInstance();

  private static final String Object1 = "";
  private static final Object Object2 = new Object();

  @AfterEach
  void clearAllCaches() {
    accessor.getCache().clear();
  }

  @Test
  void ensureApplicationCacheServiceIsASingleton() {
    ApplicationCacheAccessor cacheService1 = ApplicationCacheAccessor.getInstance();
    ApplicationCacheAccessor cacheService2 = ApplicationCacheAccessor.getInstance();
    assertThat(cacheService2, is(cacheService1));
  }

  @Test
  void ensureASingleCacheIsUsed() {
    Cache cache1 = accessor.getCache();
    Cache cache2 = accessor.getCache();
    assertThat(cache2, is(cache1));
  }

  @Test
  void ensureASingleCacheIsUsedAmongDifferentThreads() throws InterruptedException {
    Mutable<Cache> cache1 = Mutable.empty();
    Mutable<Cache> cache2 = Mutable.empty();

    Thread t1 = new Thread(() -> cache1.set(accessor.getCache()));
    Thread t2 = new Thread(() -> cache2.set(accessor.getCache()));
    t1.start();
    t2.start();
    t1.join();
    t2.join();

    assertThat(cache2, is(cache1));
  }

  @Test
  void clearCache() {
    String key1 = accessor.getCache().add(Object1);
    String key2 = accessor.getCache().add(Object2);
    assertThat(itemsCountInCache(), is(2));
    assertThat(accessor.getCache().get(key1), is(Object1));
    assertThat(accessor.getCache().get(key2), is(Object2));

    accessor.getCache().clear();
    assertThat(itemsCountInCache(), is(0));
    assertThat(accessor.getCache().get(key1), nullValue());
    assertThat(accessor.getCache().get(key1), nullValue());
  }

  @Test
  void getCachedObject() {
    String uniqueKey1 = accessor.getCache().add(Object1);
    assertThat(accessor.getCache().get("dummy"), nullValue());
    assertThat(accessor.getCache().get(uniqueKey1), is(Object1));
    assertThat(accessor.getCache().get(uniqueKey1, Object.class), is(Object1));
    assertThat(accessor.getCache().get(uniqueKey1, String.class), is(Object1));
    assertThat(accessor.getCache().get(uniqueKey1, Number.class), nullValue());
  }

  @Test
  void addIntoCache() {
    String uniqueKey1 = accessor.getCache().add(Object1);
    String uniqueKey2 = accessor.getCache().add(Object2);
    assertThat(uniqueKey1, notNullValue());
    assertThat(uniqueKey2, notNullValue());
    assertThat(uniqueKey2, not(is(uniqueKey1)));
  }

  @Test
  void addIntoCacheWithExplicitLiveExpiry() {
    ExternalCache cache = accessor.getCache();
    assertThrows(UnsupportedOperationException.class, () -> cache.add(Object1, 1));
  }

  @Test
  void addIntoCacheWithExplicitIdleExpiry() {
    ExternalCache cache = accessor.getCache();
    assertThrows(UnsupportedOperationException.class, () -> cache.add(Object1, 5, 1));
  }

  @Test
  void putObjectIntoCacheWithDifferentKeys() {
    accessor.getCache().put("A", Object1);
    accessor.getCache().put("B", Object2);
    assertThat(accessor.getCache().get("A"), is(Object1));
    assertThat(accessor.getCache().get("B"), is(Object2));
  }

  @Test
  void putObjectIntoCacheWithIdenticalKey() {
    accessor.getCache().put("A", Object1);
    accessor.getCache().put("A", Object2);
    assertThat(accessor.getCache().get("A"), is(Object2));
  }

  @Test
  void putObjectIntoCacheWithExplicitLiveExpiry() {
    ExternalCache cache = accessor.getCache();
    assertThrows(UnsupportedOperationException.class, () -> cache.put("A", Object1, 1));
  }

  @Test
  void putObjectIntoCacheWithExplicitIdleExpiry() {
    ExternalCache cache = accessor.getCache();
    assertThrows(UnsupportedOperationException.class, () -> cache.put("A", Object1, 5, 1));
  }

  @Test
  void removeObjectFromCache() {
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
  }
  
  private int itemsCountInCache() {
    return accessor.getCache().getAll().size();
  }
}