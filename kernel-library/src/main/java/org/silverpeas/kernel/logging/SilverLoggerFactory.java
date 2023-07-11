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

import org.silverpeas.kernel.logging.LoggerConfigurationManager.LoggerConfiguration;
import org.silverpeas.kernel.logging.sys.SysLoggerFactory;

import java.util.ServiceLoader;

/**
 * A factory of logger instances. It wraps in fact the implementation of the Silverpeas Logging Engine that uses a true
 * logging backend.
 * <p>
 * The factory isn't dedicated to be used by client code but by the {@link org.silverpeas.kernel.logging.SilverLogger}
 * class to obtain an instance of a logger according to the actual active logging backend. By implementing this
 * interface, the implementor has the total control of any cache mechanism as well as of the loggers manufacture
 * process.
 * <p>
 * The bind between the {@link org.silverpeas.kernel.logging.SilverLoggerFactory} interface and its implementation is
 * performed by the Java SPI (Java Service Provider Interface). Only the first available logger factory implementation
 * is loaded. If no implementation candidate is found through the Java SPI, then a default implementation, that uses the
 * standard Java Logging mechanism, is loaded.
 *
 * @author mmoquillon
 */
public interface SilverLoggerFactory {

  /**
   * Gets an instance of the logger factory. The implementation is provided by the Java Service Provider Interface and
   * this implementation wraps the concrete logging mechanism used by the Silverpeas Logging Engine. If no candidate is
   * found through the Java SPI, then a default implementation is loaded; this default implementation wraps the Java
   * Logging system.
   *
   * @return an instance of a logger factory.
   */
  static SilverLoggerFactory get() {
    return ServiceLoader.load(SilverLoggerFactory.class)
        .findFirst()
        .orElseGet(SysLoggerFactory::new);
  }

  /**
   * Gets a {@link org.silverpeas.kernel.logging.SilverLogger} instance for the specified namespace. If a logger has
   * already been created with the given namespace it is returned, otherwise a new logger is manufactured and
   * initialized.
   * <p>
   * The logging level of the returned logger will be set according to the logging configuration found for the given
   * logger namespace. If no level setting is found from the configuration or if there is no configuration found for the
   * specified namespace, then the logger level is set to null meaning it should inherit its level from its nearest
   * ancestor with a specific (non-null) level value. It is the responsibility of the implementation of the logger to
   * take care of the logging level inheritance and of the default log handlers/adapters used by Silverpeas.
   * <p>
   * This method should not be invoked directly. It is dedicated to be used by the
   * {@link org.silverpeas.kernel.logging.SilverLogger#getLogger(String)} method or by the implementation of the
   * Silverpeas Logging Engine.
   *
   * @param namespace the hierarchical dot-separated namespace of the logger mapping the hierarchical relationships
   * between the loggers from the root one.
   * @return a Silverpeas logger instance.
   */
  default SilverLogger getLogger(String namespace) {
    LoggerConfiguration configuration =
        SilverLoggerProvider.getInstance().getConfigurationManager().getLoggerConfiguration(namespace);
    return getLogger(namespace, configuration);
  }

  /**
   * Gets a {@link org.silverpeas.kernel.logging.SilverLogger} instance for the specified namespace. If a logger has
   * already been created with the given namespace it is returned, otherwise a new logger is manufactured and
   * initialized from the given logger configuration.
   * <p>
   * The logging level of the returned logger will be set according to the specified logging configuration. If no level
   * setting is found from the configuration or if there is no configuration found for the specified namespace, then the
   * logger level is set to null meaning it should inherit its level from its nearest ancestor with a specific
   * (non-null) level value. It is the responsibility of the implementation of the logger to take care of the logging
   * level inheritance and of the default log handlers/adapters used by Silverpeas.
   * <p>
   * This method should not be invoked directly. It is dedicated to be used by the
   * {@link org.silverpeas.kernel.logging.SilverLogger#getLogger(String)} method or by the implementation of the
   * Silverpeas Logging Engine.
   *
   * @param namespace the hierarchical dot-separated namespace of the logger mapping the hierarchical relationships
   * between the loggers from the root one.
   * @param configuration the logger configuration to use when initializing the manufactured logger. If the logger
   * already exists, the configuration won't be used in order to avoid any replacement of the existing configuration. To
   * update its configuration, please use instead {@link org.silverpeas.kernel.logging.LoggerConfigurationManager}.
   * @return a Silverpeas logger instance.
   */
  SilverLogger getLogger(String namespace, LoggerConfiguration configuration);

}
