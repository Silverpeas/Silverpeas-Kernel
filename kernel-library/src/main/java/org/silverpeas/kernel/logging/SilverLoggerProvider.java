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
package org.silverpeas.kernel.logging;

/**
 * A provider of a {@link SilverLogger}. It is a singleton and its single instance is lazily
 * created. It is used by the {@link SilverLogger} class to get a {@link SilverLogger} instance
 * matching a given logging namespace.
 *
 * @author mmoquillon
 * @implNote the single instance of {@link SilverLoggerProvider} is lazily spawn: it is created at
 * the first invocation of the {@link SilverLoggerProvider#getInstance()} static method.
 */
public class SilverLoggerProvider {

  /**
   * The namespace of the root logger in Silverpeas. This namespace is predefined and each logger
   * taken in charge by the Logging Engine should be a descendant of its logger.
   */
  public static final String ROOT_NAMESPACE = "silverpeas";

  private static SilverLoggerProvider instance;

  private final LoggerConfigurationManager configurationManager;
  private final SilverLoggerFactory loggerFactory = SilverLoggerFactory.getInstance();

  /**
   * Gets the single instance of this provider of {@link SilverLogger} objects.
   *
   * @return a {@link SilverLoggerProvider} instance.
   */
  public static synchronized SilverLoggerProvider getInstance() {
    if (instance == null) {
      instance = new SilverLoggerProvider();
    }
    return instance;
  }

  private SilverLoggerProvider() {
    configurationManager = new LoggerConfigurationManager();
    configurationManager.loadAllConfigurationFiles();
  }

  /**
   * Gets the logger that is defined for the specified logging namespace.
   * <p>
   * A logging namespace is the name or a category under which the messages will be log. The
   * messages will be logged only if the logging level satisfies the logger's level. If no level is
   * set for the logger, then the level from its nearest ancestor with a specific (non-null) level
   * value is taken into account.
   * </p>
   * The logger instance is obtained from the logging namespace by using the
   * {@link org.silverpeas.kernel.logging.SilverLoggerFactory} factory. The factory has also the
   * responsibility of the initialization mechanism of the logger from its logging configuration
   * parameters before returning it.
   *
   * @param module a logging namespace.
   * @return a logger instance identified by the given logging namespace, manufactured by a
   * {@link SilverLoggerFactory} instance.
   */
  public SilverLogger getLogger(final String module) {
    LoggerConfigurationManager.LoggerConfiguration configuration =
        this.configurationManager.getLoggerConfiguration(module);
    return loggerFactory.getLogger(configuration.getNamespace(), configuration);
  }

  /**
   * Gets a logger for the specified object. The logger is found from the package name of the
   * specified object.
   * <p>
   * In Silverpeas, each logger namespace matches a package name, starting from the
   * <code>silverpeas</code> subpackage: for a package <code>org.silverpeas.core.io</code>, there
   * is a logger with the namespace <code>silverpeas.core.io</code>.
   * </p>
   *
   * @param object the object from which the logger namespace will be determined.
   * @return a logger instance identified by the logging namespace that was determined from the
   * given object, manufactured by a {@link SilverLoggerFactory} instance.
   * @see SilverLoggerProvider#getLogger(String)
   */
  public SilverLogger getLogger(Object object) {
    Class<?> type = object instanceof Class ? (Class<?>) object : object.getClass();
    Package pkg = type.getPackage();
    String fqn;
    Logger logger = pkg.isAnnotationPresent(Logger.class) ? pkg.getAnnotation(Logger.class) :
        type.getAnnotation(Logger.class);
    if (logger != null) {
      fqn = logger.value();

    } else {
      fqn = pkg.getName();
    }
    String namespace = fqn.startsWith("org.silverpeas") ? fqn.substring(fqn.indexOf('.') + 1) : fqn;
    return getLogger(namespace);
  }

  /**
   * Gets the {@link LoggerConfigurationManager} instance used by this provider to manage the
   * configuration of the Silverpeas loggers. By using the {@link LoggerConfigurationManager}
   * object, you can modify programmatically the configuration of a logger.
   *
   * @return the {@link LoggerConfigurationManager} instance backed by this Silverpeas loggers
   * provider.
   */
  public LoggerConfigurationManager getConfigurationManager() {
    return configurationManager;
  }
}
  