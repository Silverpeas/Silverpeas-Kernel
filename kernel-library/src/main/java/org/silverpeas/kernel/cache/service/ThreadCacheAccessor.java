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

import org.silverpeas.kernel.cache.model.SimpleCache;

/**
 * An accessor to the cache of the current thread. This means the data are per-thread cached and
 * their lifetime doesn't span over the life of the thread.
 * <p>
 * <strong>BE VERY VERY CAREFUL:</strong> with thread pool management, the thread is usually never
 * killed because thread creation is time consuming. In pool management the threads are reused to
 * perform another treatment. So, the cache is never by default cleared. This is why you have to
 * clear explicitly the cache with {@link SimpleCache#clear()} once you're done with it.
 *
 * @author mmoquillon
 */
public class ThreadCacheAccessor implements CacheAccessor<SimpleCache> {

  private static final ThreadCacheAccessor instance = new ThreadCacheAccessor();

  /**
   * Gets the single instance of {@link ThreadCacheAccessor}.
   *
   * @return the single instance of this class.
   */
  public static ThreadCacheAccessor getInstance() {
    return instance;
  }

  private final ThreadCache cache = new ThreadCache();

  ThreadCacheAccessor() {
  }

  @Override
  public SimpleCache getCache() {
    return cache;
  }
}
