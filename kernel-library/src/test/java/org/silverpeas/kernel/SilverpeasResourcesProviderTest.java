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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.kernel.cache.service.ApplicationCacheService;
import org.silverpeas.kernel.test.TestContext;
import org.silverpeas.kernel.test.TestScopedResourcesProvider;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Test the expected {@link SilverpeasResourcesProvider} implementation is well loaded by SPI and only a single instance
 * is returned. The properties for the unit tests should be defined from the maven.properties resource file in the
 * classpath of the tests.
 *
 * @author mmoquillon
 */
class SilverpeasResourcesProviderTest {

  @Test
  @DisplayName("The instance of the expected implementation is well loaded by SPI")
  void getInstanceLoadsInstanceBySPI() {
    SilverpeasResourcesProvider instance = SilverpeasResourcesProvider.getInstance();
    assertThat(instance, notNullValue());
    assertThat(instance, instanceOf(TestScopedResourcesProvider.class));
  }

  @Test
  @DisplayName("The instance of the expected implementation is well loaded by SPI")
  void getInstanceWillCacheTheSingleInstance() {
    SilverpeasResourcesProvider instance = SilverpeasResourcesProvider.getInstance();
    SilverpeasResourcesProvider cachedInstance =
        ApplicationCacheService.getInstance().getCache()
            .get(SilverpeasResourcesProvider.class.getSimpleName() + "#instance",
                SilverpeasResourcesProvider.class);
    assertThat(cachedInstance, is(instance));
  }

  @Test
  @DisplayName("The SilverpeasResourcesProvider class is a singleton")
  void theSilverpeasResourcesProviderIsASingleton() {
    SilverpeasResourcesProvider instance = SilverpeasResourcesProvider.getInstance();
    SilverpeasResourcesProvider another = SilverpeasResourcesProvider.getInstance();
    assertThat(another, is(instance));
  }

  @Test
  @DisplayName("For the unit tests, the root path of the loggers definitions are located in a given subdirectory of " +
      "the Maven test target directory")
  void getLoggersRootPath() {
    TestContext context = new TestContext();
    TestScopedResourcesProvider provider =
        (TestScopedResourcesProvider) SilverpeasResourcesProvider.getInstance();
    Path rootPath = provider.getLoggersRootPath();
    assertThat(rootPath, notNullValue());
    assertThat(rootPath.toString(), startsWith(context.getMavenProperties().getProperty("test.resources.directory")));
  }

  @Test
  @DisplayName("For the unit tests, the root path of all the l10n resource bundles are located in" +
      " the Maven test target directory")
  void getL10nBundlesRootPath() {
    TestContext context = new TestContext();
    TestScopedResourcesProvider provider =
        (TestScopedResourcesProvider) SilverpeasResourcesProvider.getInstance();
    Path rootPath = provider.getL10nBundlesRootPath();
    assertThat(rootPath, notNullValue());
    assertThat(rootPath.toString(),
        is(context.getMavenProperties().getProperty("test.resources.directory")));
  }

  @Test
  @DisplayName("For the unit tests, the root path of all the configuration resource bundles are " +
      "located in the Maven test target directory")
  void getConfigurationFilesRootPath() {
    TestContext context = new TestContext();
    TestScopedResourcesProvider provider =
        (TestScopedResourcesProvider) SilverpeasResourcesProvider.getInstance();
    Path rootPath = provider.getL10nBundlesRootPath();
    assertThat(rootPath, notNullValue());
    assertThat(rootPath.toString(),
        is(context.getMavenProperties().getProperty("test.resources.directory")));
  }
}