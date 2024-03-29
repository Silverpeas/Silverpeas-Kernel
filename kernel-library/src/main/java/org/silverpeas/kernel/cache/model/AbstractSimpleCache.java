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

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Common implementation of a {@link SimpleCache} from which a more concrete solution can be
 * extended.
 *
 * @author Yohann Chastagnier
 * @since 25/10/13
 */
public abstract class AbstractSimpleCache implements SimpleCache {

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(final Object key, final Class<T> classType) {
    Object value = get(key);
    if (value == null || !classType.isAssignableFrom(value.getClass())) {
      return null;
    }
    return (T) value;
  }

  @Override
  public <T> T computeIfAbsent(final Object key, final Class<T> classType,
      final Supplier<T> valueSupplier) {
    Objects.requireNonNull(valueSupplier);
    T value;
    if ((value = get(key, classType)) == null && (value = valueSupplier.get()) != null) {
      put(key, value);
    }
    return value;
  }

  @Override
  public String add(final Object value) {
    String uniqueKey = UUID.randomUUID().toString();
    put(uniqueKey, value);
    return uniqueKey;
  }

  /**
   * Gets the whole content of the cache.
   *
   * @return a {@link Map} with all the items in the cache, mapped each of them by their key.
   */
  public abstract Map<Object, Object> getAll();
}
