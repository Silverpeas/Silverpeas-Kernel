package org.silverpeas.kernel;

@Service
public class MyService1 implements MyService {

  @Override
  public MyBean doFunctionalSomething() {
    return ManagedBeanProvider.getInstance().getManagedBean(MyBean1.class);
  }
}
  