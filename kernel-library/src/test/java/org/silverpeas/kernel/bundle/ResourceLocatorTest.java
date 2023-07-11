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

package org.silverpeas.kernel.bundle;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.MissingResourceException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceLocatorTest {

  private static final String LOCALIZATION_BUNDLE = "org.silverpeas.test.multilang.l10n";
  private static final String SETTING_BUNDLE = "org.silverpeas.test.settings.usersCSVFormat";

  private static final String XML_SETTING_BUNDLE = "org.silverpeas.test.settings.dbDriverSettings";

  @Test
  void useGeneralSettingShouldSucceed() {
    SettingBundle bundle = ResourceLocator.getGeneralSettingBundle();
    assertThat(bundle.exists(), is(true));
    assertThat(bundle.getString("webdav.repository"), is("repository"));
  }

  @Test
  void useAGivenSettingBundleShouldSucceed() {
    SettingBundle bundle = ResourceLocator.getSettingBundle(SETTING_BUNDLE);
    assertThat(bundle.exists(), is(true));
    assertThat(bundle.getBoolean("Enabled"), is(true));
  }

  @Test
  void getANonExistingSettingBundleShouldSucceed() {
    SettingBundle bundle = ResourceLocator.getSettingBundle("org.silverpeas.tartempion.toto");
    assertThat(bundle, is(notNullValue()));
    assertThat(bundle.exists(), is(false));
  }

  @Test
  void useANonExistingSettingBundleShouldFail() {
    SettingBundle bundle = ResourceLocator.getSettingBundle("org.silverpeas.tartempion.toto");
    assertThat(bundle.exists(), is(false));
    assertThrows(MissingResourceException.class, () -> bundle.getString("Foo"));
  }

  @Test
  void useGeneralLocalizationBundleShouldSucceed() {
    LocalizationBundle beforeBundleEn = ResourceLocator.getGeneralLocalizationBundle("en");
    LocalizationBundle beforeBundleFr = ResourceLocator.getGeneralLocalizationBundle("fr");
    LocalizationBundle bundleEn = ResourceLocator.getGeneralLocalizationBundle("en");
    LocalizationBundle bundleFr = ResourceLocator.getGeneralLocalizationBundle("fr");

    assertThat(bundleEn, sameInstance(beforeBundleEn));
    assertThat(bundleFr, sameInstance(beforeBundleFr));

    assertThat(bundleEn, is(notNullValue()));
    assertThat(bundleEn.getString("GML.cancel"), is("Cancel"));
    assertThat(bundleEn.getString("GML.cancel"), is("Cancel"));
    assertThat(bundleFr, is(notNullValue()));
    assertThat(bundleFr.getString("GML.cancel"), is("Annuler"));
    assertThat(bundleFr.getString("GML.cancel"), is("Annuler"));
  }

  @Test
  void useAGivenLocalizationBundleShouldSucceed() {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(LOCALIZATION_BUNDLE, "en");
    assertThat(bundle.exists(), is(true));
    assertThat(bundle.getString("language_fr"), is("French"));
  }

  @Test
  void useAGivenLocalizationBundleWithoutSpecifiedLocaleShouldSucceed() {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(LOCALIZATION_BUNDLE);
    assertThat(bundle.exists(), is(true));
    assertThat(bundle.getString("language_fr"), not(emptyString()));
  }

  @Test
  void useAGivenLocalizationBundleForAMissingLocaleShouldLoadBundleOfDefaultLocale() {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(LOCALIZATION_BUNDLE, "pl");
    assertThat(bundle.exists(), is(true));
    assertThat(bundle.getLocale().getLanguage(), is(Locale.getDefault().getLanguage()));
  }

  @Test
  void getANonExistingLocalizationBundleShouldSucceed() {
    LocalizationBundle bundle =
        ResourceLocator.getLocalizationBundle("org.silverpeas.tartempion.multilang.toto");
    assertThat(bundle.exists(), is(false));
  }

  @Test
  void useANonExistingLocalizationBundleShouldFail() {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle("org.silverpeas.tartempion.multilang.toto");
    assertThat(bundle.exists(), is(false));
    assertThrows(MissingResourceException.class, () -> bundle.getString("Foo"));
  }

  @Test
  void getASettingBundleForALocalizedBundleShouldFail() {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(LOCALIZATION_BUNDLE);
    assertThat(bundle.exists(), is(true));

    assertThrows(ClassCastException.class, () -> ResourceLocator.getSettingBundle(LOCALIZATION_BUNDLE));
  }

  @Test
  void getALocalizedBundleForASettingBundleShouldFail() {
    SettingBundle bundle = ResourceLocator.getSettingBundle(SETTING_BUNDLE);
    assertThat(bundle.exists(), is(true));

    assertThrows(ClassCastException.class, () -> ResourceLocator.getLocalizationBundle(SETTING_BUNDLE));
  }

  @Test
  void useAGivenXmlSettingBundleShouldSucceed() {
    XmlSettingBundle bundle =
        ResourceLocator.getXmlSettingBundle(XML_SETTING_BUNDLE);
    assertThat(bundle.exists(), is(true));
    assertThat(bundle.getString("DBDriver-configuration.Drivers-Definition.BD_Silverpeas-configuration.DriverName"),
        is(notNullValue()));
  }

  @Test
  void getANonExistingXmlSettingBundleShouldSucceed() {
    XmlSettingBundle bundle = ResourceLocator.getXmlSettingBundle("org.silverpeas.tartempion.xmlconf");
    assertThat(bundle.exists(), is(false));
  }

  @Test
  void useANonExistingXmlSettingBundleShouldFail() {
    XmlSettingBundle bundle = ResourceLocator.getXmlSettingBundle("org.silverpeas.tartempion.xmlconf");
    assertThat(bundle.exists(), is(false));
    assertThrows(MissingResourceException.class, () -> bundle.getString("Foo"));
  }

  @Test
  void getASettingBundleForAXmlBundleShouldFail() {
    XmlSettingBundle bundle = ResourceLocator.getXmlSettingBundle(XML_SETTING_BUNDLE);
    assertThat(bundle.exists(), is(true));

    assertThrows(ClassCastException.class, () -> ResourceLocator.getSettingBundle(XML_SETTING_BUNDLE));
  }

  @Test
  void getALocalizationBundleForAXmlBundleShouldFail() {
    XmlSettingBundle bundle = ResourceLocator.getXmlSettingBundle(XML_SETTING_BUNDLE);
    assertThat(bundle.exists(), is(true));

    assertThrows(ClassCastException.class, () -> ResourceLocator.getLocalizationBundle(XML_SETTING_BUNDLE));
  }

  @Test
  void getAXmlBundleForSettingBundleShouldFail() {
    SettingBundle bundle = ResourceLocator.getSettingBundle(SETTING_BUNDLE);
    assertThat(bundle.exists(), is(true));

    assertThrows(ClassCastException.class, () -> ResourceLocator.getXmlSettingBundle(SETTING_BUNDLE));
  }

  @Test
  void getAXmlBundleForLocalizationBundleShouldFail() {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(LOCALIZATION_BUNDLE);
    assertThat(bundle.exists(), is(true));

    assertThrows(ClassCastException.class, () -> ResourceLocator.getXmlSettingBundle(LOCALIZATION_BUNDLE));
  }

}