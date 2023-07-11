/*
 * Copyright (C) 2000 - 2023 Silverpeas
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
 * Unit tests on the {@link ApplicationCacheService} using under the hood a single
 * {@link InMemoryCache} service.getCache().
 * @author mmoquillon
 */
class ApplicationCacheServiceTest {

  private static final ApplicationCacheService service = ApplicationCacheService.getInstance();

  private static final String Object1 = "";
  private static final Object Object2 = new Object();

  @AfterEach
  void clearAllCaches() {
    service.clearAllCaches();
  }

  @Test
  void ensureApplicationCacheServiceIsASingleton() {
    ApplicationCacheService cacheService1 = ApplicationCacheService.getInstance();
    ApplicationCacheService cacheService2 = ApplicationCacheService.getInstance();
    assertThat(cacheService2, is(cacheService1));
  }

  @Test
  void ensureASingleCacheIsUsed() {
    Cache cache1 = service.getCache();
    Cache cache2 = service.getCache();
    assertThat(cache2, is(cache1));
  }

  @Test
  void ensureASingleCacheIsUsedAmongDifferentThreads() throws InterruptedException {
    Mutable<Cache> cache1 = Mutable.empty();
    Mutable<Cache> cache2 = Mutable.empty();

    Thread t1 = new Thread(() -> cache1.set(service.getCache()));
    Thread t2 = new Thread(() -> cache2.set(service.getCache()));
    t1.start();
    t2.start();
    t1.join();
    t2.join();

    assertThat(cache2, is(cache1));
  }

  @Test
  void clearCache() {
    String key1 = service.getCache().add(Object1);
    String key2 = service.getCache().add(Object2);
    assertThat(itemsCountInCache(), is(2));
    assertThat(service.getCache().get(key1), is(Object1));
    assertThat(service.getCache().get(key2), is(Object2));

    service.getCache().clear();
    assertThat(itemsCountInCache(), is(0));
    assertThat(service.getCache().get(key1), nullValue());
    assertThat(service.getCache().get(key1), nullValue());
  }

  @Test
  void getCachedObject() {
    String uniqueKey1 = service.getCache().add(Object1);
    assertThat(service.getCache().get("dummy"), nullValue());
    assertThat(service.getCache().get(uniqueKey1), is(Object1));
    assertThat(service.getCache().get(uniqueKey1, Object.class), is(Object1));
    assertThat(service.getCache().get(uniqueKey1, String.class), is(Object1));
    assertThat(service.getCache().get(uniqueKey1, Number.class), nullValue());
  }

  @Test
  void addIntoCache() {
    String uniqueKey1 = service.getCache().add(Object1);
    String uniqueKey2 = service.getCache().add(Object2);
    assertThat(uniqueKey1, notNullValue());
    assertThat(uniqueKey2, notNullValue());
    assertThat(uniqueKey2, not(is(uniqueKey1)));
  }

  @Test
  void addIntoCacheWithExplicitLiveExpiry() {
    ExternalCache cache = service.getCache();
    assertThrows(UnsupportedOperationException.class, () -> cache.add(Object1, 1));
  }

  @Test
  void addIntoCacheWithExplicitIdleExpiry() {
    ExternalCache cache = service.getCache();
    assertThrows(UnsupportedOperationException.class, () -> cache.add(Object1, 5, 1));
  }

  @Test
  void putObjectIntoCacheWithDifferentKeys() {
    service.getCache().put("A", Object1);
    service.getCache().put("B", Object2);
    assertThat(service.getCache().get("A"), is(Object1));
    assertThat(service.getCache().get("B"), is(Object2));
  }

  @Test
  void putObjectIntoCacheWithIdenticalKey() {
    service.getCache().put("A", Object1);
    service.getCache().put("A", Object2);
    assertThat(service.getCache().get("A"), is(Object2));
  }

  @Test
  void putObjectIntoCacheWithExplicitLiveExpiry() {
    ExternalCache cache = service.getCache();
    assertThrows(UnsupportedOperationException.class, () -> cache.put("A", Object1, 1));
  }

  @Test
  void putObjectIntoCacheWithExplicitIdleExpiry() {
    ExternalCache cache = service.getCache();
    assertThrows(UnsupportedOperationException.class, () -> cache.put("A", Object1, 5, 1));
  }

  @Test
  void removeObjectFromCache() {
    String uniqueKey1 = service.getCache().add(Object1);
    String uniqueKey2 = service.getCache().add(Object2);
    assertThat(itemsCountInCache(), is(2));
    service.getCache().remove("lkjlkj");
    assertThat(itemsCountInCache(), is(2));
    service.getCache().remove(uniqueKey1, Number.class);
    assertThat(itemsCountInCache(), is(2));
    service.getCache().remove(uniqueKey1, Object.class);
    assertThat(itemsCountInCache(), is(1));
    service.getCache().remove(uniqueKey2);
    assertThat(itemsCountInCache(), is(0));
  }
  
  private int itemsCountInCache() {
    return service.getCache().getAll().size();
  }
}