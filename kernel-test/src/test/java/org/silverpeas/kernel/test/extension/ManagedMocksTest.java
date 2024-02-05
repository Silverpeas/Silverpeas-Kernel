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

package org.silverpeas.kernel.test.extension;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.MockUtil;
import org.silverpeas.kernel.ManagedBeanProvider;
import org.silverpeas.kernel.test.MyBean1;
import org.silverpeas.kernel.test.MyBean2;
import org.silverpeas.kernel.test.MyBean3;
import org.silverpeas.kernel.test.annotations.TestManagedMocks;
import org.silverpeas.kernel.test.annotations.TestedBean;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@EnableSilverTestEnv
@TestManagedMocks({MyBean1.class, MyBean3.class})
class ManagedMocksTest {

  private final ManagedBeanProvider beanProvider = ManagedBeanProvider.getInstance();

  @TestedBean
  MyBean2 testedBean;

  @Test
  @Order(1)
  void testedBeanIsInjectedIntoTest() {
    assertThat(testedBean, notNullValue());
  }

  @Test
  @Order(2)
  void testedBeanIsManagedInTheBeanContainer() {
    Assertions.assertDoesNotThrow(() -> beanProvider.getAllManagedBeans(MyBean2.class));
  }

  @Test
  @Order(4)
  void theMocksAreWellInjected() {
    assertThat(testedBean.getMyBean(), notNullValue());
    assertThat(testedBean.getMyBean(), instanceOf(MyBean1.class));
    assertThat(MockUtil.isMock(testedBean.getMyBean()), is(true));
  }

  @Test
  @Order(3)
  void theMocksAreManagedInTheBeanContainer() {
    MyBean1 myBean1 = beanProvider.getManagedBean(MyBean1.class);
    MyBean3 myBean3 = beanProvider.getManagedBean(MyBean3.class);
    assertThat(MockUtil.isMock(myBean1), is(true));
    assertThat(MockUtil.isMock(myBean3), is(true));
  }
}
