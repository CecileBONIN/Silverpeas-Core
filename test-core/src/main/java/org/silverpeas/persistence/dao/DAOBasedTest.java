/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.persistence.dao;

import org.silverpeas.DataSetTest;

import java.sql.Connection;

/**
 * Abstract class for tests that are based on the behavior of a JPA repository. These tests are not
 * about the repository itself but on the persistence characteristics of a business object using a
 * JPA repository.
 */
public abstract class DAOBasedTest extends DataSetTest {

  @Override
  protected String getDataSourceInjectionBeanId() {
    return "dataSource";
  }

  /**
   * Calling this method to perform a DAO test without handling the connection opening and closing.
   * @param daoTest
   * @throws Exception
   */
  protected void performDAOTest(DAOTest daoTest) throws Exception {
    Connection connexion = getConnection();
    try {
      daoTest.test(connexion);
    } finally {
      connexion.close();
    }
  }

  /**
   * Interface that must be implemented to perform a DAO test.
   */
  protected interface DAOTest {

    /**
     * The method containing the test.
     * @param connection the connection for the current test.
     */
    void test(Connection connection) throws Exception;
  }
}
