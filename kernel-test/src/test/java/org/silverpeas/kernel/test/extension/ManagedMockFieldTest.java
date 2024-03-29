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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.MockUtil;
import org.silverpeas.kernel.ManagedBeanProvider;
import org.silverpeas.kernel.test.MyBean1;
import org.silverpeas.kernel.test.MyBean2;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@EnableSilverTestEnv
class ManagedMockFieldTest {

  private static final String FOO = "My business is lazy";
  private final ManagedBeanProvider beanProvider = ManagedBeanProvider.getInstance();

  @TestManagedMock
  private MyBean1 myBean1;

  @TestedBean
  private MyBean2 testedBean;

  @BeforeEach
  void initMockBehaviour() {
    assertThat(myBean1, notNullValue());
    when(myBean1.getFoo()).thenReturn(FOO);
  }

  @BeforeEach
  void checkTestedBeanInjected() {
    assertThat(testedBean, notNullValue());
  }

  @Test
  void theMockIsWellInjected() {
    assertThat(testedBean.getMyBean(), notNullValue());
    assertThat(testedBean.getMyBean(), instanceOf(MyBean1.class));
    assertThat(MockUtil.isMock(testedBean.getMyBean()), is(true));
    assertThat(testedBean.getMyBean(), is(myBean1));
    assertThat(testedBean.getMyBean().getFoo(), is(FOO));
  }

  @Test
  void theMockIsManagedInTheBeanContainer() {
    MyBean1 myBean1 = beanProvider.getManagedBean(MyBean1.class);
    assertThat(MockUtil.isMock(myBean1), is(true));
    assertThat(testedBean.getMyBean(), is(myBean1));
    assertThat(myBean1.getFoo(), is(FOO));
  }
}
