/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/
package com.stratelia.silverpeas.notificationserver.channel.silvermail;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.model.SentNotificationDetail;
import com.stratelia.silverpeas.notificationManager.model.SentNotificationInterface;
import com.stratelia.silverpeas.notificationManager.model.SentNotificationInterfaceImpl;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;

/**
 * Class declaration
 *
 * @author
 * @version %I%, %G%
 */
public class SILVERMAILSessionController extends AbstractComponentSessionController {

  protected String currentFunction;
  protected long currentMessageId = -1;

  /**
   * Constructor declaration
   *
   * @see
   */
  public SILVERMAILSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(
        mainSessionCtrl,
        context,
        "com.stratelia.silverpeas.notificationserver.channel.silvermail.multilang.silvermail",
        "com.stratelia.silverpeas.notificationserver.channel.silvermail.settings.silvermailIcons");
    setComponentRootName(URLManager.CMP_SILVERMAIL);
  }

  protected String getComponentInstName() {
    return URLManager.CMP_SILVERMAIL;
  }

  /**
   * Method declaration
   *
   * @param currentFunction
   * @see
   */
  public void setCurrentFunction(String currentFunction) {
    this.currentFunction = currentFunction;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public String getCurrentFunction() {
    return currentFunction;
  }

  /**
   * Method declaration
   *
   * @param folderName
   * @return
   * @see
   */
  public Collection<SILVERMAILMessage> getFolderMessageList(String folderName)
      throws SILVERMAILException {
    Collection<SILVERMAILMessage> messages = SILVERMAILPersistence.getMessageOfFolder(Integer
        .parseInt(getUserId()), folderName);
    return messages;
  }

  /**
   * Method declaration
   *
   * @param userId
   * @return
   * @throws NotificationManagerException
   * @see
   */
  public List<SentNotificationDetail> getUserMessageList()
      throws NotificationManagerException {
    String userId = getUserId();
    List<SentNotificationDetail> notifByUser;
    List<SentNotificationDetail> sentNotifByUser = new ArrayList<SentNotificationDetail>();
    try {
      notifByUser = getNotificationInterface().getAllNotifByUser(userId);
      for (SentNotificationDetail sentNotif : notifByUser) {
        sentNotif.setSource(getSource(sentNotif.getComponentId()));
        sentNotifByUser.add(sentNotif);
      }
    } catch (NotificationManagerException e) {
      throw new NotificationManagerException(
          "SILVERMAILSessionController.getUserMessageList()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return sentNotifByUser;
  }

  /**
   * Method declaration
   *
   * @param userId
   * @return
   * @throws NotificationManagerException
   * @see
   */
  public SentNotificationDetail getSentNotification(String notifId)
      throws NotificationManagerException {
    SentNotificationDetail sentNotification = null;
    try {
      sentNotification = getNotificationInterface().getNotification(Integer.parseInt(notifId));
      sentNotification.setSource(getSource(sentNotification.getComponentId()));
    } catch (NotificationManagerException e) {
      throw new NotificationManagerException(
          "SILVERMAILSessionController.getSentNotification()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return sentNotification;
  }

  private String getSource(String componentId) {
    ResourceLocator m_Multilang = new ResourceLocator(
        "org.silverpeas.notificationserver.channel.silvermail.multilang.silvermail",
        getLanguage());
    String source = m_Multilang.getString("UserNotification");
    if (StringUtil.isDefined(componentId)) {
      OrganisationController orga = OrganisationControllerFactory.getOrganisationController();
      ComponentInst instance = orga.getComponentInst(componentId);

      // Sometimes, source could not be found
      SpaceInst space = orga.getSpaceInstById(instance.getDomainFatherId());
      if (space != null) {
        source = space.getName() + " - " + instance.getLabel();
      } else {
        source = m_Multilang.getString("UnknownSource");
      }
    }

    return source;
  }

  /**
   * Delete the sent message notification
   *
   * @param notifId
   * @throws NotificationManagerException 
   */
  public void deleteSentNotif(String notifId) throws NotificationManagerException {
    try {
      getNotificationInterface().deleteNotif(Integer.parseInt(notifId), getUserId()); 
    } catch (NotificationManagerException e) {
      throw new NotificationManagerException(
          "SILVERMAILSessionController.deleteSentNotif()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void deleteAllSentNotif() throws NotificationManagerException {
    try {
      getNotificationInterface().deleteNotifByUser(getUserId());
    } catch (NotificationManagerException e) {
      throw new NotificationManagerException(
          "SILVERMAILSessionController.deleteAllSentNotif()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  private SentNotificationInterface getNotificationInterface()
      throws NotificationManagerException {
    SentNotificationInterface notificationInterface = null;
    try {
      notificationInterface = new SentNotificationInterfaceImpl();
    } catch (Exception e) {
      throw new NotificationManagerException(
          "SILVERMAILSessionController.getNotificationInterface()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return notificationInterface;
  }

  /**
   * Method declaration
   *
   * @param messageId
   * @return
   * @see
   */
  public SILVERMAILMessage getMessage(long messageId)
      throws SILVERMAILException {
    SILVERMAILMessage msg = SILVERMAILPersistence.getMessage(messageId);
    return msg;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public long getCurrentMessageId() {
    return currentMessageId;
  }

  /**
   * Method declaration
   *
   * @param value
   * @see
   */
  public void setCurrentMessageId(long value) {
    currentMessageId = value;
  }

  public SILVERMAILMessage getCurrentMessage() throws SILVERMAILException {
    return getMessage(currentMessageId);
  }
  
  /**
   * Delete the message notification
   *
   * @param notifId
   * @throws SILVERMAILException 
   */
  public void deleteMessage(String notifId) throws SILVERMAILException {
    try {
      long notificationId = Long.parseLong(notifId);
      SILVERMAILPersistence.deleteMessage(notificationId, getUserId()); 
    } catch (SILVERMAILException e) {
      throw new SILVERMAILException(
          "SILVERMAILSessionController.deleteMessage()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }
}
