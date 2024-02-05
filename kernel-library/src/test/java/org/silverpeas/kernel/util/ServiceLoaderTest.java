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

package org.silverpeas.kernel.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.util.test.NotSPITestService;
import org.silverpeas.kernel.util.test.TestService;
import org.silverpeas.kernel.util.test.TestServiceWithPriority;
import org.silverpeas.kernel.util.test.impl.TestService1;
import org.silverpeas.kernel.util.test.impl.TestServiceWithPriority3;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceLoaderTest {

  @Test
  @DisplayName("Loading a service by SPI should return the instance of the highest priority " +
      "implementation")
  void loadServiceByTakingIntoAccountPriority() {
    TestServiceWithPriority service = ServiceLoader.load(TestServiceWithPriority.class);
    assertThat(service, is(instanceOf(TestServiceWithPriority3.class)));
  }

  @Test
  @DisplayName("Loading a service by SPI should return the instance of the first found " +
      "implementation when no one is more priority than other")
  void loadServiceWithoutAnyPriority() {
    TestService service = ServiceLoader.load(TestService.class);
    assertThat(service, is(instanceOf(TestService1.class)));
  }

  @Test
  @DisplayName("Loading a service by SPI with no available implementations should fail")
  void loadServiceWithoutAnyImplementations() {
    assertThrows(SilverpeasRuntimeException.class,
        () -> ServiceLoader.load(NotSPITestService.class));
  }

  @Test
  @DisplayName("Getting a service by SPI should load and cache the instance of the highest  " +
      "implementation for further requests")
  void getServiceByTakingIntoAccountPriorityShouldReturnSameInstance() {
    TestServiceWithPriority expected = ServiceLoader.get(TestServiceWithPriority.class);
    assertThat(expected, is(instanceOf(TestServiceWithPriority3.class)));

    TestServiceWithPriority actual = ServiceLoader.get(TestServiceWithPriority.class);
    assertThat(actual, is(expected));
  }

  @Test
  @DisplayName("Getting a service by SPI should load and cache the instance of first " +
      "implementation found for further requests when no implementations have priority set")
  void getServiceWithoutAnyPriorityShouldReturnSameInstance() {
    TestService expected = ServiceLoader.get(TestService.class);
    assertThat(expected, is(instanceOf(TestService1.class)));

    TestService actual = ServiceLoader.get(TestService.class);
    assertThat(actual, is(expected));
  }

  @Test
  @DisplayName("Getting a service by SPI with no available implementations should fail")
  void getServiceWithoutAnyImplementations() {
    assertThrows(SilverpeasRuntimeException.class,
        () -> ServiceLoader.get(NotSPITestService.class));
  }
}
  