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

package org.silverpeas.kernel;

import org.silverpeas.kernel.test.TestScopedBeanContainer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A feeder of beans to manage in the underlying IoC subsystem. The subsystem is implemented here by
 * {@link TestScopedBeanContainer}. This feeder is to be used by the unit tests to register the
 * beans required by them for the test passes successfully.
 */
public final class ManagedBeanFeeder {

  private final BeanContainer container = ManagedBeanProvider.getInstance().beanContainer();

  public <T, U extends T> void manageBeanForType(final Class<U> beanClass, final Class<T> type) {
    try {
      Constructor<U> constructor = beanClass.getConstructor();
      constructor.trySetAccessible();
      U bean = constructor.newInstance();
      getBeanContainer().putBean(type, bean);
    } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
             InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  public <T, U extends T> void manageBeanWithName(final Class<U> beanClass, final String name) {
    try {
      U bean = instantiate(beanClass);
      getBeanContainer().putBean(name, bean);
    } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
             InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  public void clearAllManagedBeans() {
    getBeanContainer().clear();
  }

  private static <T, U extends T> U instantiate(Class<U> beanClass) throws NoSuchMethodException,
      InstantiationException, IllegalAccessException, InvocationTargetException {
    Constructor<U> constructor = beanClass.getConstructor();
    constructor.trySetAccessible();
    return constructor.newInstance();
  }

  private TestScopedBeanContainer getBeanContainer() {
    return (TestScopedBeanContainer) container;
  }
}
