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

package org.silverpeas.kernel.exception;

import org.silverpeas.kernel.BeanContainer;
import org.silverpeas.kernel.SilverpeasRuntimeException;

/**
 * Exception thrown when there are more than one candidate at a request for a single object to a
 * service. This exception is used by the {@link BeanContainer}.
 *
 * @author mmoquillon
 */
public class MultipleCandidateException extends SilverpeasRuntimeException {

  public MultipleCandidateException(String message) {
    super(message);
  }

  public MultipleCandidateException(String message, Throwable cause) {
    super(message, cause);
  }

  public MultipleCandidateException(Throwable cause) {
    super(cause);
  }
}
