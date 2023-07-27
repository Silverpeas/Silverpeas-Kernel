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

import org.junit.jupiter.api.extension.ExtendWith;
import org.silverpeas.kernel.test.UnitTest;
import org.silverpeas.kernel.test.TestBeanContainer;

import java.lang.annotation.*;

/**
 * Enables the Silverpeas environment dedicated to the unit tests. It extends the unit test with the
 * {@link SilverTestEnv} JUnit extension. This annotation is used to ask for an IoC/IoD environment
 * to be bootstrapped for the beans managed by the IoC container as well as the resolution of their
 * dependencies to be taken in charge during the execution of a unit test. This environment supports
 * the JSR-330 annotations in business or technical codes. The IoC/IoD subsystem is implemented by
 * the {@link TestBeanContainer} container that provides a simple solution dedicated only to the
 * unit tests.
 *
 * @author mmoquillon
 * @see SilverTestEnv
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
@UnitTest
@ExtendWith(SilverTestEnv.class)
public @interface EnableSilverTestEnv {

  /**
   * The test environment context to use when bootstrapping the Silverpeas unit test environment
   * with an IoC/IoD subsystem. This environment context provides a way to customize the behaviour
   * of the IoC/IoD system.
   *
   * @return the test context to use for the unit test class.
   */
  Class<? extends SilverTestEnvContext> context() default SilverTestEnvContext.class;
}
