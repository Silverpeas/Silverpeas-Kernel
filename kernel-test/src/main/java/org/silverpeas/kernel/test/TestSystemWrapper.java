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

import org.silverpeas.kernel.util.SystemWrapper;

import javax.annotation.Priority;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple implementation of the {@link SystemWrapper} to be used in the unit tests. It wraps
 * {@link System} by allowing to set additional both system properties and environment variables.
 *
 * @author mmoquillon
 */
@Priority(100)
public class TestSystemWrapper implements SystemWrapper {

  private final Map<String, String> extendedEnvs = new ConcurrentHashMap<>();

  public TestSystemWrapper() {
    extendedEnvs.putAll(System.getenv());
    extendedEnvs.putIfAbsent("SILVERPEAS_HOME",
        TestContext.getInstance().getPathOfTestResources().toString());
  }

  @Override
  public String getenv(String name) {
    return extendedEnvs.get(name);
  }

  @Override
  public Map<String, String> getenv() {
    return extendedEnvs;
  }

  @Override
  public void setProperties(Properties props) {
    System.setProperties(props);
  }

  @Override
  public String setProperty(String key, String value) {
    return System.setProperty(key, value);
  }

}
