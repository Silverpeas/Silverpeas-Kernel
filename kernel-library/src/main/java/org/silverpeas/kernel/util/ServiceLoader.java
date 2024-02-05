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
package org.silverpeas.kernel.util;

import org.silverpeas.kernel.SilverpeasRuntimeException;

import javax.annotation.Priority;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service loader which takes into account of the {@link javax.annotation.Priority} annotation to
 * load the correct single implementation of a service interface when several implementations are
 * available. If several implementations are available for a given service and no ones are annotated
 * with {@link javax.annotation.Priority}, then the first found one is loaded. This service
 * loader provides also the possibility to kept in cache the instance of the loaded implementation
 * so that is can be get without having to load it again.
 *
 * @author mmoquillon
 */
public class ServiceLoader {

  private static final Map<String, Factory<?>> loaded = new ConcurrentHashMap<>();

  private ServiceLoader() {
  }

  /**
   * Loads an instance of the specified service by using the Java SPI API. If there are several
   * implementations of the service available in the classpath, then the one with higher priority is
   * got, otherwise this is the first one found. The priority is given with the
   * {@link javax.annotation.Priority} annotation.
   *
   * @param service the service to load.
   * @param <T> the type of the service.
   * @return an instance of the loaded service.
   */
  public static <T> T load(Class<T> service) {
    var implementations = java.util.ServiceLoader.load(service);
    return implementations.stream()
        .filter(p -> p.type().isAnnotationPresent(Priority.class))
        .map(p -> Pair.of(p, p.type().getAnnotation(Priority.class).value()))
        .max(Comparator.comparingInt(Pair::getSecond))
        .map(Pair::getFirst)
        .map(java.util.ServiceLoader.Provider::get)
        .orElseGet(() ->
            implementations.findFirst()
                .orElseThrow(() -> new SilverpeasRuntimeException(
                    "No " + service.getSimpleName() + " found! At least one should be available!"))
        );
  }

  /**
   * Gets an instance of the specified service if already loaded, otherwise loads it by the Java SPI
   * API and caches it so that it can be returned later without loading it again.
   *
   * @param service the service to get.
   * @param <T> the type of the service.
   * @return an instance of the service.
   * @implSpec the application cache is used to maintain the instance in memory all along the
   * application is running.
   * @see ServiceLoader#load(Class)
   */
  public static <T> T get(Class<T> service) {
    Factory<?> factory = loaded.computeIfAbsent(service.getSimpleName() + "#instance",
            k -> new Factory<>(service));
    //noinspection unchecked
    return (T) factory.getInstance();
  }

  /**
   * A factory of objects of the specified type. The factory loads by Java SPI an implementation of
   * the given type and then abstracts the way it constructs an instance of the implementation
   * and the way it keeps it in memory.
   * @param <T> a type type.
   * @implNote for instance, the object created by the factory is kept in memory during the whole
   * runtime. (Another way is to keep it for a given time through a week reference but this
   * solution wasn't chosen.)
   */
  private static class Factory<T> {

    private final T instance;

    private Factory(Class<T> service) {
      this.instance = load(service);
    }

    public T getInstance() {
      return instance;
    }
  }
}
  