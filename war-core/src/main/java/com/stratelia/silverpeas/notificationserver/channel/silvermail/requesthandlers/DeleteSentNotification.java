/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.notificationserver.channel.silvermail.requesthandlers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.model.SentNotificationDetail;
import com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILException;
import com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILRequestHandler;
import com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILSessionController;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * Class declaration
 * @author
 * @version %I%, %G%
 */
public class DeleteSentNotification implements SILVERMAILRequestHandler {

  /**
   * Method declaration
   * @param componentSC
   * @param request
   * @return
   * @throws NotificationManagerException 
   * @throws SILVERMAILException
   * @see
   */
  public String handleRequest(ComponentSessionController componentSC,
      HttpServletRequest request) throws SILVERMAILException {
    try {
      SILVERMAILSessionController silvermailScc = (SILVERMAILSessionController) componentSC;
      String notifId = (String) request.getParameter("NotifId");
      silvermailScc.deleteSentNotif(notifId);
      List<SentNotificationDetail> sentNotifs = silvermailScc.getUserMessageList();
      request.setAttribute("SentNotifs", sentNotifs);
    } catch(NotificationManagerException e) {
      throw new SILVERMAILException(
          "DeleteSentNotification.handleRequest()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return "/SILVERMAIL/jsp/sentUserNotifications.jsp";
  }

}
