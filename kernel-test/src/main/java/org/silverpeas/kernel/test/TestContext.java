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

package org.silverpeas.kernel.test;

import org.silverpeas.kernel.SilverpeasRuntimeException;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Context of a unit test. Used to access the information of the context of such a test. This class
 * can be extended to add other information required by unit tests for their execution.
 *
 * @author mmoquillon
 */
@SuppressWarnings("unused")
public class TestContext {

  private static final TestContext INSTANCE = new TestContext();
  private Properties mavenProperties;

  public static TestContext getInstance() {
    return INSTANCE;
  }

  private TestContext() {
  }

  /**
   * Gets the properties of the project with which the unit test is configured.
   *
   * @return the Maven properties
   */
  public Properties getMavenProperties() {
    if (mavenProperties == null) {
      mavenProperties = loadMavenProperties();
    }
    return mavenProperties;
  }

  private Properties loadMavenProperties() {
    mavenProperties = new Properties();
    try (InputStream is = getClass().getClassLoader().getResourceAsStream("maven.properties")) {
      mavenProperties.load(is);
    } catch (Exception e) {
      throw new SilverpeasRuntimeException("Unable to load maven.properties for unit tests!", e);
    }
    return mavenProperties;
  }

  /**
   * Gets the path of the directory in which are located the resources available to the unit test
   * during its execution.
   *
   * @return the path of the test resources root directory.
   */
  public Path getPathOfTestResources() {
    Properties testProperties = getMavenProperties();
    String pathProperty = testProperties.getProperty("test.resources.directory", "");
    return Path.of(pathProperty);
  }

  /**
   * Gets the path of the directory into which are build the source code of the project and into
   * which the resources of the project are also copied. This doesn't concern the test code and the
   * test resources; please see {@link TestContext#getPathOfTestResources()} for that.
   * @return the path of the project build directory.
   */
  public Path getPathOfBuildDirectory() {
    Properties testProperties = getMavenProperties();
    String pathProperty = testProperties.getProperty("project.build.directory", "");
    return Path.of(pathProperty);
  }

  /**
   * Gets the path of the directory in which are located the logger configuration files that are
   * available to the unit test during its execution.
   *
   * @return the path of the logger configurations root directory.
   */
  public Path getLoggerRootHomePath() {
    return getPathOfTestResources().resolve(Path.of("org", "silverpeas", "util", "logging"));
  }
}
