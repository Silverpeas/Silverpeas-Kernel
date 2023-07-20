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

package org.silverpeas.kernel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.kernel.cache.service.ThreadCacheService;
import org.silverpeas.kernel.util.Mutable;

import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test on the providing of managed beans.
 *
 * @author mmoquillon
 */
class ManagedBeanProviderTest {

  private final ManagedBeanFeeder feeder = new ManagedBeanFeeder();
  private final ManagedBeanProvider provider = ManagedBeanProvider.getInstance();

  private final ThreadCacheService service = new ThreadCacheService();

  @AfterEach
  void cleanUpAllManagedBeans() {
    feeder.clearAllManagedBeans();
    service.clearAllCaches();
  }

  @Test
  void getExistingManagedBeanByName() {
    final String name = "foo";
    feeder.manageBeanWithName(MyBean1.class, name);
    Mutable<MyBean1> bean = Mutable.empty();
    assertDoesNotThrow(() -> bean.set(provider.getManagedBean(name)));
    assertThat(bean.isPresent(), is(true));
    assertThat(bean.get(), instanceOf(MyBean1.class));
  }

  @Test
  void getNonExistingManagedBeanByName() {
    feeder.manageBeanWithName(MyBean1.class, "toto");
    assertThrows(IllegalStateException.class, () -> provider.getManagedBean("foo"));
  }

  @Test
  void getExistingSingleInstanceByName() {
    final String name = "foo";
    feeder.manageBeanWithName(MyBean1.class, name);
    Mutable<MyBean1> bean = Mutable.empty();
    assertDoesNotThrow(() -> bean.set(provider.getSingleInstance(name)));
    assertThat(bean.isPresent(), is(true));
    assertThat(bean.get(), instanceOf(MyBean1.class));
  }

  @Test
  void getNonExistingSingleInstanceByName() {
    feeder.manageBeanWithName(MyBean1.class, "toto");
    assertThrows(IllegalStateException.class, () -> provider.getSingleInstance("foo"));
  }

  @Test
  void getAlreadyCreatedSingleInstanceByName() {
    final String name = "foo";
    MyBean1 expected = new MyBean1();
    String key = computeCacheKey(name);
    service.getCache().put(key, expected);

    MyBean1 actual = provider.getSingleInstance(name);
    assertThat(actual, is(expected));
  }

  @Test
  void getExistingManagedBeanByType() {
    feeder.manageBeanForType(MyBean2.class, MyBean2.class);
    Mutable<MyBean2> bean = Mutable.empty();
    assertDoesNotThrow(() -> bean.set(provider.getManagedBean(MyBean2.class)));
    assertThat(bean.isPresent(), is(true));
    assertThat(bean.get(), instanceOf(MyBean2.class));
  }

  @Test
  void getNonExistingManagedBeanByType() {
    feeder.manageBeanForType(MyBean2.class, MyBean.class);
    assertThrows(IllegalStateException.class, () -> provider.getManagedBean(MyBean1.class));
  }

  @Test
  void getExistingSingleInstanceByType() {
    feeder.manageBeanForType(MyBean1.class, MyBean1.class);
    Mutable<MyBean1> bean = Mutable.empty();
    assertDoesNotThrow(() -> bean.set(provider.getSingleInstance(MyBean1.class)));
    assertThat(bean.isPresent(), is(true));
    assertThat(bean.get(), instanceOf(MyBean1.class));
  }

  @Test
  void getNonExistingSingleInstanceByType() {
    feeder.manageBeanForType(MyBean2.class, MyBean.class);
    assertThrows(IllegalStateException.class, () -> provider.getSingleInstance(MyBean1.class));
  }

  @Test
  void getAlreadyCreatedSingleInstanceByType() {
    MyBean1 expected = new MyBean1();
    String key = computeCacheKey(expected.getClass().getName());
    service.getCache().put(key, expected);

    MyBean1 actual = provider.getSingleInstance(expected.getClass());
    assertThat(actual, is(expected));
  }

  @Test
  void getSingleInstanceByNameOfANonSingleton() {
    final String name = "foo";
    feeder.manageBeanWithName(MyBean2.class, name);

    assertThrows(IllegalStateException.class, () -> provider.getSingleInstance(name));
  }

  @Test
  void getSingleInstanceByTypeOfANonSingleton() {
    feeder.manageBeanForType(MyBean2.class, MyBean2.class);

    assertThrows(IllegalStateException.class, () -> provider.getSingleInstance(MyBean2.class));
  }

  @Test
  void getAllManagedBeans() {
    feeder.manageBeanForType(MyBean2.class, MyBean.class);
    feeder.manageBeanForType(MyBean1.class, MyBean.class);

    Set<MyBean> beans = provider.getAllManagedBeans(MyBean.class);
    assertThat(beans, notNullValue());
    assertThat(beans.size(), is(2));
    assertThat(beans.stream()
        .map(MyBean::getClass)
        .allMatch(c -> c.equals(MyBean1.class) || c.equals(MyBean2.class)), is(true));
  }

  @Test
  void getNoManagedBeans() {
    Set<MyBean> beans = provider.getAllManagedBeans(MyBean.class);
    assertThat(beans, notNullValue());
    assertThat(beans.isEmpty(), is(true));
  }

  private String computeCacheKey(@SuppressWarnings("SameParameterValue") final String postfix) {
    return ManagedBeanProvider.CACHE_KEY_PREFIX + postfix;
  }
}