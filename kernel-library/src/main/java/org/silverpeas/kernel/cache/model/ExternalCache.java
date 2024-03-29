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
package org.silverpeas.kernel.cache.model;

import org.silverpeas.kernel.cache.service.ApplicationCacheAccessor;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * A more complete cache provided by an external library. The cache loaded and used by the
 * {@link ApplicationCacheAccessor} have to extend this abstract
 * class.
 *
 * @author Yohann Chastagnier
 * @since 11/09/13
 */
public abstract class ExternalCache extends AbstractSimpleCache
    implements Cache {

  // In seconds, 12 hours (60 seconds x 60 minutes x 12 hours)
  private static final int DEFAULT_TIME_TO_IDLE = 60 * 60 * 12;

  @Override
  public String add(final Object value, final int timeToLive) {
    String uniqueKey = UUID.randomUUID().toString();
    put(uniqueKey, value, timeToLive);
    return uniqueKey;
  }

  @Override
  public String add(final Object value, final int timeToLive, final int timeToIdle) {
    String uniqueKey = UUID.randomUUID().toString();
    put(uniqueKey, value, timeToLive, timeToIdle);
    return uniqueKey;
  }

  @Override
  public void put(final Object key, final Object value) {
    put(key, value, 0);
  }

  @Override
  public void put(final Object key, final Object value, final int timeToLive) {
    put(key, value, timeToLive, DEFAULT_TIME_TO_IDLE);
  }

  @Override
  public <T> T computeIfAbsent(final Object key, final Class<T> classType,
      final Supplier<T> valueSupplier) {
    return computeIfAbsent(key, classType, 0, valueSupplier);
  }

  @Override
  public <T> T computeIfAbsent(final Object key, final Class<T> classType, final int timeToLive,
      final Supplier<T> valueSupplier) {
    return computeIfAbsent(key, classType, timeToLive, DEFAULT_TIME_TO_IDLE, valueSupplier);
  }
}
