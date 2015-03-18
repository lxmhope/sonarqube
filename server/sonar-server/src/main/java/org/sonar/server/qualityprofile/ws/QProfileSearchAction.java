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

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Languages;
import org.sonar.api.server.ws.*;
import org.sonar.api.server.ws.WebService.NewAction;
import org.sonar.api.utils.text.JsonWriter;
import org.sonar.server.qualityprofile.QProfile;
import org.sonar.server.qualityprofile.QProfileLookup;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class QProfileSearchAction implements RequestHandler {

  private static final String PARAM_LANGUAGE = "language";

  private final Languages languages;

  private final QProfileLookup profileLookup;

  public QProfileSearchAction(Languages languages, QProfileLookup profileLookup) {
    this.languages = languages;
    this.profileLookup = profileLookup;
  }

  @Override
  public void handle(Request request, Response response) throws Exception {
    String language = request.param(PARAM_LANGUAGE);

    List<QProfile> profiles = null;
    if (language == null) {
      profiles = profileLookup.allProfiles();
    } else {
      profiles = profileLookup.profiles(language);
    }

    Collections.sort(profiles, new Comparator<QProfile>() {
      @Override
      public int compare(QProfile o1, QProfile o2) {
        int compare = o1.language().compareTo(o2.language());
        if (compare == 0) {
          compare = o1.name().compareTo(o2.name());
        }
        return compare;
      }
    });

    JsonWriter json = response.newJsonWriter().beginObject();
    writeProfiles(json, profiles);
    writeLanguages(json);
    json.endObject().close();
  }

  private void writeProfiles(JsonWriter json, List<QProfile> profiles) {
    json.name("profiles")
      .beginArray();
    for (QProfile profile : profiles) {
      json.beginObject()
        .prop("key", profile.key())
        .prop("name", profile.name())
        .prop("language", profile.language())
        .prop("isInherited", profile.isInherited())
        .prop("parentKey", profile.parent())
        .endObject();
    }
    json.endArray();
  }

  private void writeLanguages(JsonWriter json) {
    json.name("languages")
      .beginArray();
    for (Language language : languages.all()) {
      json.beginObject()
        .prop("key", language.getKey())
        .prop("name", language.getName())
        .endObject();
    }
    json.endArray();
  }

  void define(WebService.NewController controller) {
    NewAction search = controller.createAction("search")
      .setSince("5.2")
      .setDescription("")
      .setHandler(this)
      .setResponseExample(getClass().getResource("search-example.json"));

    search.createParam(PARAM_LANGUAGE)
      .setDescription("The key of a language supported by the platform. If specified, only profiles for the given language are returned")
      .setExampleValue("js")
      .setPossibleValues(Collections2.transform(Arrays.asList(languages.all()), new Function<Language, String>() {
        @Override
        public String apply(Language input) {
          return input.getKey();
        }
      }));
  }

}
