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
package org.silverpeas.kernel;

import org.silverpeas.kernel.cache.service.ApplicationCacheService;
import org.silverpeas.kernel.cache.service.CacheService;

import java.nio.file.Path;
import java.util.ServiceLoader;

/**
 * Provider of the different resource files required by Silverpeas.
 *
 * @author mmoquillon
 * @implSpec The {@link SilverpeasResourcesProvider} implementation is loaded from the the Java SPI
 * (Java Service Provider Interface) and its instance is cached into the application cache to be
 * easily retrieved later.
 * @implNote Whatever the number of available implementations of this interface, only the first
 * found one is used.
 */
public interface SilverpeasResourcesProvider {

  static SilverpeasResourcesProvider getInstance() {
    CacheService<?> cacheService = ApplicationCacheService.getInstance();
    return cacheService.getCache().computeIfAbsent(
        SilverpeasResourcesProvider.class.getSimpleName() + "#instance",
        SilverpeasResourcesProvider.class,
        () -> ServiceLoader.load(SilverpeasResourcesProvider.class)
            .findFirst()
            .orElseThrow(() -> new SilverpeasRuntimeException(
                "No SilverpeasResourcesProvider found! At least one should be available!")));
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
