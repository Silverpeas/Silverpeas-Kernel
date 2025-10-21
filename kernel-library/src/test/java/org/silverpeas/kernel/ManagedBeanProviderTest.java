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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.kernel.cache.model.SimpleCache;
import org.silverpeas.kernel.exception.MultipleCandidateException;
import org.silverpeas.kernel.exception.NotFoundException;
import org.silverpeas.kernel.util.Mutable;

import java.lang.ref.SoftReference;
import java.util.Objects;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.silverpeas.kernel.ManagedBeanProviderTest.IsCached.isCachedUnder;

/**
 * Unit test on the providing of managed beans.
 *
 * @author mmoquillon
 */
class ManagedBeanProviderTest {

  private final ManagedBeanFeeder feeder = new ManagedBeanFeeder();
  private final ManagedBeanProvider provider = ManagedBeanProvider.getInstance();
  private final SimpleCache cache = provider.getCache();

  @AfterEach
  void cleanUpAllManagedBeans() {
    feeder.clearAllManagedBeans();
    cache.clear();
  }

  @Test
  @DisplayName("Getting by name managed bean should return it without caching it")
  void getExistingManagedBeanByName() {
    final String name = "foo";
    feeder.manageBeanWithName(MyBean2.class, name);
    Mutable<MyBean> bean = Mutable.empty();
    assertDoesNotThrow(() -> bean.set(provider.getManagedBean(name)));
    assertThat(bean.isPresent(), is(true));
    assertThat(bean.get(), instanceOf(MyBean2.class));
    assertThat(bean.get(), not(isCachedUnder(computeCacheKey(name))));
  }

  @Test
  @DisplayName("Getting by name non managed bean should throw a NotFoundException")
  void getNonExistingManagedBeanByName() {
    feeder.manageBeanWithName(MyBean1.class, "toto");
    assertThrows(NotFoundException.class, () -> provider.getManagedBean("foo"));
  }

  @Test
  @DisplayName("Getting by name a managed single instance of a singleton should return it after " +
      "caching it")
  void getExistingSingleInstanceByNameShouldCacheIt() {
    final String name = "foo";
    feeder.manageBeanWithName(MyBean1.class, name);
    Mutable<MyBean> bean = Mutable.empty();
    assertDoesNotThrow(() -> bean.set(provider.getManagedBean(name)));
    assertThat(bean.isPresent(), is(true));
    assertThat(bean.get(), instanceOf(MyBean.class));
    assertThat(bean.get(), isCachedUnder(computeCacheKey(name)));
  }

  @Test
  @DisplayName("Getting by name a managed cacheable bean should return it after caching it")
  void getExistingCacheableBeanByNameShouldCacheIt() {
    final String name = "foo";
    feeder.manageBeanWithName(MyBean3.class, name);
    Mutable<MyBean> bean = Mutable.empty();
    assertDoesNotThrow(() -> bean.set(provider.getManagedBean(name)));
    assertThat(bean.isPresent(), is(true));
    assertThat(bean.get(), instanceOf(MyBean3.class));
    assertThat(bean.get(), isCachedUnder(computeCacheKey(name)));
  }

  @Test
  @DisplayName("Getting by name an already cached single instance of a singleton should return it" +
      " directly")
  void getAlreadyCreatedSingleInstanceByName() {
    final String name = "foo";
    MyBean1 expected = new MyBean1();
    String key = computeCacheKey(name);
    cache.put(key, new SoftReference<>(expected));

    MyBean1 actual = provider.getManagedBean(name);
    assertThat(actual, is(expected));
  }

  @Test
  @DisplayName("Getting by name an already cached bean should return it directly")
  void getAlreadyCreatedCachableBeanByName() {
    final String name = "foo";
    MyBean3 expected = new MyBean3();
    String key = computeCacheKey(name);
    cache.put(key, new SoftReference<>(expected));

    MyBean3 actual = provider.getManagedBean(name);
    assertThat(actual, is(expected));
  }

