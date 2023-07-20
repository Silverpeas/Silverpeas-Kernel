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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.MockUtil;
import org.silverpeas.kernel.TestManagedBeanFeeder;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class DependencyResolverTest {

  private final DependencyResolver resolver = DependencyResolver.get();
  private final TestManagedBeanFeeder feeder = new TestManagedBeanFeeder();

  @BeforeEach
  void resolverExists() {
    assertThat(resolver, notNullValue());
  }

  @AfterEach
  void cleanUpAllManagedBeans() {
    feeder.removeAllManagedBeans();
  }

  @Test
  @DisplayName("The field is mocked if no managed bean is found for the injection point")
  void specificInjectionIsMockedWhenNoManagedBeanIsAvailable() {
    MyBean3 myBean3 = new MyBean3();
    resolver.resolve(myBean3);

    MyBean1 myBean1 = myBean3.getMyBean1();
    assertThat(MockUtil.isMock(myBean1), is(true));
  }

  @Test
  @DisplayName("The field is mocked if no managed bean is found for the injection point")
  void genericInjectionIsMockedWhenNoManagedBeanIsAvailable() {
    MyBean2 myBean2 = new MyBean2();
    resolver.resolve(myBean2);

    MyBean myBean = myBean2.getMyBean();
    assertThat(MockUtil.isMock(myBean), is(true));
  }

  @Test
  @DisplayName("The field is valued with the managed bean matching the injection point")
  void specificInjectionIsResolvedWithTheMatchingManagedBean() {
    String foo = "Hello World!";
    MyBean1 expected = new MyBean1();
    expected.setFoo(foo);
    feeder.manageBean(expected, MyBean1.class);

    MyBean3 myBean3 = new MyBean3();
    resolver.resolve(myBean3);

    MyBean1 myBean1 = myBean3.getMyBean1();
    assertThat(myBean1, notNullValue());
    assertThat(myBean1.getFoo(), is(foo));
  }

  @Test
  @DisplayName("The generic field is valued with the managed bean matching the injection point")
  void genericInjectionIsResolvedWithTheMatchingManagedBean() {
    String foo = "Hello World!";
    MyBean1 expected = new MyBean1();
    expected.setFoo(foo);
    feeder.manageBean(expected, MyBean.class);

    MyBean2 myBean2 = new MyBean2();
    resolver.resolve(myBean2);

    MyBean myBean = myBean2.getMyBean();
    assertThat(myBean, notNullValue());
    assertThat(myBean, instanceOf(MyBean1.class));
    assertThat(myBean.getFoo(), is(foo));
  }

}