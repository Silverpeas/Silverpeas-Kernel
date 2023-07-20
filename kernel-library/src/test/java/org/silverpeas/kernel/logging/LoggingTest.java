/*
 * Copyright (C) 2000 - 2023 Silverpeas
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

package org.silverpeas.kernel.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.kernel.logging.sys.BufferHandler;
import org.silverpeas.kernel.logging.test.MyTestBean3;

import java.util.logging.LogManager;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.silverpeas.kernel.logging.Level.*;

/**
 * Unit tests on logging of message with and without the use of the {@link Logger} annotation.
 *
 * @author mmoquillon
 */
class LoggingTest {

  private final BufferHandler bufferHandler = new BufferHandler();

  @BeforeEach
  void redirectLoggingOutput() {
    Stream.of("silverpeas", "silverpeas.kernel", "silverpeas.kernel.logging", "silverpeas.kernel.logging.test")
        .forEach(n -> {
          java.util.logging.Logger logger = java.util.logging.Logger.getLogger(n);
          logger.addHandler(bufferHandler);
        });
  }

  @Test
  void logMessageWithLoggableLevel() {
    MyTestBean1 bean = new MyTestBean1();
    assertNamespaceMatches("silverpeas.kernel.logging", bean);
    assertLevelIsLoggable(INFO, bean);

    String message = "Hello World 1!!";
    bean.writeMessageIntoLog(INFO, message);

    assertMessageIsLogged(message);
  }

  @Test
  void logMessageWithNonLoggableLevel() {
    MyTestBean1 bean = new MyTestBean1();
    assertNamespaceMatches("silverpeas.kernel.logging", bean);
    assertLevelIsNotLoggable(DEBUG, bean);

    String message = "Hello World 2!!";
    bean.writeMessageIntoLog(DEBUG, message);

    assertMessageIsNotLogged(message);
  }

  @Test
  void annotatedBeanLogsMessageWithLoggableLevel() {
    MyTestBean2 bean = new MyTestBean2();
    assertNamespaceMatches("silverpeas.kernel.logging.test", bean);
    assertLevelIsLoggable(WARNING, bean);

    String message = "Hello World 3!!";
    bean.writeMessageIntoLog(WARNING, message);

    assertMessageIsLogged(message);
  }

  @Test
  void annotatedBeanLogsMessageWithNonLoggableLevel() {
    MyTestBean2 bean = new MyTestBean2();
    assertNamespaceMatches("silverpeas.kernel.logging.test", bean);
    assertLevelIsNotLoggable(DEBUG, bean);

    String message = "Hello World 4!!";
    bean.writeMessageIntoLog(DEBUG, message);

    assertMessageIsNotLogged(message);
  }

  @Test
  void beanInAnnotatedPackageLogsMessageWithLoggableLevel() {
    MyTestBean3 bean = new MyTestBean3();
    assertNamespaceMatches("silverpeas.kernel", bean);
    assertLevelIsLoggable(WARNING, bean);

    String message = "Hello World 5!!";
    bean.writeMessageIntoLog(WARNING, message);

    assertMessageIsLogged(message);
  }

  @Test
  void beanInAnnotatedPackageLogsMessageWithNonLoggableLevel() {
    MyTestBean3 bean = new MyTestBean3();
    assertNamespaceMatches("silverpeas.kernel", bean);
    assertLevelIsNotLoggable(INFO, bean);

    String message = "Hello World 6!!";
    bean.writeMessageIntoLog(INFO, message);

    assertMessageIsNotLogged(message);
  }

  private void assertMessageIsLogged(String message) {
    boolean hasMessage = bufferHandler.getBufferLines().contains(message);
    assertThat(hasMessage, is(true));
  }

  private void assertMessageIsNotLogged(String message) {
    boolean hasMessage = bufferHandler.getBufferLines().contains(message);
    assertThat(hasMessage, is(false));
  }

  private void assertLevelIsLoggable(Level level, Object object) {
    assertThat(SilverLogger.getLogger(object).isLoggable(level), is(true));
  }

  private void assertLevelIsNotLoggable(Level level, Object object) {
    assertThat(SilverLogger.getLogger(object).isLoggable(level), is(false));
  }

  private void assertNamespaceMatches(String namespace, Object object) {
    assertThat(SilverLogger.getLogger(object).getNamespace(), is(namespace));
  }
}