  @Test
  @DisplayName("Getting by name a managed single instance of a service should return it after " +
      "caching it")
  void getExistingSingleServiceInstanceByNameShouldCacheIt() {
    final String name = "foo";
    feeder.manageBeanWithName(MyService1.class, name);
    Mutable<MyService> bean = Mutable.empty();
    assertDoesNotThrow(() -> bean.set(provider.getManagedBean(name)));
    assertThat(bean.isPresent(), is(true));
    assertThat(bean.get(), instanceOf(MyService1.class));
    assertThat(bean.get(), isCachedUnder(computeCacheKey(name)));
  }

  @Test
  @DisplayName("Getting by name a managed cacheable instance of a service should return it after " +
      "caching it")
  void getExistingCacheableServiceInstanceByNameShouldCacheIt() {
    final String name = "foo";
    feeder.manageBeanWithName(MyService2.class, name);
    Mutable<MyService> bean = Mutable.empty();
    assertDoesNotThrow(() -> bean.set(provider.getManagedBean(name)));
    assertThat(bean.isPresent(), is(true));
    assertThat(bean.get(), instanceOf(MyService2.class));
    assertThat(bean.get(), isCachedUnder(computeCacheKey(name)));
  }

  @Test
  @DisplayName("Getting by name an already cached single instance of a service should return it" +
      " directly")
  void getAlreadyCreatedSingleServiceInstanceByName() {
    final String name = "foo";
    MyService1 expected = new MyService1();
    String key = computeCacheKey(name);
    cache.put(key, new SoftReference<>(expected));

    MyService1 actual = provider.getManagedBean(name);
    assertThat(actual, is(expected));
  }

  @Test
  @DisplayName("Getting by type a managed bean should return it without caching it")
  void getExistingManagedBeanByType() {
    feeder.manageBeanForType(MyBean2.class, MyBean2.class);
    Mutable<MyBean> bean = Mutable.empty();
    assertDoesNotThrow(() -> bean.set(provider.getManagedBean(MyBean2.class)));
    assertThat(bean.isPresent(), is(true));
    assertThat(bean.get(), instanceOf(MyBean2.class));
    assertThat(bean.get(), not(isCachedUnder(computeCacheKey(MyBean2.class.getName()))));
  }

  @Test
  @DisplayName("Getting by type non managed bean should throw a NotFoundException")
  void getNonExistingManagedBeanByType() {
    feeder.manageBeanForType(MyBean2.class, MyBean.class);
    assertThrows(NotFoundException.class, () -> provider.getManagedBean(MyBean1.class));
  }

  @Test
  @DisplayName("Getting by type a managed single instance of a singleton should return it after " +
      "caching it")
  void getExistingSingleInstanceByType() {
    feeder.manageBeanForType(MyBean1.class, MyBean.class);
    Mutable<MyBean> bean = Mutable.empty();
    assertDoesNotThrow(() -> bean.set(provider.getManagedBean(MyBean.class)));
    assertThat(bean.isPresent(), is(true));
    assertThat(bean.get(), instanceOf(MyBean1.class));
    assertThat(bean.get(), isCachedUnder(computeCacheKey(MyBean.class.getName())));
  }

  @Test
  @DisplayName("Getting by type a managed cacheable bean should return it after caching it")
  void getExistingCacheableBeanByType() {
    feeder.manageBeanForType(MyBean3.class, MyBean.class);
    Mutable<MyBean> bean = Mutable.empty();
    assertDoesNotThrow(() -> bean.set(provider.getManagedBean(MyBean.class)));
    assertThat(bean.isPresent(), is(true));
    assertThat(bean.get(), instanceOf(MyBean3.class));
    assertThat(bean.get(), isCachedUnder(computeCacheKey(MyBean.class.getName())));
  }

  @Test
  @DisplayName("Getting by type an already cached single instance of a singleton should return it" +
      " directly")
  void getAlreadyCreatedSingleInstanceByType() {
    MyBean1 expected = new MyBean1();
    String key = computeCacheKey(expected.getClass().getName());
    cache.put(key, new SoftReference<>(expected));

    MyBean1 actual = provider.getManagedBean(expected.getClass());
    assertThat(actual, is(expected));
  }

  @Test
  @DisplayName("Getting by type a managed single instance of a service should return it after " +
      "caching it")
  void getExistingSingleServiceInstanceByType() {
    feeder.manageBeanForType(MyService1.class, MyService.class);
    Mutable<MyService> bean = Mutable.empty();
    assertDoesNotThrow(() -> bean.set(provider.getManagedBean(MyService.class)));
    assertThat(bean.isPresent(), is(true));
    assertThat(bean.get(), instanceOf(MyService1.class));
    assertThat(bean.get(), isCachedUnder(computeCacheKey(MyService.class.getName())));
  }

