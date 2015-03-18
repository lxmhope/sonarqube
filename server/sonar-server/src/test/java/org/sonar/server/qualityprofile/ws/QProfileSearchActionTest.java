/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.qualityprofile.ws;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.i18n.I18n;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Languages;
import org.sonar.server.qualityprofile.QProfile;
import org.sonar.server.qualityprofile.QProfileLookup;
import org.sonar.server.qualityprofile.QProfileService;
import org.sonar.server.rule.RuleService;
import org.sonar.server.ws.WsTester;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QProfileSearchActionTest {

  @Mock
  QProfileLookup profileLookup;

  @Mock
  I18n i18n;

  Language xoo1, xoo2;

  WsTester tester;

  @Before
  public void setUp() throws Exception {
    QProfileService profileService = mock(QProfileService.class);
    RuleService ruleService = mock(RuleService.class);

    xoo1 = createLanguage("xoo1");
    xoo2 = createLanguage("xoo2");

    tester = new WsTester(new QProfilesWs(
      mock(QProfileRestoreBuiltInAction.class),
      new QProfileSearchAction(new Languages(xoo1, xoo2), profileLookup),
      mock(RuleActivationActions.class),
      mock(BulkRuleActivationActions.class)));
  }

  @Test
  public void search_nominal() throws Exception {
    List<QProfile> profiles = Arrays.asList(
      new QProfile().setKey("sonar-way-xoo1-12345").setLanguage(xoo1.getKey()).setName("Sonar way"),
      new QProfile().setKey("sonar-way-xoo2-23456").setLanguage(xoo2.getKey()).setName("Sonar way"),
      new QProfile().setKey("my-sonar-way-xoo2-34567").setLanguage(xoo2.getKey()).setName("My Sonar way").setParent("sonar-way-xoo2-23456")
      );
    when(profileLookup.allProfiles()).thenReturn(profiles);

    tester.newGetRequest("api/qualityprofiles", "search").execute().assertJson(this.getClass(), "search.json");
  }

  @Test
  public void search_for_language() throws Exception {
    List<QProfile> profiles = Arrays.asList(
      new QProfile().setKey("sonar-way-xoo1-12345").setLanguage(xoo1.getKey()).setName("Sonar way")
      );
    when(profileLookup.profiles(xoo1.getKey())).thenReturn(profiles);
    tester.newGetRequest("api/qualityprofiles", "search").setParam("language", xoo1.getKey()).execute().assertJson(this.getClass(), "search_xoo1.json");
  }

  private Language createLanguage(final String key) {
    return new AbstractLanguage(key, StringUtils.capitalize(key)) {
      @Override
      public String[] getFileSuffixes() {
        return new String[] {key};
      }
    };
  }
}
