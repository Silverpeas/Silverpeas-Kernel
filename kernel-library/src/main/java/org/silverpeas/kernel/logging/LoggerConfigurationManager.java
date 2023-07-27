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

import org.silverpeas.kernel.SilverpeasResourcesProvider;
import org.silverpeas.kernel.SilverpeasRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * There is a single global LoggerConfigurationManager object that is used to manage a set of
 * configuration about the different Loggers available in Silverpeas. This single instance can only
 * be got from the {@link SilverLoggerProvider} object.
 * <p>
 * Each logger in Silverpeas is usually mapped to a given Silverpeas module. A Silverpeas module is
 * a functional component in Silverpeas. The component can provide a service to others components or
 * implement a business service for the end user. By defining a mapping between a module and a
 * logger, when some objects write some messages into a log, only the name of the module in which
 * these objects belong is required to figure out what logger to use; it is hence no necessary to
 * remind the schema of the loggers namespace in use in Silverpeas. Each mapping is defined in a
 * logging configuration file (a Java properties file) with optionally, as property, the logging
 * level to use for the mapped logger.
 * <p>
 * A logging configuration for a Silverpeas module is stored into a properties file that must be
 * located in the loggers root directory indicated by
 * {@link SilverpeasResourcesProvider#getLoggersRootPath()}. The following properties are expected
 * into such a file:
 * </p>
 * <ul>
 *   <li>namespace: the hierarchical path of the logger in which each node is separated by a
 *   dot</li>
 *   <li>level: optional property indicating the level of traces of the logger. If not set, the
 *   level is figuring out by
 *   coming upward to the nearest parent logger for which a level is defined.</li>
 * </ul>
 *
 * @author mmoquillon
 * @apiNote It isn't possible to instantiate explicitly this class. To get a
 * {@link LoggerConfigurationManager} instance, you should ask to the {@link SilverLoggerProvider}
 * single instance.
 * @implNote The loading of the loggers configuration isn't done at instantiation; it has to be
 * invoked explicitly. The instantiation of a {@link LoggerConfigurationManager} is performed by the
 * {@link SilverLoggerProvider} single instance.
 */
public class LoggerConfigurationManager {

  private static final String LOGGER_CONF_FILE_SUFFIX = "Logging.properties";
  private static final String THIS_LOGGER_NAMESPACE = "silverpeas.core.logging";
  private static final String LOGGER_NAMESPACE = "namespace";
  private static final String LOGGER_LEVEL = "level";
  private static final int INITIAL_CAPACITY = 128;

  private static final Map<String, LoggerConfiguration> configs =
      new ConcurrentHashMap<>(INITIAL_CAPACITY);

  protected LoggerConfigurationManager() {
  }

  private Map<String, LoggerConfiguration> getLoggerConfigurations() {
    return configs;
  }

  protected void loadAllConfigurationFiles() {
    java.util.logging.Logger.getLogger(THIS_LOGGER_NAMESPACE)
        .log(java.util.logging.Level.INFO, "Silverpeas Logging Engine initialization...");
    Path configurationHome = SilverpeasResourcesProvider.getInstance().getLoggersRootPath();

    try (Stream<Path> loggers = Files.list(configurationHome)) {
      configs.clear();
      configs.putAll(loggers
          .filter(p -> p.getFileName().toString().endsWith(LOGGER_CONF_FILE_SUFFIX))
          .map(this::loadLoggerConfiguration)
          .collect(Collectors.toMap(LoggerConfiguration::getNamespace, l -> l)));
    } catch (IOException | SilverpeasRuntimeException e) {
      java.util.logging.Logger.getLogger(THIS_LOGGER_NAMESPACE)
          .log(java.util.logging.Level.WARNING, e.getMessage(), e);
    }
    if (configs.isEmpty()) {
      java.util.logging.Logger.getLogger(THIS_LOGGER_NAMESPACE)
          .log(java.util.logging.Level.WARNING,
              "No logging configuration files found for Silverpeas");
    }
  }

  /**
   * Gets the configuration parameters for the logger with the specified namespace.
   *
   * @param namespace a logger namespace.
   * @return the configuration of the logger defined for the specified namespace.
   */
  public LoggerConfiguration getLoggerConfiguration(String namespace) {
    Map<String, LoggerConfiguration> loggerConfigurations = getLoggerConfigurations();
    return loggerConfigurations.computeIfAbsent(namespace, ns -> new LoggerConfiguration(null, ns));
  }

  /**
   * Saves the configuration of the logger referred by the specified configuration instance.
   * <p>
   * If no configuration exists for the logger referred by the configuration object, then nothing is
   * done.
   *
   * @param configuration the new configuration of the logger defined for the specified Silverpeas
   * module.
   */
  public void saveLoggerConfiguration(LoggerConfiguration configuration) {
    Map<String, LoggerConfiguration> loggerConfigurations = getLoggerConfigurations();
    if (loggerConfigurations.containsKey(configuration.getNamespace()) &&
        configuration.hasConfigurationFile()) {
      LoggerConfiguration existing = loggerConfigurations.get(configuration.getNamespace());
      Properties properties = new Properties();

      try (InputStream input = Files.newInputStream(configuration.getConfigurationFile())) {
        properties.load(input);
      } catch (IOException e) {
        java.util.logging.Logger.getLogger(THIS_LOGGER_NAMESPACE)
            .log(java.util.logging.Level.WARNING, e.getMessage(), e);
      }

      if (configuration.getLevel() == null) {
        properties.remove(LOGGER_LEVEL);
        existing.setLevel(null);
      } else {
        properties.setProperty(LOGGER_LEVEL, configuration.getLevel().name());
        existing.setLevel(configuration.getLevel());
      }

      try (OutputStream output = Files.newOutputStream(configuration.getConfigurationFile())) {
        properties.store(output, null);
      } catch (IOException e) {
        java.util.logging.Logger.getLogger(THIS_LOGGER_NAMESPACE)
            .log(java.util.logging.Level.WARNING, e.getMessage(), e);
      }
    }
  }

  /**
   * Gets the available configuration of all the Silverpeas loggers. If a logger has no
   * configuration file, then it isn't taken into account.
   *
   * @return a set of logger configurations sorted by logger namespace.
   */
  public Set<LoggerConfiguration> getAvailableLoggerConfigurations() {
    Collection<LoggerConfiguration> allConfigurations = getLoggerConfigurations().values();
    return allConfigurations.stream()
        .filter(LoggerConfiguration::hasConfigurationFile)
        .collect(Collector.of(TreeSet::new, TreeSet::add, (left, right) -> {
          left.addAll(right);
          return left;
        }));
  }

  private LoggerConfiguration loadLoggerConfiguration(Path loggerConfFile) {
    Properties loggerProperties = new Properties();
    try (InputStream input = Files.newInputStream(loggerConfFile)) {
      loggerProperties.load(input);
    } catch (IOException e) {
      throw new SilverpeasRuntimeException(e);
    }
    String namespace = loggerProperties.getProperty(LOGGER_NAMESPACE);
    Level level = null;
    String strLevel = loggerProperties.getProperty(LOGGER_LEVEL);
    if (strLevel != null && !strLevel.trim().isEmpty()) {
      try {
        level = Level.valueOf(strLevel);
      } catch (Exception t) {
        java.util.logging.Logger.getLogger(THIS_LOGGER_NAMESPACE)
            .log(java.util.logging.Level.SEVERE, t.getMessage(), t);
      }
    }
    return new LoggerConfiguration(loggerConfFile, namespace).withLevel(level);
  }

  public static class LoggerConfiguration implements Comparable<LoggerConfiguration> {
    private final String namespace;
    private final Path file;
    private Level level;

    LoggerConfiguration(Path configFile, String namespace) {
      this.file = configFile;
      this.namespace = namespace;
    }

    public LoggerConfiguration withLevel(final Level level) {
      setLevel(level);
      return this;
    }

    private Path getConfigurationFile() {
      return file;
    }

    protected boolean hasConfigurationFile() {
      return file != null && Files.exists(file) && Files.isRegularFile(file);
    }

    public String getNamespace() {
      return namespace;
    }

    public Level getLevel() {
      return level;
    }

    public void setLevel(final Level level) {
      this.level = level;
    }

    @Override
    public int compareTo(final LoggerConfiguration other) {
      return this.getNamespace().compareTo(other.getNamespace());
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof LoggerConfiguration)) {
        return false;
      }

      final LoggerConfiguration that = (LoggerConfiguration) o;
      return compareTo(that) == 0;
    }

    @Override
    public int hashCode() {
      return Objects.hash(getNamespace());
    }
  }

}
