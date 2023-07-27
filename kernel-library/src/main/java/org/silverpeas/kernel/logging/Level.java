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
package org.silverpeas.kernel.logging;

/**
 * The different logging levels taken in charge by the Silverpeas logger. Such levels define in what
 * a message is important: the higher the level is, the more important the message is. The loggers
 * have also a logging level from which they accept to write out the messages: all messages with a
 * level greater or equal than its own level will be taken in charge; the loggers level define the
 * lower level of messages they accept to log.
 * <p>
 * Because each logging solution in the Java ecosystem defines their own logging level, it is
 * required to abstract them in order to be agnostic to any logging mechanisms.
 * </p>
 * <p>
 * This Level enumeration defines a set of standard logging levels that can be used to control
 * logging output. The logging Level objects are ordered and are specified by ordered integers.
 * Enabling logging at a given level also enables logging at all higher levels.
 *
 * @author mmoquillon
 */
public enum Level {
  /**
   * The lower logging level: this level includes all the below levels; whatever their level, the
   * messages will be written into the logs.
   */
  DEBUG(500),

  /**
   * The INFO level: only messages with the tracing level of INFO, WARNING and ERROR will be written
   * into the logs.
   */
  INFO(700),
  /**
   * The WARNING level: only messages with the tracing level of WARNING and ERROR will be written
   * into the logs.
   */
  WARNING(800),
  /**
   * The ERROR level: only messages with the tracing level of ERROR will be written into the logs.
   */
  ERROR(900);

  private final int value;

  Level(int order) {
    this.value = order;
  }

  /**
   * Gets the level value of this logging level. The higher the value is, the more important the
   * level is.
   *
   * @return an integer indicating the importance of the level.
   */
  public int value() {
    return this.value;
  }

  /**
   * Is this level is greater or equal to the specified logging level?
   *
   * @param level another logging level
   * @return true if this level is more important or equally important than the given other level.
   */
  public boolean isGreaterOrEqual(Level level) {
    return value >= level.value;
  }
}
