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

import org.silverpeas.kernel.annotation.Nullable;

import java.util.function.Supplier;

/**
 * A cache is a storage mechanism to store intermediate data got from any subsystems or from
 * external services in order to retrieve them fastly and hence more efficiently. A cache is
 * dedicated to boost performance and to simplify the scalability.
 * <p>
 * This interface defines a simple cache that only provides usual methods expected for a cache.
 * </p>
 *
 * @author Yohann Chastagnier
 * @since 11/09/13
 */
public interface SimpleCache {

  /**
   * Clear the content of the cache.
   */
  void clear();

  /**
   * Gets an element from the cache.
   *
   * @param key the key with which the object to get is mapped in the cache.
   * @return the object mapped with the key or null if no there is no object mapped with the
   * specified key.
   */
  @Nullable
  Object get(Object key);

  /**
   * Is this cache contains an object mapped with the specified key? It uses the
   * {@link SimpleCache#get(Object)} method for doing.
   *
   * @param key the key with which the object to get is mapped in the cache.
   * @return true if there is an mapped with the key. False otherwise.
   * @see SimpleCache#get(Object)
   */
  default boolean has(Object key) {
    return get(key) != null;
  }

  /**
   * Gets a typed element from the cache. Null is returned if an element exists for the given key
   * but the object doesn't satisfy the expected type.
   *
   * @param <T> the concrete type of the object to get.
   * @param key the key with which the object to get is mapped in the cache.
   * @param classType the class type the instance to get as to satisfy.
   * @return the object mapped with the key or null if either there is no object mapped with the
   * specified key or the object doesn't satisfy the expected class type.
   */
  @Nullable
  <T> T get(Object key, Class<T> classType);

  /**
   * Gets a typed element mapped with the specified key from the cache or computes it with the
   * specified value supplier if no such key exists. If an element exists for the given key but the
   * object type doesn't match the expected one, a new computation is performed. If the element
   * returned by the computation is null, then nothing is put into the cache. The value supplier
   * should not modify this cache during computation.
   *
   * @param <T> the concrete type of the object to get.
   * @param key the key with which the object to get is mapped in the cache.
   * @param classType the class of the instance to get.
   * @param valueSupplier the function that will computes the data to put into the cache.
   * @return the object mapped with the key or null if there is no object to map with the specified
   * key.
   */
  @Nullable
  <T> T computeIfAbsent(Object key, Class<T> classType, Supplier<T> valueSupplier);

  /**
   * Removes an element from the cache and return it.
   *
   * @param key the key with which the object to get is mapped in the cache.
   * @return the object removed from the cache.
   */
  Object remove(Object key);

  /**
   * Removes a typed element from the cache and return it. Null is returned if an element exists for
   * the given key but the object type doesn't correspond.
   *
   * @param <T> the concrete type of the object to remove.
   * @param key the key with which the object to get is mapped in the cache.
   * @param classType the class of the instance to remove.
   * @return the object removed from the cache.
   */
  <T> T remove(Object key, Class<T> classType);

  /**
   * Adds a value and generate a unique key to retrieve later the value. After 12 hours without be
   * used the value is trashed.
   *
   * @param value the object to add in the cache.
   * @return the key with which the added object is mapped in the cache.
   */
  String add(Object value);

  /**
   * Puts a value for a given key. After 12 hours without be used the value is trashed.
   *
   * @param key the key with which the object to put has to be mapped
   * @param value the object to put in the cache.
   */
  void put(Object key, Object value);
}
