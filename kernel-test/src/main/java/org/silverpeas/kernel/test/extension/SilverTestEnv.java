/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.junit.jupiter.api.extension.*;
import org.mockito.internal.util.MockUtil;
import org.silverpeas.kernel.ManagedBeanProvider;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.TestManagedBeanFeeder;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.test.TestSystemWrapper;
import org.silverpeas.kernel.test.annotations.*;
import org.silverpeas.kernel.test.extension.SilverTestEnvContext.TestExecutionContext;
import org.silverpeas.kernel.test.util.Reflections;
import org.silverpeas.kernel.util.SystemWrapper;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * Prepares the environment specific to Silverpeas to run unit tests.
 * <p>Firstly, it bootstraps the container of beans to use for IoC/IoD and preloads it with some managed beans usually
 * required by the unit tests.</p>
 * <p>
 * Secondly it scans for fields and parameters annotated with {@link TestManagedBean} and {@link TestManagedMock} to
 * register them automatically into the bean container used in tests. If the class of a {@link TestManagedBean}
 * annotated field is qualified by a {@link Qualifier} annotated annotation, then it is registered under that qualifier
 * also. For any parameter annotated with {@link TestManagedMock}, it is first resolved by looking for an already
 * registered mock in the bean container (otherwise it is mocked and registered as for fields). Any methods annotated
 * with {@link TestManagedBean} are resolved in last.
 * </p>
 * <p>
 * Thirdly it scans for fields annotated with {@link TestedBean} to scan it for injection point in order to resolve
 * those dependencies either by settings them with a bean already registered into the bean container or by mocking it.
 * </p>
 * <p>
 * The ordering of the declaration of the different such annotated fields in the test class is very important as they
 * are treated sequentially in their declaration ordering. So, any bean that is required by others beans has to be
 * declared before those others beans.
 * </p>
 *
 * @author mmoquillon
 */
