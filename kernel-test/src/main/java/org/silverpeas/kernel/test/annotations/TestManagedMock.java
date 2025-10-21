/*
 * Copyright (C) 2000 - 2022-2023 Silverpeas
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

package org.silverpeas.kernel.test.annotations;

import java.lang.annotation.*;

/**
 * This annotation is used to indicate that a field or a parameter of a test (method) in a unit test
 * class has to be mocked and the resulting mock put into the IoC container used in the unit tests
 * and this before any execution of any unit tests. If the type is annotated with the
 * {@link jakarta.inject.Named} qualifier, the mock will be also registered under this specified
 * name.
 * <p>
 * If the test class declares several annotated fields having a common type among their ancestor,
 * then the type of the fields will be registered into the IoC container for that type and their
 * values could be get by using the
 * {@link org.silverpeas.kernel.test.TestBeanContainer#getAllBeansByType(Class, Annotation...)}
 * method. In that case, the call of
 * {@link org.silverpeas.kernel.test.TestBeanContainer#getBeanByType(Class, Annotation...)} method
 * with that type as parameter will throw an exception.
 * </p>
 * <p>
 * If the annotation is applied to a parameter, then a bean of the parameter type is first looking
 * for in the IoC container used in the tests. If no such bean is found, then the type is mocked,
 * the mock is then put into the bean container and finally it is passed as parameter value. By
 * using this annotation with the parameters, you can get any previously mocked bean to, for
 * example, specify behaviours for the current unit test.
 * </p>
 *
 * @author mmoquillon
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TestManagedMock {

  /**
   * Is all of the methods of the mock should be stubbed? By default, a call to method of such an
   * object is stubbed unless a behaviour was previously set to that method. To reverse this default
   * behaviour, that is to say to allow a call to the methods invokes the <i>real</i> implementation
   * of the called method, just set this property to false.
   */
  boolean stubbed() default true;
}
