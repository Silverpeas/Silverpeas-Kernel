/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
import org.silverpeas.kernel.logging.SilverLogger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The resource bundles and the properties files can be located into a particular directory out of
 * the the classpath of a running Silverpeas application. Therefore this class loader aims to manage
 * the access to the resources in this particular location; it acts as a bridge between the current
 * hierarchy of class loaders and this particular unmanaged location.
 * <p>
 * By default, when a resource is asked, it looks for in the current hierarchy of class loaders
 * before to seek the resource into the resources directory in the Silverpeas home directory.
 *
 * @author ehugonnet
 */
@Technical
public class ConfigurationClassLoader extends ClassLoader {

  private final Path resourceRootPath;

  public ConfigurationClassLoader(final Path resourcesRootPath, final ClassLoader parent) {
    super(parent);
    this.resourceRootPath = resourcesRootPath;
  }

  @Override
  public synchronized void clearAssertionStatus() {
    super.clearAssertionStatus();
  }

  @Override
  public URL getResource(String name) {
    URL resource = super.getResource(name);
    if (resource == null && name != null) {
      Path file = resourceRootPath.resolve(name);
      if (Files.exists(file) && Files.isRegularFile(file)) {
        try {
          resource = file.toUri().toURL();
        } catch (MalformedURLException ex) {
          SilverLogger.getLogger(this).error("Malformed URL for resource " + name, ex);
        }
      }
    }
    return resource;
  }

  @Override
  public InputStream getResourceAsStream(String name) {
    InputStream inputStream = super.getResourceAsStream(name);
    if (inputStream == null && name != null) {
      Path file = resourceRootPath.resolve(name);
      if (Files.exists(file) && Files.isRegularFile(file)) {
        try {
          inputStream = Files.newInputStream(file);
        } catch (FileNotFoundException e) {
          SilverLogger.getLogger(this).error("Resource " + name + " not found", e);
        } catch (IOException e) {
          SilverLogger.getLogger(this).error("Unexpected error while opening the resource "
              + name, e);
        }
      }
    }
    return inputStream;
  }

  @Override
  protected synchronized Class<?> loadClass(String name, boolean resolve)
      throws ClassNotFoundException {
    return super.loadClass(name, resolve);
  }

  @Override
  public synchronized void setClassAssertionStatus(String className, boolean enabled) {
    super.setClassAssertionStatus(className, enabled);
  }

  @Override
  public synchronized void setDefaultAssertionStatus(boolean enabled) {
    super.setDefaultAssertionStatus(enabled);
  }

  @Override
  public synchronized void setPackageAssertionStatus(String packageName, boolean enabled) {
    super.setPackageAssertionStatus(packageName, enabled);
  }

}
