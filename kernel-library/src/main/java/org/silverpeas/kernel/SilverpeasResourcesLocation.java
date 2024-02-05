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
package org.silverpeas.kernel;

import org.silverpeas.kernel.util.ServiceLoader;

import java.nio.file.Path;

/**
 * Location of the different resource files required by Silverpeas. These resources include the
 * loggers configuration files, the Silverpeas properties files, the L10n bundles, and so on.
 *
 * @author mmoquillon
 * @implSpec The {@link SilverpeasResourcesLocation} implementation is loaded from the the Java SPI
 * (Java Service Provider Interface) and the single instance is kept in memory for further
 * retrieval.
 * @implNote Whatever the number of available implementations of this interface, only the first
 * found one is used. Nevertheless, by using the @{@link javax.annotation.Priority} annotation, the
 * implementation to use can be explicitly indicated.
 */
public interface SilverpeasResourcesLocation {

  /**
   * Gets the single object of the {@link SilverpeasResourcesLocation} singleton. If the
   * implementation isn't yet loaded, then it is loaded and instantiated once time.
   *
   * @return the single instance of {@link SilverpeasResourcesLocation}
   * @implNote The implementation is loaded and instantiated by Java SPI at first call. The single
   * instance is then kept in memory so that it is returned at each next call.
   */
  static SilverpeasResourcesLocation getInstance() {
    return ServiceLoader.get(SilverpeasResourcesLocation.class);
  }

  /**
   * Gets the path of the root directory into which are defined all the loggers to use by
   * Silverpeas. In this directory should be located the properties file defining and configuring
   * each of the loggers of Silverpeas.
   *
   * @return the path of the root directory of the Silverpeas loggers.
   */
  Path getLoggersRootPath();

  /**
   * Gets the path of the root directory into which are located the l10n resource bundles for
   * Silverpeas.
   *
   * @return the path of the root directory of the resource bundles.
   */
  Path getL10nBundlesRootPath();

  /**
   * Gets the path of the root directory into which are located the configuration resource files
   *
   * @return the path of the root directory of the properties files.
   */
  Path getConfigurationFilesRootPath();
}
