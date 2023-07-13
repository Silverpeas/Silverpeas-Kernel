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
package org.silverpeas.kernel.logging.sys;

import org.junit.jupiter.api.Test;
import org.silverpeas.kernel.logging.*;
import org.silverpeas.kernel.logging.test.MyTestBean3;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit test on the SysLogger implementation of Logger.
 * @author mmoquillon
 */
class SysLoggerTest {

  private static final String LOGGER_NAMESPACE = "silverpeas.test";

  @Test
  void getALogger() {
    final String namespace = LOGGER_NAMESPACE;
    SilverLogger logger = new SysLogger(namespace);
    assertThat(logger.getNamespace(), is(namespace));
    assertThat(logger.getLevel(), notNullValue());
    assertThat(logger.getLevel(), is(Level.INFO));
  }

  @Test
  void getADeepLogger() {
    final String namespace = LOGGER_NAMESPACE + ".toto.titi.tutu.boo";
    SilverLogger logger = new SysLogger(namespace);
    assertThat(logger.getNamespace(), is(namespace));
    assertThat(logger.getLevel(), notNullValue());
    assertThat(logger.getLevel(), is(Level.INFO));
  }

  @Test
  void changeLoggerLevel() {
    final String namespace = LOGGER_NAMESPACE;
    SilverLogger logger = new SysLogger(namespace);
    assertThat(logger.getNamespace(), is(namespace));
    assertThat(logger.getLevel(), not(Level.DEBUG));
    logger.setLevel(Level.DEBUG);
    assertThat(logger.getLevel(), is(Level.DEBUG));
  }

  @Test
  void loggerNamespaceMatchesBeanPackage() {
    String expectedNamespace = "silverpeas.kernel.logging";

    SilverLogger logger = SilverLogger.getLogger(MyTestBean1.class);
    assertThat(logger.getNamespace(), is(expectedNamespace));
  }

  @Test
  void loggerNamespaceMatchesAnnotatedBean() {
    Logger annotation = MyTestBean2.class.getAnnotation(Logger.class);
    assertThat(annotation, notNullValue());
    String expectedNamespace = annotation.value();

    SilverLogger logger = SilverLogger.getLogger(MyTestBean2.class);
    assertThat(logger.getNamespace(), is(expectedNamespace));
  }

  @Test
  void loggerNamespaceMatchesAnnotatedPackage() {
    Logger annotation = MyTestBean3.class.getPackage().getAnnotation(Logger.class);
    assertThat(annotation, notNullValue());
    String expectedNamespace = annotation.value();

    SilverLogger logger = SilverLogger.getLogger(MyTestBean3.class);
    assertThat(logger.getNamespace(), is(expectedNamespace));
  }
}
