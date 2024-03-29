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
package org.silverpeas.kernel.bundle;

import org.silverpeas.kernel.annotation.Technical;
import org.silverpeas.kernel.util.StringUtil;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Defines the frequency for {@link ResourceBundle} cache invalidation. The default period is 4
 * hours. You can specify a period in ms with the System property
 * <code>silverpeas.refresh.configuration</code>.
 *
 * @author ehugonnet
 */
@Technical
class ConfigurationControl extends ResourceBundle.Control {

  public static final long DEFAULT_RELOAD = 14400000L; // 4 hours
  private long reload = DEFAULT_RELOAD;
  public static final String REFRESH_CONFIG = "silverpeas.refresh.configuration";

  ConfigurationControl() {
    String refresh = System.getProperty(REFRESH_CONFIG);
    if (StringUtil.isDefined(refresh) && StringUtil.isLong(refresh)) {
      reload = Long.parseLong(refresh);
    }
  }

  @Override
  public long getTimeToLive(String baseName, Locale locale) {
    return reload;
  }

}
