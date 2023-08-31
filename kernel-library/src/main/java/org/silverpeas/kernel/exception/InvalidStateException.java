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

package org.silverpeas.kernel.exception;

import org.silverpeas.kernel.SilverpeasRuntimeException;

/**
 * Exception thrown when an operation has been invoked on an object that is in a state that doesn't
 * match with the expectation of the operation execution.
 *
 * @author mmoquillon
 */
@SuppressWarnings("unused")
public class InvalidStateException extends SilverpeasRuntimeException {

  public InvalidStateException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidStateException(String message) {
    super(message);
  }
}