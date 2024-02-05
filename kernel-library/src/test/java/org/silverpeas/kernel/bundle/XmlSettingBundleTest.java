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
import org.silverpeas.kernel.util.SystemWrapper;

import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class XmlSettingBundleTest {

  private static final String XML_SETTING_BUNDLE = "org.silverpeas.test.settings.dbDriverSettings";

  @BeforeAll
  public static void initSystemProperties() {
    SystemWrapper.getInstance().setProperty("JDBC_URL", "jdbc:postgresql://localhost:5432/SilverpeasV5DLE");
  }

  @Test
  void getAllValuesFromAGivenParameter() {
    XmlSettingBundle bundle = ResourceLocator.getXmlSettingBundle(XML_SETTING_BUNDLE);
    assertThat(bundle, is(notNullValue()));

    String[] values = bundle.getStringArray("DBDriver-configuration.Drivers-Declaration.Drivers");
    assertThat(values.length, is(5));
    assertThat(values[0], is("BD_Silverpeas"));
    assertThat(values[1], is("SQL_Access_Example"));
    assertThat(values[2], is("Oracle_Access_Example"));
    assertThat(values[3], is("BOTA_Access_Example"));
    assertThat(values[4], is("Postgres_Access_Example"));
  }

  @Test
  void getAParameterValueByAbsoluteKey() {
    XmlSettingBundle bundle = ResourceLocator.getXmlSettingBundle(XML_SETTING_BUNDLE);
    assertThat(bundle, is(notNullValue()));

    String value = bundle.getString(
        "DBDriver-configuration.Drivers-Definition.BD_Silverpeas-configuration.DriverName");
    assertThat(value, is("Accès a la base de donnée SILVERPEAS"));

    value = bundle.getString(
        "DBDriver-configuration.Drivers-Definition.BD_Silverpeas-configuration.ClassName");
    assertThat(value, is("com.inet.tds.TdsDriver"));

    value = bundle.getString(
        "DBDriver-configuration.Drivers-Definition.BD_Silverpeas-configuration.Description");
    assertThat(value, is("Paramétrage d'accès à la base de données Silverpeas"));

    value = bundle.getString(
        "DBDriver-configuration.Drivers-Definition.BD_Silverpeas-configuration.JDBCUrl");
    assertThat(value, is("jdbc:inetdae7:serveur?database=base&port=1433"));
  }

  @Test
  void getAParameterValueByRelativeKey() {
    XmlSettingBundle bundle = ResourceLocator.getXmlSettingBundle(XML_SETTING_BUNDLE);
    assertThat(bundle, is(notNullValue()));

    String value = bundle.getString("Drivers-Definition.BD_Silverpeas-configuration.DriverName");
    assertThat(value, is("Accès a la base de donnée SILVERPEAS"));

    value = bundle.getString("Drivers-Definition.BD_Silverpeas-configuration.ClassName");
    assertThat(value, is("com.inet.tds.TdsDriver"));

    value = bundle.getString("Drivers-Definition.BD_Silverpeas-configuration.Description");
    assertThat(value, is("Paramétrage d'accès à la base de données Silverpeas"));

    value = bundle.getString("Drivers-Definition.BD_Silverpeas-configuration.JDBCUrl");
    assertThat(value, is("jdbc:inetdae7:serveur?database=base&port=1433"));
  }

  @Test
  void getAParameterFromABadPath() {
    XmlSettingBundle bundle = ResourceLocator.getXmlSettingBundle(XML_SETTING_BUNDLE);
    assertThat(bundle.exists(), is(true));
    assertThrows(MissingResourceException.class, () -> bundle.getString("BD_Silverpeas-configuration.DriverName"));
  }

  @Test
  void goToASettingSection() {
    XmlSettingBundle bundle = ResourceLocator.getXmlSettingBundle(XML_SETTING_BUNDLE);
    assertThat(bundle, is(notNullValue()));

    XmlSettingBundle.SettingSection section =
        bundle.getSettingSection("Drivers-Definition.BD_Silverpeas-configuration");
    assertThat(section, is(notNullValue()));
    assertThat(section.getName(), is("BD_Silverpeas-configuration"));
    assertThat(section.getString("DriverName"), is("Accès a la base de donnée SILVERPEAS"));
    assertThat(section.getString("ClassName"), is("com.inet.tds.TdsDriver"));
    assertThat(section.getString("Description"),
        is("Paramétrage d'accès à la base de données Silverpeas"));
    assertThat(section.getString("JDBCUrl"), is("jdbc:inetdae7:serveur?database=base&port=1433"));
  }

  @Test
  void goRecursivelyToASettingSection() {
    XmlSettingBundle bundle = ResourceLocator.getXmlSettingBundle(XML_SETTING_BUNDLE);
    assertThat(bundle, is(notNullValue()));

    XmlSettingBundle.SettingSection section = bundle.getSettingSection("Drivers-Definition");
    assertThat(section.getName(), is("Drivers-Definition"));

    section = section.getSettingSection("BD_Silverpeas-configuration");
    assertThat(section.getName(), is("BD_Silverpeas-configuration"));
    assertThat(section.getString("DriverName"), is("Accès a la base de donnée SILVERPEAS"));
    assertThat(section.getString("ClassName"), is("com.inet.tds.TdsDriver"));
    assertThat(section.getString("Description"),
        is("Paramétrage d'accès à la base de données Silverpeas"));
    assertThat(section.getString("JDBCUrl"), is("jdbc:inetdae7:serveur?database=base&port=1433"));
  }

  @Test
  void goToSeveralSettingSections() {
    XmlSettingBundle bundle = ResourceLocator.getXmlSettingBundle(XML_SETTING_BUNDLE);
    assertThat(bundle, is(notNullValue()));

    XmlSettingBundle.SettingSection section =
        bundle.getSettingSection("Drivers-Definition.BD_Silverpeas-configuration");
    assertThat(section, is(notNullValue()));
    assertThat(section.getName(), is("BD_Silverpeas-configuration"));
    assertThat(section.getString("DriverName"), is("Accès a la base de donnée SILVERPEAS"));
    assertThat(section.getString("ClassName"), is("com.inet.tds.TdsDriver"));
    assertThat(section.getString("Description"),
        is("Paramétrage d'accès à la base de données Silverpeas"));
    assertThat(section.getString("JDBCUrl"), is("jdbc:inetdae7:serveur?database=base&port=1433"));

    section = bundle.getSettingSection("Drivers-Definition.Postgres_Access_Example-configuration");
    assertThat(section, is(notNullValue()));
    assertThat(section.getName(), is("Postgres_Access_Example-configuration"));
    assertThat(section.getString("DriverName"), is("Acces a la base de tests Postgres"));
    assertThat(section.getString("ClassName"), is("org.postgresql.Driver"));
    assertThat(section.getString("Description"),
        is("Ceci est un exemple d'acces a une base de donnees Postgres"));
    assertThat(section.getString("JDBCUrl"),
        is("jdbc:postgresql://localhost:5432/SilverpeasV5DLE"));
  }

  @Test
  void goToASettingSectionByAnInvalidPath() {
    XmlSettingBundle bundle = ResourceLocator.getXmlSettingBundle(XML_SETTING_BUNDLE);
    assertThat(bundle, is(notNullValue()));

    assertThrows(MissingResourceException.class, () -> bundle.getSettingSection("BD_Silverpeas-configuration"));
  }

  @Test
  void getAllSettingsSectionAtTheSamePath() {
    XmlSettingBundle bundle = ResourceLocator.getXmlSettingBundle(XML_SETTING_BUNDLE);
    assertThat(bundle, is(notNullValue()));

    List<XmlSettingBundle.SettingSection> sections =
        bundle.getAllSettingSection("DataType-Definition.DataType");
    assertThat(sections.size(), is(12));

    assertThat(sections.get(0).getAttribute("name"), is("BIGINT"));
    assertThat(sections.get(0).getAttribute("sqlType"), is("java.sql.Types.BIGINT"));
    assertThat(sections.get(0).getAttribute("javaType"), is("java.lang.Long"));
    assertThat(sections.get(0).getAttribute("length"), is(nullValue()));

    assertThat(sections.get(1).getAttribute("name"), is("BOOLEAN"));
    assertThat(sections.get(1).getAttribute("sqlType"), is("java.sql.Types.BIT"));
    assertThat(sections.get(1).getAttribute("javaType"), is("java.lang.Boolean"));
    assertThat(sections.get(1).getAttribute("length"), is(nullValue()));

    assertThat(sections.get(2).getAttribute("name"), is("CHAR"));
    assertThat(sections.get(2).getAttribute("sqlType"), is("java.sql.Types.CHAR"));
    assertThat(sections.get(2).getAttribute("javaType"), is("java.lang.String"));
    assertThat(sections.get(2).getAttribute("length"), is("true"));
  }

  @Test
  void getAttributesSetFromAnXmlNode() {
    XmlSettingBundle bundle = ResourceLocator.getXmlSettingBundle(XML_SETTING_BUNDLE);
    assertThat(bundle, is(notNullValue()));
    List<XmlSettingBundle.SettingSection> sections =
        bundle.getAllSettingSection("DataType-Definition.DataType");
    assertThat(sections.isEmpty(), is(false));

    Set<String> attributes = sections.get(0).attributeSet();
    assertThat(attributes, containsInAnyOrder("name", "sqlType", "javaType"));
  }

  @Test
  void getAllSettingsSectionAtAnInvalidPath() {
    XmlSettingBundle bundle = ResourceLocator.getXmlSettingBundle(XML_SETTING_BUNDLE);
    assertThat(bundle, is(notNullValue()));

    assertThrows(MissingResourceException.class, () -> bundle.getAllSettingSection("DataType"));
  }

  @Test
  void getNonExistingProperty() {
    XmlSettingBundle bundle = ResourceLocator.getXmlSettingBundle(XML_SETTING_BUNDLE);
    assertThrows(MissingResourceException.class, () -> bundle.getString("DataType"));
  }
}