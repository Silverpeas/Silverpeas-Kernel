package org.silverpeas.kernel.test.extension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.MockUtil;
import org.silverpeas.kernel.ManagedBeanProvider;
import org.silverpeas.kernel.test.MyBean1;
import org.silverpeas.kernel.test.MyBean2;
import org.silverpeas.kernel.test.annotations.TestManagedBean;
import org.silverpeas.kernel.test.annotations.TestManagedMock;

import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@EnableSilverTestEnv
class ManagedBeanMockFieldParameterTest {

  private static final String FOO = "My business is lazy";
  private final ManagedBeanProvider beanProvider = ManagedBeanProvider.getInstance();

  @TestManagedMock
  private MyBean1 myBean1;

  @BeforeEach
  void initMockBehaviour() {
    assertThat(myBean1, notNullValue());
    when(myBean1.getFoo()).thenReturn(FOO);
  }

  @Test
  void theMockIsWellInjected(@TestManagedBean MyBean2 myBean2) {
    assertThat(myBean2.getMyBean(), notNullValue());
    assertThat(myBean2.getMyBean(), instanceOf(MyBean1.class));
    assertThat(MockUtil.isMock(myBean2.getMyBean()), is(true));
    assertThat(myBean2.getMyBean(), is(myBean1));
    assertThat(myBean2.getMyBean().getFoo(), is(FOO));
  }

  @Test
  void theMockIsManagedInTheBeanContainer(@TestManagedBean MyBean2 myBean2) {
    MyBean1 myBean1 = beanProvider.getManagedBean(MyBean1.class);
    assertThat(MockUtil.isMock(myBean1), is(true));
    assertThat(myBean2.getMyBean(), is(myBean1));
    assertThat(myBean1.getFoo(), is(FOO));
  }

  @Test
  void theMockIsDirectlyInjectedIntoParameter(@TestManagedMock MyBean1 expected) {
    assertThat(expected, is(myBean1));
  }
}
  