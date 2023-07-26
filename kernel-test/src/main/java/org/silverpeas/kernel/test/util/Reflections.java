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
import org.silverpeas.kernel.annotation.Nullable;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Common reflection utility operations.
 *
 * @author mmoquillon
 */
public final class Reflections {

  private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

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
   * Constructs an instance of the specified concrete class by using its default non-arguments constructor. The
   * constructor can be private, package-private, protected or public.
   *
   * @param clazz the class to instantiate.
   * @param <T> the type of the object to construct.
   * @return the new instance of the given class.
   * @throws SilverpeasReflectionException if an error occurs while instantiating the specified class.
   */
  @NonNull
  public static <T> T instantiate(@NonNull Class<T> clazz) throws SilverpeasReflectionException {
    try {
      Objects.requireNonNull(clazz);
      MethodHandles.Lookup beanTypeLookup = MethodHandles.privateLookupIn(clazz, lookup);
      MethodType defaultConstructor = MethodType.methodType(void.class);
      MethodHandle constructorHandle = beanTypeLookup.findConstructor(clazz, defaultConstructor);
      //noinspection unchecked
      return (T) constructorHandle.invoke();
    } catch (Throwable t) {
      throw new SilverpeasReflectionException(t);
    }
  }

  /**
   * Sets the specified value to the given field of the specified object.
   *
   * @param object the object having the field to set with the given value.
   * @param field the field of the object to set with the given value.
   * @param value the value to set.
   * @throws SilverpeasReflectionException if an error occurs while setting the field of the object with the value.
   */
  public static void setField(@NonNull Object object, @NonNull Field field, @Nullable Object value)
      throws SilverpeasReflectionException {
    try {
      Objects.requireNonNull(object);
      Objects.requireNonNull(field);
      MethodHandles.Lookup fieldLookup = MethodHandles.privateLookupIn(object.getClass(), lookup);
      field.trySetAccessible();
      MethodHandle fieldSetter = fieldLookup.unreflectSetter(field);
      fieldSetter.invoke(object, value);
    } catch (Throwable e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Sets the specified value to the given field of the specified object inherited from the given type of the object.
   *
   * @param type a type the object satisfies and that is extended by its class.
   * @param object the object having the field to set with the given value.
   * @param field the field of the object to set with the given value.
   * @param value the value to set.
   * @throws SilverpeasReflectionException if an error occurs while setting the field of the object with the value.
   */
  public static void setField(@NonNull Class<?> type, @NonNull Object object, @NonNull Field field,
      @Nullable Object value) throws SilverpeasReflectionException {
    try {
      Objects.requireNonNull(type);
      Objects.requireNonNull(object);
      Objects.requireNonNull(field);
      MethodHandles.Lookup fieldLookup = MethodHandles.privateLookupIn(type, lookup);
      field.trySetAccessible();
      MethodHandle fieldSetter = fieldLookup.unreflectSetter(field);
      fieldSetter.invoke(object, value);
    } catch (Throwable e) {
      throw new SilverpeasRuntimeException(e);
    }
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