public class SilverTestEnv
    implements TestInstancePostProcessor, ParameterResolver, BeforeEachCallback, AfterEachCallback {

  private final TestManagedBeanFeeder beanFeeder = new TestManagedBeanFeeder();

  private final ManagedBeanProvider beanProvider = ManagedBeanProvider.getInstance();

  private SilverTestEnvContext context;

  /**
   * Loads into the IoC container dedicated to the unit test all the classes or beans that are annotated with one of the
   * following annotations:
   * <ul>
   * <li>{@link TestManagedBeans}: the specified classes of objects are registered into the IoC container</li>
   * <li>{@link TestManagedMock}: the annotated field of the test instance is set with a mock that has been
   * registered into the IoC container in order to resolve the dependency on it of managed beans used by the tested
   * class</li>
   * <li>{@link TestManagedBean}: the annotated field of the test instance is set with a bean that was automatically
   * put into the IoC container. If yet explicitly instantiated, the bean is put directly into the container and its
   * dependencies are resolved.</li>
   * <li>{@link TestedBean}: the annotated field represents an instance of the class covered by the current test
   * class. The field is set with a bean that was automatically put into the IoC container. If yet explicitly
   * instantiated, the bean is put directly into the container and its dependencies are resolved.</li>
   * </ul>
   * <p>
   * All the beans taken in charge by the IoC container will have their own dependencies resolved and their method
   * annotated with {@link PostConstruct} invoked. This is why the order of declarations of the annotated fields is
   * very important in the case an annotated field has a dependency on a bean referred by another field.
   * </p>
   * <strong>Be caution:</strong> any {@link TestedBean} annotated fields should be declared
   * lastly for their dependencies to have a change to be set with any previous declared {@link TestManagedMock} and
   * {@link TestManagedBean} annotated field values.
   * </p>
   *
   * @param testInstance the instance of the test class.
   * @param context the context of the extension.
   */
  @Override
  public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) {
    initSilverTestEnvContext(testInstance);
    clearEnv();

    TestSystemWrapper system = new TestSystemWrapper();
    beanFeeder.manageBean(system, SystemWrapper.class);
    SystemProperty[] systemProperties =
        testInstance.getClass().getAnnotationsByType(SystemProperty.class);
    if (systemProperties.length >= 1) {
      for (SystemProperty systemProperty : systemProperties) {
        system.setProperty(systemProperty.key(), systemProperty.value());
      }
    }

    preloadManagedBeansAndMocks();

    TestManagedMocks testManagedMocks =
        testInstance.getClass().getAnnotation(TestManagedMocks.class);
    if (testManagedMocks != null) {
      for (Class<?> type : testManagedMocks.value()) {
        Object mock = mock(type);
        registerInBeanContainer(mock);
      }
    }

    TestManagedBeans testManagedBeans =
        testInstance.getClass().getAnnotation(TestManagedBeans.class);
    if (testManagedBeans != null) {
      for (Class<?> type : testManagedBeans.value()) {
        registerInBeanContainer(type);
      }
    }

    Reflections.loopInheritance(testInstance.getClass(), type -> {
      Field[] fields = type.getDeclaredFields();
      for (Field field : fields) {
        processMockedBeanAnnotation(type, field, testInstance);
        processTestManagedBeanAnnotation(type, field, testInstance);
        processTestedBeanAnnotation(type, field, testInstance);
      }
    });
  }

  private void initSilverTestEnvContext(Object testInstance) {
    EnableSilverTestEnv testEnv = testInstance.getClass().getAnnotation(EnableSilverTestEnv.class);
    Class<? extends SilverTestEnvContext> contextClass = testEnv.context();
    if (contextClass.equals(SilverTestEnvContext.class)) {
      context = SilverTestEnvContext.DEFAULT_CONTEXT;
    } else {
      try {
        Constructor<? extends SilverTestEnvContext> constructor = contextClass.getConstructor();
        context = constructor.newInstance();
      } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
        throw new SilverpeasRuntimeException(e);
      }
    }
  }

  private void preloadManagedBeansAndMocks() {
    // preload beans
    List<Class<?>> beanTypes = context.getBeansToManage();
    beanTypes.forEach(this::registerInBeanContainer);

    // preload mocks
    List<Object> mocks = context.getMocksToManage();
    mocks.forEach(this::registerInBeanContainer);
  }

  private void clearEnv() {
    beanFeeder.removeAllManagedBeans();
    context.clear();
  }

  /**
   * Is the parameter in a test's method is supported by this extension for value injection?
   *
   * @param parameterContext the context of the parameter.
   * @param extensionContext the context of the extension.
   * @return true if the parameter is either annotated with @{@link TestManagedBean} or with {@link TestManagedMock}
   */
  @Override
  public boolean supportsParameter(final ParameterContext parameterContext,
      final ExtensionContext extensionContext) {
    return parameterContext.isAnnotated(TestManagedMock.class) ||
        parameterContext.isAnnotated(TestManagedBean.class);
  }

  /**
   * Resolves the parameter referred by the parameter context by valuing it according to its annotation: if annotated
   * with {@link TestManagedBean}, the parameter will be instantiated with its default constructor; if annotated with
   * {@link TestManagedMock}, the parameter will be mocked.
   *
   * @param parameterContext the context of the parameter.
   * @param extensionContext the context of the extension.
   * @return the value of the parameter to inject.
   */
  @Override
  public Object resolveParameter(final ParameterContext parameterContext,
      final ExtensionContext extensionContext) {
    Object bean;
    final Parameter parameter = parameterContext.getParameter();
    if (parameterContext.isAnnotated(TestManagedMock.class)) {
      bean = beanProvider.getManagedBean(parameter.getType());
      if (bean == null) {
        TestManagedMock annotation = parameterContext.findAnnotation(TestManagedMock.class)
            .orElseThrow(() -> new SilverpeasRuntimeException("No TestManagedMock annotation found!"));
        bean = annotation.stubbed() ? mock(parameter.getType()) : spy(parameter.getType());
        registerInBeanContainer(bean);
      }
    } else if (parameterContext.isAnnotated(TestManagedBean.class)) {
      bean = beanProvider.getManagedBean(parameter.getType());
      if (bean == null) {
        registerInBeanContainer(parameter.getType());
        bean = beanProvider.getManagedBean(parameter.getType());
      }
    } else {
      bean = null;
    }
    return bean;
  }

  /**
   * Invokes {@link SilverTestEnvContext#beforeTest(TestExecutionContext)}
   *
   * @param ctx the current extension context
   */
  @Override
  public void beforeEach(@NonNull final ExtensionContext ctx) {
    TestExecutionContext exeCtx = new TestExecutionContext(ctx.getRequiredTestClass(), ctx.getRequiredTestInstance(),
        ctx.getRequiredTestMethod());
    context.beforeTest(exeCtx);
  }

  /**
   * Invokes {@link SilverTestEnvContext#afterTest(TestExecutionContext)} and then clear the IoC container.
   *
   * @param ctx the current extension context
   */
  @Override
  public void afterEach(@NonNull final ExtensionContext ctx) {
    TestExecutionContext exeCtx = new TestExecutionContext(ctx.getRequiredTestClass(), ctx.getRequiredTestInstance(),
        ctx.getRequiredTestMethod());
    context.afterTest(exeCtx);
    beanFeeder.removeAllManagedBeans();
  }

  private void processTestManagedBeanAnnotation(final Class<?> type, final Field field, final Object testInstance)
      throws ReflectiveOperationException {
    if (field.isAnnotationPresent(TestManagedBean.class)) {
      setupTestInstanceField(type, field, testInstance);
    }
  }

  private void processMockedBeanAnnotation(final Class<?> type, final Field field, final Object testInstance) {
    if (field.isAnnotationPresent(TestManagedMock.class)) {
      TestManagedMock annotation = field.getAnnotation(TestManagedMock.class);
      Object bean = annotation.stubbed() ? mock(field.getType()) : spy(field.getType());
      Reflections.setField(type, testInstance, field, bean);
      registerInBeanContainer(bean);
      registerInBeanContainerByName(bean, field);
    }
  }

  private void processTestedBeanAnnotation(final Class<?> type, final Field field, final Object testInstance)
      throws ReflectiveOperationException {
    if (field.isAnnotationPresent(TestedBean.class)) {
      setupTestInstanceField(type, field, testInstance);
    }
  }

  private void setupTestInstanceField(final Class<?> type, final Field field, final Object testInstance)
      throws ReflectiveOperationException {
    field.trySetAccessible();
    Object bean = field.get(testInstance);
    if (bean == null) {
      bean = Reflections.instantiate(field.getType());
    }
    registerInBeanContainer(bean);
    registerInBeanContainerByName(bean, field);
    Reflections.setField(type, testInstance, field, bean);
  }

  private Annotation[] getQualifiers(final AnnotatedElement element) {
    return Reflections.getDeclaredAnnotations(element, Qualifier.class);
  }

  /**
   * Registers in the {@link org.silverpeas.kernel.BeanContainer} the specified class for his instances to be managed by
   * it. By registering a class instead of directly a bean, the life-cycle of the beans of the class will be taken in
   * charge by the container, meaning their instantiation will be done on demand (the singleton pattern is taken in
   * charge with the {@link javax.inject.Singleton} annotation).
   *
   * @param beanType the class of the beans to manage.
   * @param <T> the concrete type of the bean.
   */
  private <T> void registerInBeanContainer(Class<T> beanType) {
    Annotation[] qualifiers = getQualifiers(beanType);
    //noinspection unchecked
    TypeConsumer registerer = t ->
        beanFeeder.manageBeanForType(beanType, (Class<? super T>) t, qualifiers);

    applyRegisteringInBeanContainer(registerer, beanType);
  }

  /**
   * Registers in the {@link org.silverpeas.kernel.BeanContainer} the specified bean so that it can be retrieved later
   * by the unit tests. In this case, no specific life-cycle management is performed by the container. It is a shortcut
   * for the beans to be directly accessible through the IoD mechanism during the execution of a unit test.
   *
   * @param bean the bean to register.
   * @param <T> the concrete type of the bean.
   */
  @SuppressWarnings({"unchecked"})
  private <T> void registerInBeanContainer(T bean) {
    final Class<T> beanType = (Class<T>) (MockUtil.isMock(bean) || MockUtil.isSpy(bean) ?
        MockUtil.getMockHandler(bean).getMockSettings().getTypeToMock() : bean.getClass());

    Annotation[] qualifiers = getQualifiers(beanType);
    TypeConsumer registerer = t ->
        beanFeeder.manageBean(bean, (Class<? super T>) t, qualifiers);

    applyRegisteringInBeanContainer(registerer, beanType);
  }

  private <T> void registerInBeanContainerByName(T bean, AnnotatedElement element) {
    if (element.isAnnotationPresent(Named.class)) {
      Named namedQualifier = element.getAnnotation(Named.class);
      String name = namedQualifier.value();
      beanFeeder.manageBeanWithName(bean, name);
    }
  }

  private <T> void applyRegisteringInBeanContainer(TypeConsumer registerer, Class<T> clazz) {
    Function<Class<?>, Class<?>[]> typesFinder = c -> {
      Class<?>[] interfaces = c.getInterfaces();
      Class<?>[] types = Arrays.copyOf(interfaces, interfaces.length + 1);
      types[types.length - 1] = c;
      return types;
    };

    Stream.of(typesFinder.apply(clazz))
        .filter(t -> t.getTypeParameters().length == 0)
        .forEach(t -> {
          try {
            registerer.consume(t);
          } catch (ReflectiveOperationException e) {
            throw new SilverpeasRuntimeException(e);
          }
        });

    if (!clazz.isInterface()) {
      Reflections.loopInheritance(clazz.getSuperclass(), c -> Stream.of(typesFinder.apply(c))
          .filter(t -> Modifier.isAbstract(t.getModifiers()))
          .filter(t -> t.getTypeParameters().length == 0)
          .forEach(t -> {
            try {
              registerer.consume(t);
            } catch (ReflectiveOperationException e) {
              throw new SilverpeasRuntimeException(e);
            }
          }));
    }
  }

  @FunctionalInterface
  private interface TypeConsumer {
    void consume(final Class<?> type) throws ReflectiveOperationException;
  }
}
