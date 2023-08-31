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

package org.silverpeas.kernel.test;

import org.silverpeas.kernel.BeanContainer;
import org.silverpeas.kernel.exception.MultipleCandidateException;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A simple implementation of a {@link BeanContainer} to be used in the unit tests.
 */
public class TestScopedBeanContainer implements BeanContainer {

  private final Map<String, Set<?>> beans = new ConcurrentHashMap<>();

  @Override
  public <T> Optional<T> getBeanByName(String name) {
    try {
      Set<?> theBeans = getBeans(name);
      if (theBeans.size() > 1) {
        throw new MultipleCandidateException("Several available beans: " + name);
      }
      return getSingleBean(theBeans);
    } catch (ClassCastException e) {
      return Optional.empty();
    }
  }

  @Override
  public <T> Optional<T> getBeanByType(Class<T> type, Annotation... qualifiers) {
    try {
      Set<?> theBeans = getBeans(type.getName());
      if (theBeans.size() > 1) {
        throw new MultipleCandidateException("Several available beans: " + type.getName());
      }
      return getSingleBean(theBeans);
    } catch (ClassCastException e) {
      return Optional.empty();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Set<T> getAllBeansByType(Class<T> type, Annotation... qualifiers) {
    try {
      Set<?> theBeans = getBeans(type.getName());
      return theBeans.stream().map(b -> (T) b).collect(Collectors.toSet());
    } catch (ClassCastException e) {
      return Set.of();
    }
  }

  private Set<?> getBeans(String key) {
    Set<?> theBeans = beans.get(key);
    return theBeans == null ? Set.of() : theBeans;
  }

  @SuppressWarnings("unchecked")
  private <T> Optional<T> getSingleBean(Set<?> beans) {
    return beans.stream()
        .map(b -> (T) b)
        .findAny();
  }

  public <T> void putBean(Class<T> type, T bean) {
    putBean(type.getName(), bean);
  }

  @SuppressWarnings("unchecked")
  public <T> void putBean(String name, T bean) {
    Set<T> theBeans = (Set<T>) beans.computeIfAbsent(name, k -> new HashSet<>());
    theBeans.add(bean);
  }

  public void clear() {
    beans.clear();
  }
}
