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

import org.sonar.api.server.ws.WebService;

public class QProfilesWs implements WebService {

  public static final String API_ENDPOINT = "api/qualityprofiles";

  private final QProfileRestoreBuiltInAction resetAction;
  private final QProfileSearchAction searchAction;
  private final RuleActivationActions ruleActivationActions;
  private final BulkRuleActivationActions bulkRuleActivationActions;


  public QProfilesWs(QProfileRestoreBuiltInAction resetAction,
    QProfileSearchAction searchAction,
                     RuleActivationActions ruleActivationActions,
                     BulkRuleActivationActions bulkRuleActivationActions) {
    this.resetAction = resetAction;
    this.searchAction = searchAction;
    this.ruleActivationActions = ruleActivationActions;
    this.bulkRuleActivationActions = bulkRuleActivationActions;
  }

  @Override
  public void define(Context context) {
    NewController controller = context.createController(API_ENDPOINT)
      .setDescription("Quality Profiles")
      .setSince("4.4");

    resetAction.define(controller);
    searchAction.define(controller);
    ruleActivationActions.define(controller);
    bulkRuleActivationActions.define(controller);

    controller.done();
  }
}
