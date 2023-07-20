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

package org.silverpeas.kernel.test;

import org.silverpeas.kernel.SilverpeasResourcesProvider;
import org.silverpeas.kernel.SilverpeasRuntimeException;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Implementation of the {@link SilverpeasResourcesProvider} interface to be used in the scope of a
 * unit test.
 *
 * @author mmoquillon
 */
public class TestScopedResourcesProvider implements SilverpeasResourcesProvider {
  private final TestContext context = new TestContext();

  @Override
  public Path getLoggersRootPath() {
    Path resourcesDirPath = context.getPathOfTestResources();
    return resourcesDirPath.resolve(Path.of("org", "silverpeas", "test", "logging"));
  }

  @Override
  public Path getL10nBundlesRootPath() {
    return getConfigurationFilesRootPath();
  }

  @Override
  public Path getConfigurationFilesRootPath() {
    return context.getPathOfTestResources();
  }

}
