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

package org.silverpeas.kernel.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.kernel.exception.MultipleCandidateException;
import org.silverpeas.kernel.exception.NotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
@DisplayName("Test the management of the beans by the IoC/IoD container dedicated to unit tests")
class TestBeanContainerTest {

  private final TestBeanContainer container = new TestBeanContainer();

  @AfterEach
  void cleanBeanContainer() {
    container.clear();
  }

  @Test
  @DisplayName("Beans have their post-constructor invoked when asked by name")
  void ensurePostConstructIsInvokedWhenBeanAskedByName() {
    final String name = "foo";
    MyBean1 myBean = new MyBean1();
    assertThat(myBean.getFoo(), nullValue());
    container.putBean(myBean, name);

    Optional<MyBean1> expected = container.getBeanByName(name);
    assertThat(expected.isPresent(), is(true));
    assertThat(expected.get().getFoo(), is(MyBean1.DEFAULT_FOO));
  }

  @Test
  @DisplayName("Beans have their post-constructor invoked when asked by type")
  void ensurePostConstructIsInvokedWhenBeanAskedByType() {
    MyBean1 myBean = new MyBean1();
    assertThat(myBean.getFoo(), nullValue());
    container.putBean(myBean, MyBean1.class);

    Optional<MyBean1> expected = container.getBeanByType(MyBean1.class);
    assertThat(expected.isPresent(), is(true));
    assertThat(expected.get().getFoo(), is(MyBean1.DEFAULT_FOO));
  }

  @Test
  @DisplayName("Beans are instantiated when asked by name and their post-constructor invoked")
  void ensureBeanAskedByNameIsWellInstantiatedAndItsPostConstructInvoked() {
    final String name = "foo";
    container.putBean(MyBean1.class, name);

    Optional<MyBean1> expected = container.getBeanByName(name);
    assertThat(expected.isPresent(), is(true));
    assertThat(expected.get().getFoo(), is(MyBean1.DEFAULT_FOO));
  }

  @Test
  @DisplayName("Beans are instantiated when asked by type and their post-constructor invoked")
  void ensureBeanAskedByTypeIsWellInstantiatedAndItsPostConstructInvoked() {
    container.putBean(MyBean1.class, MyBean1.class);

    Optional<MyBean1> expected = container.getBeanByType(MyBean1.class);
    assertThat(expected.isPresent(), is(true));
    assertThat(expected.get().getFoo(), is(MyBean1.DEFAULT_FOO));
  }

  @Test
  @DisplayName("Same instance at each ask for a bean by name")
  void ensureBeanByNameIsSingleInstance() {
    final String name = "foo";
    container.putBean(MyBean1.class, name);

    Optional<MyBean1> expected1 = container.getBeanByName(name);
    Optional<MyBean1> expected2 = container.getBeanByName(name);
    assertThat(expected1.isPresent(), is(true));
    assertThat(expected2.isPresent(), is(true));
    assertThat(expected1.get(), is(expected2.get()));
  }

  @Test
  @DisplayName("Same instance at each ask for a bean by type")
  void ensureBeanByTypeIsSingleInstance() {
    container.putBean(MyBean1.class, MyBean1.class);

    Optional<MyBean1> expected1 = container.getBeanByType(MyBean1.class);
    Optional<MyBean1> expected2 = container.getBeanByType(MyBean1.class);
    assertThat(expected1.isPresent(), is(true));
    assertThat(expected2.isPresent(), is(true));
    assertThat(expected1.get(), is(expected2.get()));
  }

  @Test
  @DisplayName("Same instance at each ask for a bean by generic type")
  void ensureBeanByGenericTypeIsSingleInstance() {
    container.putBean(MyBean1.class, MyBean.class);

    Optional<MyBean> expected1 = container.getBeanByType(MyBean.class);
    Optional<MyBean> expected2 = container.getBeanByType(MyBean.class);
    assertThat(expected1.isPresent(), is(true));
    assertThat(expected2.isPresent(), is(true));
    assertThat(expected1.get(), is(expected2.get()));
  }

  @Test
  void ensureBeanForGenericTypeIsGot() {
    container.putBean(MyBean1.class, MyBean.class);

    Optional<MyBean> expected = container.getBeanByType(MyBean.class);
    assertThat(expected.isPresent(), is(true));
    assertThat(expected.get(), instanceOf(MyBean1.class));
  }

  @Test
  void ensureAllBeansSatisfyingGenericTypeIsGot() {
    container.putBean(MyBean1.class, MyBean.class);
    container.putBean(MyBean4.class, MyBean.class);

    Set<MyBean> beans = container.getAllBeansByType(MyBean.class);
    assertThat(beans, notNullValue());
    assertThat(beans.size(), is(2));
    Set<Class<? extends MyBean>> classes = beans.stream().map(MyBean::getClass).collect(Collectors.toSet());
    assertThat(classes.size(), is(2));
    //noinspection unchecked
    assertThat(classes, hasItems(MyBean1.class, MyBean4.class));
  }

  @Test
  void ensureFailureWithSeveralCandidatesForAName() {
    final String name = "foo";
    container.putBean(MyBean1.class, name);
    container.putBean(MyBean2.class, name);

    assertThrows(MultipleCandidateException.class, () -> container.getBeanByName(name));
  }

  @Test
  void ensureNothingIsGotWithNoCandidatesForAName() {
    final String name = "foo";
    assertThat(container.getBeanByName(name).isEmpty(), is(true));
  }

  @Test
  void ensureFailureWithSeveralCandidatesForAType() {
    container.putBean(MyBean1.class, MyBean.class);
    container.putBean(MyBean4.class, MyBean.class);

    assertThrows(MultipleCandidateException.class, () -> container.getBeanByType(MyBean.class));
  }

  @Test
  void ensureNothingIsGotWithNoCandidatesForAType() {
    assertThat(container.getBeanByType(MyBean.class).isEmpty(), is(true));
  }
}