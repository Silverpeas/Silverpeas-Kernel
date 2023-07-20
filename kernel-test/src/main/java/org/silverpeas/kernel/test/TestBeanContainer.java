/*
 * Copyright (C) 2000 - 2022-2023 Silverpeas
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
package org.silverpeas.kernel.test;

import org.mockito.internal.util.MockUtil;
import org.silverpeas.kernel.BeanContainer;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.annotation.NonNull;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of the {@link BeanContainer} interface dedicated to the unit tests implying and requiring an IoC/IoD
 * container. It plays like a simple smart stub of a true IoC container.
 * <p>
 * It provides additional methods allowing to register in the container the classes of beans or to put directly into the
 * container the beans to manage. If a class is put into the container, then both the instantiation and its
 * initialization is managed by the container. If a bean is explicitly put into the container, then only its
 * initialization is taken in charge by the container. The initialization of a bean consists of its dependency
 * resolution and of the invocation of its {@link PostConstruct} annotated method (in case there is one). When the
 * container is cleared, an invocation of the @{@link PreDestroy} annotated method is invoked for each bean before to be
 * cleared (in case they have one). This implementation of the {@link BeanContainer} is built upon some assumptions to
 * be kept simple and usable in unit tests:
 * </p>
 * <ul>
 *   <li>Only one bean is managed for a given type and for all the parents of this type.</li>
 *   <li>Only the default constructor without parameters is supported. For classes with a constructor having the
 *   parameters to be resolved by the container, it is recommended to calls it explicitly in the test and to put the
 *   created instance directly into the container.</li>
 *   <li>The beans aren't proxied, meaning no resolution of the parameters is performed when a method of a managed
 *   bean is invoked. This has to be taken in charge by the test itself.</li>
 *   <li>The {@link javax.inject.Provider} implementations aren't supported</li>
 * </ul>
 *
 * @author mmoquillon
 */
public class TestBeanContainer implements BeanContainer {
  private final Map<String, Set<ElectiveBean<?>>> container = new ConcurrentHashMap<>();

