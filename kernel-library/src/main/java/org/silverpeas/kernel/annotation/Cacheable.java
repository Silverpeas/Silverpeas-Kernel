/*
 * Copyright (C) 2000 - 2025 Silverpeas
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

package org.silverpeas.kernel.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation is to tag classes managed by the underlying IoC/IoD container as to be eligible
 * to the cache.
 * <p>
 * In Silverpeas, managed beans and their dependencies to others managed beans are usually resolved
 * by the used IoC/IoD implementation. Nevertheless, Silverpeas provides also a way for non-managed
 * objects to access the managed beans. This is done with the
 * {@link org.silverpeas.kernel.ManagedBeanProvider} singleton. Because, depending on the IoC/IoD
 * implementation, such access can be costly, {@link org.silverpeas.kernel.ManagedBeanProvider}
 * maintains a cache for singletons (id est for managed beans annotated with the
 * {@link jakarta.inject.Singleton} lifecycle pseudo-scope). But it is also possible to inform the
 * managed bean provider to cache also some peculiar managed beans and this by annotating their
 * class with this {@link Cacheable} annotation.
 * </p>
 * <p>
 * This annotation should be used carefully because only a single instance will be cached and thus
 * served by {@link org.silverpeas.kernel.ManagedBeanProvider}. So the beans have to be stateless
 * and their lifecycle should be long enough to not hurt the lifecycle management of managed beans
 * by the IoC/IoD container.
 * </p>
 *
 * @author mmoquillon
 */
@Documented
@Retention(RUNTIME)
@Managed
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface Cacheable {
}
