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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.silverpeas.kernel.SilverpeasResourcesLocation;
import org.silverpeas.kernel.util.SystemWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalizationBundleTest {

  private static final String LOCALIZATION_BUNDLE = "org.silverpeas.test.multilang.l10n";

  @BeforeAll
  public static void initSystemProperties() {
    SystemWrapper.getInstance().setProperty("FOO", "/home/silveruser/webapp/images/logos");
  }

  @Test
  void getLocalizationBundleOfDefaultLocale() {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(LOCALIZATION_BUNDLE);
    assertThat(bundle.exists(), is(true));
    assertThat(bundle.getBaseBundleName(), is(LOCALIZATION_BUNDLE));
    assertThat(bundle.getLocale().getLanguage(), is(Locale.getDefault().getLanguage()));
  }

  @Test
  void getNonExistingL10nProperty() {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(LOCALIZATION_BUNDLE);
    assertThat(bundle.exists(), is(true));
    assertThrows(MissingResourceException.class, () -> bundle.getString("Foo"));
  }

  @ParameterizedTest
  @CsvSource({
      "fr, Français, Anglais, Allemand, /home/silveruser/webapp/images/logos/fr/logo-language.png",
      "en, French, English, German, /home/silveruser/webapp/images/logos/en/logo-language.png"
  })
  void getCorrectLocalizedPropertyForEachLocale(Locale locale, String valueFr, String valueEn,
      String valueDe, String iconPath) {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(LOCALIZATION_BUNDLE, locale);
    assertThat(bundle.getLocale(), is(locale));
    assertThat(bundle.getString("language_fr"), is(valueFr));
    assertThat(bundle.getString("language_en"), is(valueEn));
    assertThat(bundle.getString("language_de"), is(valueDe));
    assertThat(bundle.getString("icon"), is(iconPath));
  }

  @Test
  void getParametrizedText() {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(LOCALIZATION_BUNDLE, Locale.FRENCH);
    String value1 = bundle.getStringWithParams("cle.param.2", "Bidule", "Toto");
    assertThat(value1, is("\"Bidule\" est plus petit que \"Toto\""));

    String value2 = bundle.getStringWithParams("cle.param.2bis", "Bidule", "Toto");
    assertThat(value2, is("'Bidule' est plus petit que 'Toto'"));
  }

  @Test
  void getParametrizedTextWithXSS() {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(LOCALIZATION_BUNDLE, Locale.FRENCH);
    String value = bundle.getStringWithParams("cle.param.2", "Bidule",
        "<img src=x onerror=prompt(document.cookie)/>");
    assertThat(value, is("\"Bidule\" est plus petit que \"&lt;img src=x onerror=prompt(document" +
        ".cookie)/&gt;\""));
  }

  @Test
  void getParametrizedTextWithNumber() {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(LOCALIZATION_BUNDLE,
        Locale.ENGLISH);
    String value1 = bundle.getStringWithParams("cle.param.1", 0);
    assertThat(value1, is("just"));

    String value2= bundle.getStringWithParams("cle.param.1", 1);
    assertThat(value2, is("1 hour"));

    String value3= bundle.getStringWithParams("cle.param.1", 3);
    assertThat(value3, is("3 hours"));
  }

  @Test
  void getMissingPropertyShouldThrowMissingResourceException() {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(LOCALIZATION_BUNDLE);
    assertThat(bundle.containsKey("Foo"), is(false));
    assertThrows(MissingResourceException.class, () -> bundle.getString("Foo"));
  }

  @Test
  void changeLocale() {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(LOCALIZATION_BUNDLE, "fr");
    assertThat(bundle.getLocale(), is(Locale.FRENCH));
    assertThat(bundle.getString("language_fr"), is("Français"));

    bundle.changeLocale("en");
    assertThat(bundle.getLocale(), is(Locale.ENGLISH));
    assertThat(bundle.getString("language_fr"), is("French"));
  }

  @Test
  void l10nBundleWrapsAlsoGeneralTranslations() {
    LocalizationBundle generalBundle = ResourceLocator.getGeneralLocalizationBundle(Locale.ENGLISH);
    assertThat(generalBundle.getString("GML.address"), is("Address"));

    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(LOCALIZATION_BUNDLE,
        Locale.ENGLISH);
    assertThat(bundle.getString("GML.address"), is("Address"));
  }

  @Test
  void getSpecificKey() throws IOException {
    String relativePath = LOCALIZATION_BUNDLE.replace(".", "/") + "_fr.properties";
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(LOCALIZATION_BUNDLE, Locale.FRENCH);
    Properties properties = loadBundleAsProperties(relativePath);

    Enumeration<String> specificKeys = bundle.getSpecificKeys();
    int count = 0;
    while (specificKeys.hasMoreElements()) {
      count++;
      String key = specificKeys.nextElement();
      assertThat(properties, hasKey(key));
    }
    assertThat(count, is(properties.keySet().size()));
  }

  Properties loadBundleAsProperties(final String bundleRelativePath) throws IOException {
    Path l10nRootPath = SilverpeasResourcesLocation.getInstance().getL10nBundlesRootPath();
    Path bundlePath = l10nRootPath.resolve(bundleRelativePath);
    Properties properties = new Properties();
    properties.load(Files.newInputStream(bundlePath));
    return properties;
  }
}