/*
 * Copyright (C) 2009-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.it.issue2.suite;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.selenium.Selenese;
import org.junit.ClassRule;
import org.sonar.wsclient.base.HttpException;
import org.sonar.wsclient.issue.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class AbstractIssueTestCase2 {

  @ClassRule
  public static Orchestrator orchestrator = Issue2TestSuite.ORCHESTRATOR;

  protected Issue searchIssueByKey(String issueKey) {
    List<Issue> issues = search(IssueQuery.create().issues(issueKey)).list();
    assertThat(issues).hasSize(1);
    return issues.get(0);
  }

  protected List<Issue> searchIssuesByComponent(String componentKey) {
    return search(IssueQuery.create().componentRoots(componentKey)).list();
  }

  protected List<Issue> searchUnresolvedIssuesByComponent(String componentKey) {
    return search(IssueQuery.create().componentRoots(componentKey).resolved(false)).list();
  }

  protected static Issue searchRandomIssue() {
    List<Issue> issues = search(IssueQuery.create()).list();
    assertThat(issues).isNotEmpty();
    return issues.get(0);
  }

  protected static Issues search(IssueQuery issueQuery){
    return issueClient().find(issueQuery);
  }

  protected static IssueClient issueClient() {
    return orchestrator.getServer().wsClient().issueClient();
  }

  protected static IssueClient adminIssueClient() {
    return orchestrator.getServer().adminWsClient().issueClient();
  }

  protected static ActionPlanClient adminActionPlanClient() {
    return orchestrator.getServer().adminWsClient().actionPlanClient();
  }

  protected static void createManualRule(){
    orchestrator.executeSelenese(Selenese.builder().setHtmlTestsInClasspath("create-manual-rule",
      "/selenium/issue/manual-issue/create-manual-rule.html"
    ).build());
  }

  protected static void deleteManualRules(){
    try {
      Connection connection = orchestrator.getDatabase().openConnection();
      connection.prepareStatement("DELETE FROM rules WHERE rules.plugin_name='manual'").execute();
    } catch (SQLException e) {
      throw new IllegalStateException("Fail to remove manual rules", e);
    }
  }

  protected void verifyHttpException(Exception e, int expectedCode){
    assertThat(e).isInstanceOf(HttpException.class);
    HttpException exception = (HttpException) e;
    assertThat(exception.status()).isEqualTo(expectedCode);
  }

}