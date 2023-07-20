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
 * This annotation is used to indicate that the type of a field or a parameter of a test (method) in the unit test class
 * has to be managed by the IoC container used by the unit tests. The field or the parameter is then valued with an
 * expected managed bean before executing any unit test. For doing, the bean type must have a default constructor. Once
 * instantiated, the dependencies on others managed beans will be resolved and if the bean has a
 * {@link javax.annotation.PostConstruct} annotated method, then this method will be invoked. This annotation is for
 * classes for which a true instance is preferred to a mock in a unit test.
 * <p>
 * The field annotated with this annotation can be explicitly instantiated, in that case the instance will be put
 * directly into the IoC container. The dependencies of the instance on others managed bean will be then resolved and
 * the {@link javax.annotation.PostConstruct} annotated method, if any, will be invoked. If the test class declares
 * several annotated fields having a common type among their ancestor, then the type of the fields will be registered
 * for that type and their value could be get by using the
 * {@link org.silverpeas.kernel.test.TestBeanContainer#getAllBeansByType(Class, Annotation...)} method. In this case,
 * the call of {@link org.silverpeas.kernel.test.TestBeanContainer#getBeanByType(Class, Annotation...)} method with that
 * type as parameter will throw an exception.
 * </p>
 * <p>
 * If the annotation is applied to a parameter, then a bean of the parameter type is first looking for in the bean
 * container used in the tests. If no such bean is found, then the type registered as to be managed by the IoC container
 * and finally the parameter is valued with resulting managed bean. By using this annotation with the parameters, you
 * can get any previously registered bean.
 * </p>
 *
 * @author mmoquillon
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TestManagedBean {

}