  @Test
  @DisplayName("Getting by type an already cached single instance of a service should return it" +
      " directly")
  void getAlreadyCreatedSingleServiceInstanceByType() {
    MyService1 expected = new MyService1();
    String key = computeCacheKey(expected.getClass().getName());
    cache.put(key, new SoftReference<>(expected));

    MyService1 actual = provider.getManagedBean(expected.getClass());
    assertThat(actual, is(expected));
  }

  @Test
  @DisplayName("Getting by type an already cached cacheable instance of a service should return " +
      "it directly")
  void getAlreadyCreatedCacheableServiceInstanceByType() {
    MyService2 expected = new MyService2();
    String key = computeCacheKey(expected.getClass().getName());
    cache.put(key, new SoftReference<>(expected));

    MyService2 actual = provider.getManagedBean(expected.getClass());
    assertThat(actual, is(expected));
  }

  @Test
  @DisplayName("Getting a bean by a name having several possible candidates should throw " +
      "MultipleCandidateException")
  void getMoreThanOneBeanByName() {
    final String name = "foo";
    feeder.manageBeanWithName(MyBean2.class, name);
    feeder.manageBeanWithName(MyBean1.class, name);

    assertThrows(MultipleCandidateException.class, () -> provider.getManagedBean(name));
  }

  @Test
  @DisplayName("Getting a bean for a type having several possible candidates should throw " +
      "MultipleCandidateException")
  void getMoreThanOneBeanWhenAskedOneBeanByType() {
    feeder.manageBeanForType(MyBean2.class, MyBean.class);
    feeder.manageBeanForType(MyBean1.class, MyBean.class);

    assertThrows(MultipleCandidateException.class, () -> provider.getManagedBean(MyBean.class));
  }

  @Test
  @DisplayName("Getting all the beans satisfying a given type should return them without caching " +
      "them")
  void getAllManagedBeans() {
    feeder.manageBeanForType(MyBean3.class, MyBean.class);
    feeder.manageBeanForType(MyBean2.class, MyBean.class);
    feeder.manageBeanForType(MyBean1.class, MyBean.class);

    Set<MyBean> beans = provider.getAllManagedBeans(MyBean.class);
    assertThat(beans, notNullValue());
    assertThat(beans.size(), is(3));
    assertThat(beans.stream()
        .map(MyBean::getClass)
        .allMatch(c -> c.equals(MyBean1.class)
            || c.equals(MyBean2.class)
            || c.equals(MyBean3.class)), is(true));
    beans.forEach(b -> {
      assertThat(b, not(isCachedUnder(computeCacheKey(MyBean.class.getName()))));
      assertThat(b, not(isCachedUnder(computeCacheKey(b.getClass().getName()))));
    });
  }

  @Test
  @DisplayName("Getting all non existing beans satisfying a given type should return an empty set")
  void getNoManagedBeans() {
    Set<MyBean> beans = provider.getAllManagedBeans(MyBean.class);
    assertThat(beans, notNullValue());
    assertThat(beans.isEmpty(), is(true));
  }

  private String computeCacheKey(@SuppressWarnings("SameParameterValue") final String postfix) {
    return ManagedBeanProvider.CACHE_KEY_PREFIX + postfix;
  }

  /**
   * Matcher of the presence of a bean into the current thread cache under a given name.
   *
   * @author mmoquillon
   */
  protected static class IsCached extends BaseMatcher<Object> {

    private final SimpleCache cache = ManagedBeanProvider.getInstance().getCache();
    private final String cacheKey;

    private IsCached(String key) {
      this.cacheKey = key;
    }

    @Override
    public boolean matches(Object o) {
      //noinspection DataFlowIssue
      return cache.has(cacheKey) &&
          cache.get(cacheKey) instanceof SoftReference &&
          Objects.equals(cache.get(cacheKey, SoftReference.class).get(), o);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("cached in the thread cache because it is a singleton");
    }

    public static Matcher<Object> isCachedUnder(final String key) {
      return new IsCached(key);
    }
  }
}