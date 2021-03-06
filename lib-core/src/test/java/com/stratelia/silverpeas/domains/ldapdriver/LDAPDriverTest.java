/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.domains.ldapdriver;

import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.silverpeas.ldap.CreateLdapServer;
import org.silverpeas.ldap.OpenDJRule;

import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;

@CreateLdapServer(ldifConfig = "opendj/config/config.ldif", serverHome = "opendj", ldifFile =
"silverpeas-ldap.ldif")
public class LDAPDriverTest {

  @ClassRule
  public static OpenDJRule ldapRule = new OpenDJRule();
  private String connectionId;
  private LDAPDriver driver = new LDAPDriver();

  public LDAPDriverTest() {
  }

  @Before
  public void prepareConnection() throws Exception {
    driver.init(0, "org.silverpeas.domains.domainLDAP", null);
    connectionId = LDAPUtility.openConnection(driver.driverSettings);
  }

  @After
  public void closeConnection() throws AdminException {
    LDAPUtility.closeConnection(connectionId);
  }

  @Test
  public void getAllUsers() throws Exception {
    List<String> userNames = Arrays.asList(new String[]{"Nicolas", "Aaren", "Aarika", "Aaron",
          "Aartjan", "Abagael", "Abagail", "Abahri", "Abbas", "Abbe"});
    UserDetail[] users = driver.getAllUsers();
    assertThat(users, notNullValue());
    assertThat(users, arrayWithSize(10));
    for (UserDetail aUser : users) {
      assertThat(userNames, hasItem(aUser.getFirstName()));
    }
  }

  @Test
  public void getAllGroups() throws Exception {
    Group[] groups = driver.getAllGroups();
    assertThat(groups, notNullValue());
    assertThat(groups, arrayWithSize(3));
    List<String> groupNames = Arrays.asList(new String[]{"Groupe 1", "Groupe 2", "Groupe 3"});
    List<String> groupDescriptions = Arrays.asList(new String[]{"Description du premier groupe",
          "Description du second groupe", "Description du troisème groupe"});
    for (Group group : groups) {
      assertThat(groupNames, hasItem(group.getName()));
      assertThat(groupDescriptions, hasItem(group.getDescription()));
    }
  }

  @Test
  public void getAUser() throws Exception {
    UserDetail user = driver.getUser("user.7");
    assertThat(user, notNullValue());
    assertThat(user.getFirstName(), is("Abahri"));
    assertThat(user.getLastName(), is("Abazari"));
  }
  
  @Test
  public void getAUserFull() throws Exception {
    UserFull user = driver.getUserFull("user.7");
    assertThat(user, notNullValue());
    assertThat(user.getFirstName(), is("Abahri"));
    assertThat(user.getLastName(), is("Abazari"));
    assertThat(user.getValue("city"), is("Hattiesburg"));
  }

  @Test
  public void getAGroup() throws Exception {
    Group group = driver.getGroup("a95b39de-ea91-45cb-9af0-890670075d54");
    assertThat(group, notNullValue());
    assertThat(group.getName(), is("Groupe 1"));
    assertThat(group.getDescription(), is("Description du premier groupe"));
  }
  
  @Test
  public void updateAUserFull() throws Exception {
    String newCity = "Grenoble";
    UserFull user = driver.getUserFull("user.7");
    assertThat(user.getValue("city"), is("Hattiesburg"));
    user.setValue("city", newCity);
    user.setValue("matricule", "123");
    user.setValue("homePhone", "");
    driver.updateUserFull(user);
    
    user = driver.getUserFull("user.7");
    // checks an updatable field is well updated
    assertThat(user.getValue("city"), is(newCity));
    // checks a non-updatable field is not updated
    assertThat(user.getValue("matricule"), is("7"));
    // checks reset is ok
    assertThat(user.getValue("homePhone"), isEmptyOrNullString());
  }
  
}