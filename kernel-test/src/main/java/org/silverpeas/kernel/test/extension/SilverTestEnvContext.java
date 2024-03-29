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

import org.silverpeas.kernel.BeanContainer;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.cache.service.ApplicationCacheAccessor;
import org.silverpeas.kernel.cache.service.ThreadCacheAccessor;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Context of the Silverpeas testing environment enabled by the
 * {@link org.silverpeas.kernel.test.extension.EnableSilverTestEnv} Junit extension. Such context is
 * a way to customize the environment.
 *
 * @author mmoquillon
 */
public abstract class SilverTestEnvContext {

  /**
   * Default test context.
   */
  public static final SilverTestEnvContext DEFAULT_CONTEXT = new SilverTestEnvContext() {
    @Override
    public void init() {
      // nothing to initialize by default
    }

    @Override
    public void beforeTest(TestExecutionContext context) {
      // nothing to do
    }

    @Override
    public void afterTest(TestExecutionContext context) {
      // nothing to do
    }

    @Override
    public void clear() {
      // clear the current thread local cache (others threads are ignored at this point)
      ThreadCacheAccessor.getInstance().getCache().clear();
      ApplicationCacheAccessor.getInstance().getCache().clear();
    }

    @Override
    @NonNull
    public List<Object> getMocksToManage() {
      return List.of();
    }

    @Override
    @NonNull
    public List<Class<?>> getBeansToManage() {
      return List.of();
    }
  };

  /**
   * Initializes the test environment context. This method is usually invoked by the Silverpeas unit
   * test environment that was enabled with the
   * {@link org.silverpeas.kernel.test.extension.EnableSilverTestEnv} annotation once this one is
   * bootstrapped and initialized. The initialization is done once the
   * {@link org.silverpeas.kernel.test.annotations.SystemProperties} and
   * {@link org.silverpeas.kernel.test.annotations.SystemProperty} annotations are processed so that
   * any additional properties can be found from {@link org.silverpeas.kernel.util.SystemWrapper} by
   * the context for its initialization.
   */
  @SuppressWarnings("unused")
  public abstract void init();

  /**
   * Execute some actions to perform before the execution of a unit test.
   *
   * @param context the execution context of the current test.
   */
  public abstract void beforeTest(final TestExecutionContext context);

  /**
   * Execute some actions to perform once the execution of a unit test has been completed.
   *
   * @param context the execution context of the current test.
   */
  public abstract void afterTest(final TestExecutionContext context);

  /**
   * Gets the mocks to preload into the IoC/IoD subsystem used in the tests.
   *
   * @return a non-null list of already preconfigured mocks to manage for the unit tests.
   */
  @NonNull
  public abstract List<Object> getMocksToManage();

  /**
   * Gets the classes of the beans to preload into the IoC/IoD subsystem used in the tests. Theses
   * beans will be managed by the IoC/IoD subsystem, meaning their
   * {@link javax.annotation.PostConstruct} annotated methods will be invoked and their dependencies
   * satisfied (by mock unless there is a bean available in the
   * {@link BeanContainer} and satisfying the dependency).
   *
   * @return a non-null list of classes of beans to manage for the unit tests.
   */
  @NonNull
  public abstract List<Class<?>> getBeansToManage();


  /**
   * Clears the test environment context.
   */
  public abstract void clear();

  /**
   * Execution context of a given test.
   *
   * @author mmoquillon
   */
  public static class TestExecutionContext {
    private final Object instance;
    private final Class<?> type;
    private final Method test;

    TestExecutionContext(Class<?> type, Object instance, Method test) {
      this.instance = instance;
      this.type = type;
      this.test = test;
    }

    /**
     * Gets the instance of the test.
     *
     * @return an object of a unit test class.
     */
    @SuppressWarnings("unused")
    public Object getInstance() {
      return instance;
    }

    /**
     * Gets the type of the unit test class.
     *
     * @return the unit test class.
     */
    public Class<?> getType() {
      return type;
    }

    /**
     * Gets the unit test targeted for execution.
     *
     * @return the method representing the unit test.
     */
    public Method getTest() {
      return test;
    }
  }

}
