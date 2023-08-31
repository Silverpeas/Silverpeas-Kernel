/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.kernel.cache.model.AbstractSimpleCache;

import java.util.HashMap;
import java.util.Map;

/**
 * A cache per-thread. It uses the {@link ThreadLocal} mechanism to access data in the current
 * thread's cache.
 *
 * @author Yohann Chastagnier
 * @since 25/10/13
 */
class ThreadCache extends AbstractSimpleCache {

  private static final ThreadLocal<Map<Object, Object>> cache = new ThreadLocal<>();

  ThreadCache() {
  }

  /**
   * Gets all the content of the cache as a dictionary.
   *
   * @return a {@link Map} with all the key/value pairs stored in the cache.
   */
  @Override
  public Map<Object, Object> getAll() {
    Map<Object, Object> threadCache = cache.get();
    if (threadCache == null) {
      threadCache = new HashMap<>();
      cache.set(threadCache);
    }
    return threadCache;
  }

  @Override
  public void clear() {
    cache.remove();
  }

  @Override
  public Object get(final Object key) {
    return getAll().get(key);
  }

  @Override
  public Object remove(final Object key) {
    Object value = get(key);
    removeIfPresent(key, value);
    return value;
  }

  @Override
  public <T> T remove(final Object key, final Class<T> classType) {
    T value = get(key, classType);
    removeIfPresent(key, value);
    return value;
  }

  @Override
  public void put(final Object key, final Object value) {
    getAll().put(key, value);
  }

  private void removeIfPresent(Object key, Object value) {
    if (value != null) {
      getAll().remove(key);
    }
  }
}
