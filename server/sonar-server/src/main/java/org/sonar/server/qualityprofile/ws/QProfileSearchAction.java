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
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Languages;
import org.sonar.api.server.ws.*;
import org.sonar.api.server.ws.WebService.NewAction;
import org.sonar.api.utils.text.JsonWriter;
import org.sonar.server.qualityprofile.QProfile;
import org.sonar.server.qualityprofile.QProfileLookup;

import java.util.*;

public class QProfileSearchAction implements RequestHandler {

  private static final String FIELD_KEY = "key";
  private static final String FIELD_NAME = "name";
  private static final String FIELD_LANGUAGE = "language";
  private static final String FIELD_IS_INHERITED = "isInherited";
  private static final String FIELD_PARENT_KEY = "parentKey";
  private static final Set<String> ALL_FIELDS = ImmutableSet.of(FIELD_KEY, FIELD_NAME, FIELD_LANGUAGE, FIELD_IS_INHERITED, FIELD_PARENT_KEY);

  private static final String PARAM_LANGUAGE = FIELD_LANGUAGE;
  private static final String PARAM_FIELDS = "f";


  private final Languages languages;

  private final QProfileLookup profileLookup;

  public QProfileSearchAction(Languages languages, QProfileLookup profileLookup) {
    this.languages = languages;
    this.profileLookup = profileLookup;
  }

  @Override
  public void handle(Request request, Response response) throws Exception {
    List<String> fields = request.paramAsStrings(PARAM_FIELDS);
    Preconditions.checkArgument(fields == null || ALL_FIELDS.containsAll(fields));

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
    writeProfiles(json, profiles, fields);
    writeLanguages(json);
    json.endObject().close();
  }

  private void writeProfiles(JsonWriter json, List<QProfile> profiles, List<String> fields) {
    json.name("profiles")
      .beginArray();
    for (QProfile profile : profiles) {
      json.beginObject()
        .prop(FIELD_KEY, nullUnlessNeeded(FIELD_KEY, profile.key(), fields))
        .prop(FIELD_NAME, nullUnlessNeeded(FIELD_NAME, profile.name(), fields))
        .prop(FIELD_LANGUAGE, nullUnlessNeeded(FIELD_LANGUAGE, profile.language(), fields))
        .prop(FIELD_PARENT_KEY, nullUnlessNeeded(FIELD_PARENT_KEY, profile.parent(), fields));
      // Special case for boolean
      if (fieldIsNeeded(FIELD_IS_INHERITED, fields)) {
        json.prop(FIELD_IS_INHERITED, profile.isInherited());
      }
      json.endObject();
    }
    json.endArray();
  }

  private <T> T nullUnlessNeeded(String field, T value, List<String> fields) {
    return fieldIsNeeded(field, fields) ? value : null;
  }

  private boolean fieldIsNeeded(String field, List<String> fields) {
    return fields == null || fields.contains(field);
  }

  private void writeLanguages(JsonWriter json) {
    json.name("languages")
      .beginArray();
    for (Language language : languages.all()) {
      json.beginObject()
        .prop(FIELD_KEY, language.getKey())
        .prop(FIELD_NAME, language.getName())
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

    search.createParam(PARAM_FIELDS)
      .setDescription("Use to restrict returned fields")
      .setExampleValue("key,language")
      .setPossibleValues(ALL_FIELDS);
  }

}
