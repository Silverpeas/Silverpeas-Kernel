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

import org.silverpeas.kernel.cache.model.SimpleCache;

/**
 * A service managing the life-cycle of a given type of caches. The service can manage a single or
 * several caches. A cache service should be a singleton.
 *
 * @param <T> the concrete type of {@link SimpleCache}
 * @author mmoquillon
 */
public interface CacheService<T extends SimpleCache> {

  /**
   * Gets a cache from this service. The returned cache is either the same instance at each call or
   * a new one, in this case the new instance is kept to be tracked by the service.
   *
   * @return either a new cache or a single one according to the policy of the service about the
   * cache(s) on which it works.
   */
  T getCache();

  /**
   * Clears all the caches on which this service works.
   */
  void clearAllCaches();

}
