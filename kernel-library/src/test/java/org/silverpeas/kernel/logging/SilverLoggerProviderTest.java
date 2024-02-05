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

import org.junit.jupiter.api.Test;
import org.silverpeas.kernel.util.Mutable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class SilverLoggerProviderTest {

  @Test
  void ensureSilverLoggerProviderIsASingleton() {
    SilverLoggerProvider provider1 = SilverLoggerProvider.getInstance();
    SilverLoggerProvider provider2 = SilverLoggerProvider.getInstance();
    assertThat(provider2, is(provider1));
  }

  @Test
  void ensureASingleLoggerConfigurationManagerIsUsed() {
    LoggerConfigurationManager manager1 =
        SilverLoggerProvider.getInstance().getConfigurationManager();
    LoggerConfigurationManager manager2 =
        SilverLoggerProvider.getInstance().getConfigurationManager();
    assertThat(manager2, is(manager1));
  }

  @Test
  void ensureTheSameLoggerConfigurationManagerIsUsedAmongDifferentThreads() throws InterruptedException {
    SilverLoggerProvider provider = SilverLoggerProvider.getInstance();
    Mutable<LoggerConfigurationManager> manager1 = Mutable.empty();
    Mutable<LoggerConfigurationManager> manager2 = Mutable.empty();

    Thread t1 = new Thread(() -> manager1.set(provider.getConfigurationManager()));
    Thread t2 = new Thread(() -> manager2.set(provider.getConfigurationManager()));
    t1.start();
    t2.start();
    t1.join();
    t2.join();

    assertThat(manager2, is(manager1));
  }

  @Test
  void ensureLoggerGotMatchesTheNamespaceAndLevel() {
    SilverLoggerProvider provider = SilverLoggerProvider.getInstance();
    SilverLogger logger = provider.getLogger(this);
    assertThat(logger, notNullValue());
    assertThat(logger.getNamespace(), is("silverpeas.kernel.logging"));
    assertThat(logger.getLevel(), is(Level.INFO));
  }
}