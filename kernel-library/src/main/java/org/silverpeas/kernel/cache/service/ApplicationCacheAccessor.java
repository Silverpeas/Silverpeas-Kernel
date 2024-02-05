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

import org.silverpeas.kernel.cache.model.ExternalCache;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * An accessor to a cache whose lifetime is within the whole application runtime. This accessor uses
 * a specific implementation of the cache that is retrieved by using the Java Service Loader
 * mechanism. So, through this accessor you can use tiers caching subsystem like Infinispan or
 * EhCache for example, for better performance. This is why this accessor maintains only a single
 * instance of such a cache. In the case no external cache is available, then by default an
 * in-memory cache is used, meaning no additional cache functionalities (like time-to-live for
 * example) are supported.
 *
 * @author mmoquillon
 * @apiNote The {@link ApplicationCacheAccessor} is a singleton.
 */
public class ApplicationCacheAccessor implements CacheAccessor<ExternalCache> {

  private static final ApplicationCacheAccessor instance = new ApplicationCacheAccessor();

  /**
   * Gets the single instance of {@link ApplicationCacheAccessor}.
   *
   * @return the single instance of this class.
   */
  public static ApplicationCacheAccessor getInstance() {
    return instance;
  }

  private final ExternalCache cache;

  ApplicationCacheAccessor() {
    cache = ServiceLoader.load(ExternalCache.class)
        .findFirst()
        .orElseGet(DefaultCache::new);
  }

  @Override
  public ExternalCache getCache() {
    return cache;
  }

  /**
   * Default cache to use when no external cache is provided by SPI. The default cache stores the
   * data in memory. For doing it wraps an {@link InMemoryCache} instance. No additional cache
   * functionalities (like time-to-live) are implemented by this cache. If these functionalities are
   * used an {@link UnsupportedOperationException} exception is thrown.
   */
  private static class DefaultCache extends ExternalCache {

    private final InMemoryCache cache = new InMemoryCache();

    /**
     * Operation not supported.
     *
     * @see ExternalCache#put(Object, Object, int, int)
     */
    @Override
    public void put(Object key, Object value, int timeToLive, int timeToIdle) {
      throw new UnsupportedOperationException();
    }

    /**
     * Operation not supported.
     *
     * @see ExternalCache#computeIfAbsent(Object, Class, int, int, Supplier)
     */
    @Override
    public <T> T computeIfAbsent(Object key, Class<T> classType, int timeToLive, int timeToIdle,
        Supplier<T> valueSupplier) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String add(Object value) {
      return cache.add(value);
    }

    @Override
    public void put(Object key, Object value) {
      cache.put(key, value);
    }

    @Override
    public <T> T computeIfAbsent(Object key, Class<T> classType, Supplier<T> valueSupplier) {
      return cache.computeIfAbsent(key, classType, valueSupplier);
    }

    @Override
    public void clear() {
      cache.clear();
    }

    @Override
    public Object get(Object key) {
      return cache.get(key);
    }

    @Override
    public Object remove(Object key) {
      return cache.remove(key);
    }

    @Override
    public <T> T remove(Object key, Class<T> classType) {
      return cache.remove(key, classType);
    }

    @Override
    public Map<Object, Object> getAll() {
      return cache.getAll();
    }
  }
}
