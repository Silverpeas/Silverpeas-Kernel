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

package org.silverpeas.kernel.test.extension;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SilverpeasBundle;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides easy stubbing on the {@link LocalizationBundle} in unit tests. This class is to declare
 * as a field of the unit test class a {@link LocalizationBundle} that will be used by the tests and
 * with which some translations can be preconfigured or modified without having to use a set of
 * physical properties files (a {@link LocalizationBundle} is mapped to a l10n properties file). By
 * annotating the field with the {@link org.junit.jupiter.api.extension.RegisterExtension}
 * annotation, the required resources initialization is performed before the execution of a unit
 * test and the resource cleanup is applied once the unit test resumed.
 *
 * @author silveryocha
 */
@SuppressWarnings("unused")
public class LocalizationBundleStub implements BeforeEachCallback, AfterEachCallback {

  /**
   * The default ISO 639-1 code of the french language for the {@link LocalizationBundle}
   */
  public static final String LANGUAGE_FR = "fr";
  /**
   * The default ISO 639-1 code of the english language for the {@link LocalizationBundle}
   */
  public static final String LANGUAGE_EN = "en";
  /**
   * The default ISO 639-1 code of the german language for the {@link LocalizationBundle}
   */
  public static final String LANGUAGE_DE = "de";
  /**
   * All the languages the {@link LocalizationBundle} actually supports.
   */
  public static final String[] LANGUAGE_ALL = new String[] {LANGUAGE_FR, LANGUAGE_EN, LANGUAGE_DE};

  private final String bundleName;
  private final Map<String, Map<String, String>> bundleMap = new HashMap<>();
  private final Set<String> bundleNames = new HashSet<>();
  private Locale currentLocale;
  private Map<String, SilverpeasBundle> bundleCache;

  private final Set<String> languages;

  public LocalizationBundleStub(final String bundleName, String ...languages) {
    this.bundleName = bundleName;
    if (languages.length == 0) {
      this.languages = new HashSet<>();
      this.languages.add(Locale.FRANCE.getLanguage());
    } else {
      this.languages = Stream.of(languages).collect(Collectors.toSet());
    }
  }

  /**
   * Puts a couple key / value linked to default locale set by the extension itself.
   *
   * @param key the key.
   * @param value the value.
   * @return itself.
   */
  public LocalizationBundleStub put(final String key, final String value) {
    return put(Locale.getDefault(), key, value);
  }

  /**
   * Puts a couple key / value linked to specified locale.
   *
   * @param locale the locale.
   * @param key the key.
   * @param value the value.
   * @return itself.
   */
  public LocalizationBundleStub put(final String locale, final String key, final String value) {
    return put(new Locale(locale), key, value);
  }

  @Override
  public void beforeEach(final ExtensionContext context) throws Exception {
    init();
  }

  @Override
  public void afterEach(final ExtensionContext context) {
    clear();
  }

  @SuppressWarnings("unchecked")
  private void init() throws IllegalAccessException {
    currentLocale = Locale.getDefault();
    Locale.setDefault(Locale.FRANCE);
    final String newDefaultLocale = Locale.getDefault().getLanguage();
    bundleCache =
        (Map<String, SilverpeasBundle>) FieldUtils.readStaticField(ResourceLocator.class,
            "bundles", true);
    bundleCache.put(bundleName, new LocalizationBundleTest(newDefaultLocale, bundleMap));
    bundleNames.add(bundleName);
    languages.forEach(l -> {
      final String bundleLocaleName = bundleName + "_" + l;
      bundleCache.put(bundleLocaleName, new LocalizationBundleTest(l, bundleMap));
      bundleNames.add(bundleLocaleName);
    });
  }

  protected void clear() {
    Locale.setDefault(currentLocale);
    bundleNames.forEach(bundleCache::remove);
  }

  private LocalizationBundleStub put(final Locale locale, final String key, final String value) {
    getBundleMap(locale, bundleMap).put(key, value);
    return this;
  }

  private static Map<String, String> getBundleMap(final Locale locale,
      final Map<String, Map<String, String>> bundleMap) {
    return bundleMap.computeIfAbsent(locale.getLanguage(), l -> new HashMap<>());
  }

  private static class LocalizationBundleTest extends LocalizationBundle {
    private final Map<String, Map<String, String>> bundleMap;

    private LocalizationBundleTest(String locale,
        final Map<String, Map<String, String>> bundleMap) {
      super("", new Locale(locale), null, true);
      this.bundleMap = bundleMap;
    }

    @Override
    protected Object handleGetObject(@NonNull final String key) {
      final Map<String, String> localeOne = getBundleMap(getLocale(), bundleMap);
      Object result = localeOne.get(key);
      if (result == null) {
        final Map<String, String> defaultOne = getBundleMap(Locale.getDefault(), bundleMap);
        result = defaultOne.get(key);
      }
      return result;
    }
  }
}
  