  @Override
  public <T> Optional<T> getBeanByName(String name) throws IllegalStateException {
    try {
      Set<ElectiveBean<T>> theBeans = getBeans(name);
      checkUniqueness(theBeans, name);
      ElectiveBean<T> theBean = theBeans.iterator().next();
      return Optional.of(theBean.make());
    } catch (ClassCastException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public <T> Optional<T> getBeanByType(Class<T> type, Annotation... qualifiers) throws IllegalStateException {
    try {
      Set<ElectiveBean<T>> theBeans = getBeans(type.getName(), qualifiers);
      checkUniqueness(theBeans, type.getName());
      ElectiveBean<T> theBean = theBeans.iterator().next();
      return Optional.of(theBean.make());
    } catch (ClassCastException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public <T> Set<T> getAllBeansByType(Class<T> type, Annotation... qualifiers) {
    try {
      Set<ElectiveBean<T>> theBeans = getBeans(type.getName(), qualifiers);
      return theBeans.stream()
          .map(ElectiveBean::make)
          .collect(Collectors.toSet());
    } catch (ClassCastException e) {
      throw new IllegalStateException(e);
    }
  }

  private <T> Set<ElectiveBean<T>> getBeans(String key, Annotation... qualifiers) {
    Set<ElectiveBean<?>> electiveBeans = container.get(key);
    if (electiveBeans == null || electiveBeans.isEmpty()) {
      throw new IllegalStateException("No bean found: " + key);
    }
    //noinspection unchecked
    Set<ElectiveBean<T>> beans = electiveBeans.stream()
        .filter(e -> e.satisfies(qualifiers))
        .map(e -> (ElectiveBean<T>) e)
        .collect(Collectors.toSet());
    if (beans.isEmpty()) {
      String q = qualifiersToMsg(qualifiers);
      throw new IllegalStateException("No bean found: " + key + q);
    }
    return beans;
  }

  /**
   * Puts directly into the container the specified bean and with the given name so that the bean could be retrieved
   * later with that name.
   *
   * @param bean the bean to manage.
   * @param name the name inder which the bean will be put into the container.
   */
  public void putBean(Object bean, String name) {
    Set<ElectiveBean<?>> beans = container.computeIfAbsent(name, k -> new HashSet<>());
    //noinspection unchecked
    Class<Object> type = (Class<Object>) bean.getClass();
    var electiveBean = new ElectiveBean<>(bean, type);
    beans.add(electiveBean);
  }

  /**
   * Asks to manage the specified class of beans for the given type and with the specified qualifiers.
   *
   * @param beanType the class of beans to put into the container at instantiation time. Only one bean will be created
   * on demand.
   * @param type the type for which the beans will be created and retrieved later.
   * @param qualifiers zero, one or more annotations qualifying the beans put into the container in order to be
   * retrieved later with these qualifiers.
   */
  public <T> void putBean(Class<T> beanType, Class<? super T> type, Annotation... qualifiers) {
    Set<ElectiveBean<?>> beans = container.computeIfAbsent(type.getName(), k -> new HashSet<>());
    beans.add(new ElectiveBean<>(beanType, qualifiers));
  }

  /**
   * Puts directly into the container the specified bean for the given type and with the specified qualifiers.
   *
   * @param bean the bean to manage.
   * @param type the type for which the bean will be retrieved later.
   * @param qualifiers zero, one or more annotations qualifying the bean put into the container in order to be retrieved
   * later with these qualifiers.
   * @param <T> the concrete type of the bean.
   */
  public <T> void putBean(T bean, Class<T> type, Annotation... qualifiers) {
    Set<ElectiveBean<?>> beans = container.computeIfAbsent(type.getName(), k -> new HashSet<>());
    beans.add(new ElectiveBean<>(bean, type, qualifiers));
  }

  /**
   * Clears the container. The @{@link PreDestroy} annotated method of each managed bean (if such a method exists) will
   * be invoked before being wiped out.
   */
  public void clear() {
    container.values().stream()
        .flatMap(Collection::stream)
        .forEach(ElectiveBean::dispose);
    container.clear();
  }

  private static <T> void checkUniqueness(Set<ElectiveBean<T>> theBeans, String key, Annotation... qualifiers) {
    if (theBeans.size() > 1) {
      String q = qualifiersToMsg(qualifiers);
      throw new IllegalStateException("More than one elective bean available: " + key + q);
    }
  }

  private static String qualifiersToMsg(Annotation... qualifiers) {
    String q = "";
    if (qualifiers.length > 0) {
      q = " with qualifiers " +
          Stream.of(qualifiers)
              .map(Annotation::toString)
              .collect(Collectors.joining(", "));
    }
    return q;
  }

  private static class ElectiveBean<T> {
    private final Set<Annotation> qualifiers;
    private final Class<T> type;
    private T bean = null;

    private ElectiveBean(@NonNull Class<T> type, @NonNull Annotation... qualifiers) {
      Objects.requireNonNull(type);
      Objects.requireNonNull(qualifiers);
      this.qualifiers = new HashSet<>(List.of(qualifiers));
      this.type = type;
    }

    private ElectiveBean(@NonNull T bean, @NonNull Class<T> type, @NonNull Annotation... qualifiers) {
      this(type, qualifiers);
      Objects.requireNonNull(bean);
      this.bean = bean;
      if (!MockUtil.isMock(bean)) {
        DependencyResolver resolver = DependencyResolver.get();
        resolver.resolve(bean);
        invokePostConstruction(bean);
      }
    }

    boolean satisfies(Annotation... qualifiers) {
      List<Annotation> q = List.of(qualifiers);
      return (this.qualifiers.isEmpty() && q.isEmpty()) || this.qualifiers.containsAll(q);
    }

    @NonNull
    synchronized T make() {
      if (bean != null) {
        return bean;
      }
      try {
        Constructor<T> constructor = type.getDeclaredConstructor();
        constructor.trySetAccessible();
        bean = constructor.newInstance();
        DependencyResolver resolver = DependencyResolver.get();
        resolver.resolve(bean);
        invokePostConstruction(bean);
        return bean;
      } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
               InvocationTargetException e) {
        throw new IllegalStateException("A default constructor without any parameters should be available");
      }
    }

    void dispose() {
      if (bean != null) {
        invokePreDestruction(bean);
        bean = null;
      }
    }

    private void invokePostConstruction(final Object bean) {
      Method[] methods = bean.getClass().getDeclaredMethods();
      for (Method method : methods) {
        if (method.isAnnotationPresent(PostConstruct.class)) {
          method.trySetAccessible();
          try {
            method.invoke(bean);
          } catch (IllegalAccessException | InvocationTargetException e) {
            throw new SilverpeasRuntimeException(e);
          }
          break;
        }
      }
    }

    private void invokePreDestruction(final Object bean) {
      try {
        Method[] methods = bean.getClass().getDeclaredMethods();
        for (Method method : methods) {
          if (method.isAnnotationPresent(PreDestroy.class)) {
            method.trySetAccessible();
            method.invoke(bean);
            break;
          }
        }
      } catch (InvocationTargetException | IllegalAccessException e) {
        throw new SilverpeasRuntimeException(e);
      }
    }
  }
}
