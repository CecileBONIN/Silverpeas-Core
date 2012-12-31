/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util.publication.ejb;

import java.sql.Connection;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;

import com.silverpeas.components.model.AbstractTestDao;

import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Calendar.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
public class LastPublicationDAOTest extends AbstractTestDao {

  public LastPublicationDAOTest() {
  }

  @Override
  protected String getDatasetFileName() {
    return "test-last-publication-dao-dataset.xml";
  }

  @Test
  public void testSelectPksByStatus() throws Exception {
    Connection con = getConnection().getConnection();
    List<String> componentIds = newArrayList("kmelia100", "kmelia200");
    Calendar calend = Calendar.getInstance();
    calend.set(YEAR, 2009);
    calend.set(MONTH, NOVEMBER);
    calend.set(DAY_OF_MONTH, 15);
    calend.set(HOUR_OF_DAY, 10);
    calend.set(MINUTE, 15);
    calend.set(SECOND, 0);
    calend.set(MILLISECOND, 0);
    Collection<PublicationPK> keys = PublicationDAO.selectPKsByStatus(con, componentIds,
        PublicationDetail.VALID);
    assertNotNull(keys);
    assertEquals(4, keys.size());
    assertThat(keys, IsIterableContainingInOrder.contains(new PublicationPK("200", "kmelia200"),
        new PublicationPK("101", "kmelia100"), new PublicationPK("100", "kmelia100"),
        new PublicationPK(
        "202", "kmelia200")));
    hashCode();
  }

  @Test
  public void testSelectLastPublications() throws Exception {
    Connection con = getConnection().getConnection();
    List<String> componentIds = newArrayList("kmelia100", "kmelia200");
    Calendar calend = Calendar.getInstance();
    calend.set(YEAR, 2009);
    calend.set(MONTH, NOVEMBER);
    calend.set(DAY_OF_MONTH, 15);
    calend.set(HOUR_OF_DAY, 10);
    calend.set(MINUTE, 15);
    calend.set(SECOND, 0);
    calend.set(MILLISECOND, 0);
    Collection<PublicationPK> keys = PublicationDAO.selectUpdatedPublicationsSince(con,
        componentIds, PublicationDetail.VALID, calend.getTime(), 0);
    assertThat(keys, is(not(nullValue())));
    assertThat(keys.size(), is(3));
    assertThat(keys, IsIterableContainingInOrder.contains(new PublicationPK("200", "kmelia200"),
        new PublicationPK("101", "kmelia100"), new PublicationPK("100", "kmelia100")));
    keys = PublicationDAO.selectUpdatedPublicationsSince(con, componentIds, PublicationDetail.VALID,
        calend.getTime(), 1);
    assertThat(keys, is(not(nullValue())));
    assertThat(keys.size(), is(2));
    assertThat(keys, IsIterableContainingInOrder.contains(new PublicationPK("200", "kmelia200"),
        new PublicationPK("101", "kmelia100")));
  }
}
