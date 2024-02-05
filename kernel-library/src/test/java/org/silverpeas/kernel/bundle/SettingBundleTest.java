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


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.silverpeas.kernel.util.SystemWrapper;

import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SettingBundleTest {

  private static final String SETTING_BUNDLE = "org.silverpeas.test.settings.usersCSVFormat";

  private static SettingBundle settingBundle;

  @BeforeAll
  public static void initSystemProperties() {
    SystemWrapper.getInstance().setProperty("STAMP", "234558373");
  }

  @BeforeAll
  public static void loadSettingBundle() {
    settingBundle = ResourceLocator.getSettingBundle(SETTING_BUNDLE);
    assertThat(settingBundle, notNullValue());
    assertThat(settingBundle.exists(), is(true));
    assertThat(settingBundle.getBaseBundleName(), is(SETTING_BUNDLE));
  }

  @Test
  void getMissingPropertyShouldThrowMissingResourceException() {
    assertThat(settingBundle.containsKey("Foo"), is(false));

    Assertions.assertThrows(MissingResourceException.class, () -> settingBundle.getString("Foo"));
    Assertions.assertThrows(MissingResourceException.class, () -> settingBundle.getFloat("Foo"));
    Assertions.assertThrows(MissingResourceException.class, () -> settingBundle.getBoolean("Foo"));
    Assertions.assertThrows(MissingResourceException.class, () -> settingBundle.getInteger("Foo"));
    Assertions.assertThrows(MissingResourceException.class, () -> settingBundle.getLong("Foo"));
    Assertions.assertThrows(MissingResourceException.class, () -> settingBundle.getList("Foo"));
  }

  @Test
  void getMissingListPropertyShouldReturnAnEmptyList() {
    assertThat(settingBundle.containsKey("Foo_1.Foo"), is(false));
    List<String> values = settingBundle.getStringList("Foo", "Foo");
    assertThat(values, empty());

    assertThat(settingBundle.containsKey("User_1.Foo"), is(false));
    List<String> otherValues = settingBundle.getStringList("User", "Foo");
    assertThat(otherValues, empty());
  }

  @Test
  void getStringListOnNameSuffixShouldReturn6Values() {
    List<String> values = settingBundle.getStringList("User", "Name");
    assertThat(values, contains("Nom", "Prenom", "Login", "Email", "", "MotDePasse"));
  }

  @Test
  void getStringListOnNameSuffixShouldReturn0ValueOnNoMaxAnd6ValuesWhenMaxIsSpecified() {
    PropertyValuesList values = settingBundle.getStringList("User", "Unknown");
    assertThat(values, empty());
    values = settingBundle.getStringList("User", "Unknown", 6);
    assertThat(values, contains("", "", "", "", "", ""));
  }

  @Test
  void getStringListOnNameSuffixShouldReturn3ValuesAsRequested() {
    PropertyValuesList values = settingBundle.getStringList("User", "Name", 3);
    assertThat(values, contains("Nom", "Prenom", "Login"));
  }

  @Test
  void getStringListOnNameSuffixShouldReturnEmptyArray() {
    PropertyValuesList values = settingBundle.getStringList("User_", "Name");
    assertThat(values, empty());
  }

  @Test
  void getStringListOnNameSuffixShouldAlsoReturnEmptyArray() {
    PropertyValuesList values = settingBundle.getStringList("User", "Unknown");
    assertThat(values, empty());
  }

  @Test
  void getStringListOnNameSuffixShouldAgainReturnEmptyArray() {
    PropertyValuesList values = settingBundle.getStringList("User", ".Name");
    assertThat(values, empty());
  }

  @Test
  void getString() {
    String value = settingBundle.getString("User_1.Name");
    assertThat(value, is("Nom"));
  }

  @Test
  void getStringWithDefaultValue() {
    String value = settingBundle.getString("User_1", "Foo");
    assertThat(value, is("Foo"));
  }

  @Test
  void getStringListWithDefaultSeparator() {
    String[] values = settingBundle.getList("Format");
    assertThat(values.length, is(5));
    assertThat(values[0], is("Nom"));
    assertThat(values[1], is("Prenom"));
    assertThat(values[2], is("Login"));
    assertThat(values[3], is("Email"));
    assertThat(values[4], is("MotDePasse"));
  }

  @Test
  void getStringListWithSpecificSeparator() {
    String[] values = settingBundle.getList("Types", " ");
    assertThat(values.length, is(8));
    assertThat(values[0], is("STRING"));
    assertThat(values[1], is("INT"));
    assertThat(values[2], is("BOOLEAN"));
    assertThat(values[3], is("FLOAT"));
    assertThat(values[4], is("DATEFR"));
    assertThat(values[5], is("DATEUS"));
    assertThat(values[6], is("SARRAY"));
    assertThat(values[7], is("LONG"));
  }

  @Test
  void getStringListWithDefaultSeparatorAndWithDefaultValues() {
    String[] values = settingBundle.getList("PROUT",
        new String[]{"STRING", "INTEGER", "DOUBLE", "DATE", "BOOLEAN"});
    assertThat(values.length, is(5));
    assertThat(values[0], is("STRING"));
    assertThat(values[1], is("INTEGER"));
    assertThat(values[2], is("DOUBLE"));
    assertThat(values[3], is("DATE"));
    assertThat(values[4], is("BOOLEAN"));
  }

  @Test
  void getBoolean() {
    boolean value = settingBundle.getBoolean("Enabled");
    assertThat(value, is(true));
  }

  @Test
  void getBooleanWithDefaultValue() {
    boolean value = settingBundle.getBoolean("Disabled", false);
    assertThat(value, is(false));
  }

  @Test
  void getLong() {
    long value = settingBundle.getLong("Count");
    assertThat(value, is(8L));
  }

  @Test
  void getLongWithDefaultValue() {
    long value = settingBundle.getLong("Foo", 1000);
    assertThat(value, is(1000L));
  }

  @Test
  void getFloat() {
    float value = settingBundle.getFloat("Version");
    assertThat(value, is(3.21f));
  }

  @Test
  void getFloatWithDefaultValue() {
    float value = settingBundle.getFloat("Foo", 42.0f);
    assertThat(value, is(42.0f));
  }

  @Test
  void getInteger() {
    int value = settingBundle.getInteger("Stamp");
    assertThat(value, is(234558373));
  }

  @Test
  void getIntegerWithDefaultValue() {
    int value = settingBundle.getInteger("Foo", 42);
    assertThat(value, is(42));
  }

  @Test
  void getAsResourceBundle() {
    ResourceBundle bundle = settingBundle.asResourceBundle();
    assertThat(bundle, notNullValue());
    Set<String> keys = bundle.keySet();
    Set<String> expectedKeys = settingBundle.keySet();
    assertThat(keys, is(expectedKeys));
  }
}