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

import org.junit.jupiter.api.extension.ExtendWith;
import org.silverpeas.kernel.logging.Level;

import java.lang.annotation.*;

/**
 * Annotation to specify a logger level. This annotation is managed by the {@link LoggerExtension}
 * JUnit 5 extension. Be cautious: the annotation cannot be used in
 * {@link org.junit.jupiter.api.BeforeEach} and in @{@link org.junit.jupiter.api.BeforeAll}
 * annotated methods.
 * @author mmoquillon
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@ExtendWith(LoggerExtension.class)
public @interface LoggerLevel {

  Level value();
}
