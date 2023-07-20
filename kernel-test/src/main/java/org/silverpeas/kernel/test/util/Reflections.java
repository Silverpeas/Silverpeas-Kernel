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

package org.silverpeas.kernel.test.util;

import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.annotation.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Common reflection utility operations.
 *
 * @author mmoquillon
 */
public final class Reflections {

  private Reflections() {
  }

  /**
   * Browses the inheritance graph of the specified type up to his root parent and applies for each parent type the
   * specified type consumer.
   *
   * @param fromType the type from which the inheritance graph is browsed
   * @param consumer a function to apply to each type in the inheritance graph
   */
  public static void loopInheritance(@NonNull final Class<?> fromType, @NonNull final TypeConsumer consumer) {
    Objects.requireNonNull(fromType);
    Objects.requireNonNull(consumer);
    try {
      Class<?> type = fromType;
      while (type != null && !type.isInterface() && !type.equals(Object.class)) {
        consumer.consume(type);
        type = type.getSuperclass();
      }
    } catch (ReflectiveOperationException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Gets all the annotations declared on the specified element and that satisfies the given annotation type.
   *
   * @param element the annotated element.
   * @param annotationType the type of annotation the annotations to return have to satisfy.
   * @return an array with the annotations satisfying the given type.
   */
  @NonNull
  public static Annotation[] getDeclaredAnnotations(@NonNull AnnotatedElement element,
      @NonNull Class<? extends Annotation> annotationType) {
    Objects.requireNonNull(element);
    Objects.requireNonNull(annotationType);
    return Stream.of(element.getDeclaredAnnotations())
        .filter(a -> a.annotationType().getAnnotationsByType(annotationType).length > 0)
        .toArray(Annotation[]::new);
  }

  /**
   * A function to apply on a given type.
   *
   * @author mmoquillon
   */
  @FunctionalInterface
  public interface TypeConsumer {
    void consume(final Class<?> type) throws ReflectiveOperationException;
  }
}
