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
import org.silverpeas.kernel.logging.LoggerConfigurationManager.LoggerConfiguration;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

class LoggerConfigurationManagerTest {

  private LoggerConfigurationManager manager;

  @BeforeEach
  void initLoggerConfigurationManager() {
    manager = new LoggerConfigurationManager();
    manager.loadAllConfigurationFiles();
  }

  @Test
  void allConfigurationFilesAreLoaded() {
    assertThat(manager.getAvailableLoggerConfigurations().size(), is(7));
  }

  @Test
  void getLoggerConfigurationWithoutLevelOfAnExistingNamespace() {
    final String namespace = "silverpeas.commons.util.test";
    LoggerConfiguration configuration = manager.getLoggerConfiguration(namespace);
    assertThat(configuration, not(nullValue()));
    assertThat(configuration.getNamespace(), is(namespace));
    assertThat(configuration.getLevel(), is(nullValue()));
    assertThat(configuration.hasConfigurationFile(), is(true));
  }

  @Test
  void getLoggerConfigurationWithLevelOfAnExistingNamespace() {
    final String namespace = "silverpeas.commons.util";
    LoggerConfiguration configuration = manager.getLoggerConfiguration(namespace);
    assertThat(configuration, not(nullValue()));
    assertThat(configuration.getNamespace(), is(namespace));
    assertThat(configuration.getLevel(), is(Level.INFO));
    assertThat(configuration.hasConfigurationFile(), is(true));
  }

  @Test
  void getLoggerConfigurationOfANonExistingNamespace() {
    final String namespace = "tartempion.toto";
    LoggerConfiguration configuration = manager.getLoggerConfiguration(namespace);
    assertThat(configuration, not(nullValue()));
    assertThat(configuration.getNamespace(), is(namespace));
    assertThat(configuration.getLevel(), is(nullValue()));
    assertThat(configuration.hasConfigurationFile(), is(false));
  }

  @Test
  void updateLoggerConfigurationOfAnExistingNamespacePersistsTheChange() {
    final String namespace = "silverpeas.commons";
    final Level level = Level.DEBUG;
    LoggerConfiguration configuration = manager.getLoggerConfiguration(namespace);
    assertThat(configuration, not(nullValue()));
    assertThat(configuration.getLevel(), not(level));

    // update configuration and ensures the change is done
    configuration.setLevel(level);
    manager.saveLoggerConfiguration(configuration);
    configuration = manager.getLoggerConfiguration(namespace);
    assertThat(configuration.getLevel(), is(level));

    // reload configuration files to ensure the configuration was really persisted
    manager.loadAllConfigurationFiles();
    configuration = manager.getLoggerConfiguration(namespace);
    assertThat(configuration.getLevel(), is(level));
  }

  @Test
  void updateLoggerConfigurationOfANonExistingNamespaceSaveItInMemoryOnly() {
    final String namespace = "tartempion.toto";
    final Level level = Level.DEBUG;
    LoggerConfiguration configuration = manager.getLoggerConfiguration(namespace);
    assertThat(configuration, not(nullValue()));
    assertThat(configuration.getLevel(), is(nullValue()));
    assertThat(configuration.hasConfigurationFile(), is(false));

    // the logger is changed
    configuration.setLevel(level);
    manager.saveLoggerConfiguration(configuration);
    configuration = manager.getLoggerConfiguration(namespace);
    assertThat(configuration.getLevel(), is(level));
    assertThat(configuration.hasConfigurationFile(), is(false));

    // but the change isn't persisted as the logger has no config file
    manager.loadAllConfigurationFiles();
    configuration = manager.getLoggerConfiguration(namespace);
    assertThat(configuration.getLevel(), is(nullValue()));
    assertThat(configuration.hasConfigurationFile(), is(false));
  }
}