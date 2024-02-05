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
 * An accessor to a cache. A cache accessor should be a singleton. Each type of cache should have
 * their own accessor and this is by the accessor a cache is got. The accessor is a way to abstract
 * the life-cycle of a cache as this life-cycle shouldn't be managed by the clients but by the
 * Silverpeas Cache API implementation itself.
 *
 * @param <T> the concrete type of {@link SimpleCache}
 * @author mmoquillon
 */
public interface CacheAccessor<T extends SimpleCache> {

  /**
   * Gets an instance of the cache referred by this accessor. Despite the method can return at each
   * call either the same instance or different instances, the cache represented by the instances
   * will be the same.
   *
   * @return the cache referred by this accessor.
   */
  T getCache();

}
