/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
<<<<<<< HEAD
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
=======
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
>>>>>>> master
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.attachment.web.OnlineAttachment;

import com.silverpeas.accesscontrol.AttachmentAccessController;
import com.silverpeas.accesscontrol.ComponentAccessController;
import com.silverpeas.accesscontrol.DocumentVersionAccessController;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.RestRequest;

import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.SilverpeasWebUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.DocumentVersionPK;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;

/**
 * Class declaration
 *
 * @author
 */
public class RestOnlineFileServer extends HttpServlet {

  private static final long serialVersionUID = 4039504051749955604L;

  @Override
  public void init(ServletConfig config) {
    try {
      super.init(config);
    } catch (ServletException se) {
      SilverTrace.fatal("peasUtil", "FileServer.init", "peasUtil.CANNOT_ACCESS_SUPERCLASS");
    }
  }

  @Override
  public void service(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    RestRequest restRequest = new RestRequest(req, "");
    SilverTrace.info("peasUtil", "OnlineFileServer.doPost", "root.MSG_GEN_ENTER_METHOD");
    try {
      OnlineFile file = getWantedFile(restRequest);
      if (file != null) {
        display(res, file);
        return;
      }
    } catch (IllegalAccessException ex) {
      res.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    displayWarningHtmlCode(res);
  }

  protected OnlineFile getWantedFile(RestRequest restRequest) throws Exception {
    OnlineFile file = getWantedAttachment(restRequest);
    if (file == null) {
      file = getWantedVersionnedDocument(restRequest);
    }
    return file;
  }

  protected OnlineFile getWantedAttachment(RestRequest restRequest) throws Exception {
    String componentId = restRequest.getElementValue("componentId");
    OnlineFile file = null;
    String attachmentId = restRequest.getElementValue("attachmentId");
    String language = restRequest.getElementValue("lang");
    if (StringUtil.isDefined(attachmentId)) {
      SimpleDocument attachment = AttachmentServiceFactory.getAttachmentService().
          searchAttachmentById(new SimpleDocumentPK(attachmentId), language);
      if (attachment != null) {
        if (isUserAuthorized(restRequest, componentId, attachment)) {
          file = new OnlineAttachment(attachment);
        } else {
          throw new IllegalAccessException("You can't access this file " + attachment.getFilename());
        }
      }
    }
    return file;
  }

  protected OnlineFile getWantedVersionnedDocument(RestRequest restRequest) throws Exception {
    String componentId = restRequest.getElementValue("componentId");
    OnlineFile file = null;
    String documentId = restRequest.getElementValue("documentId");
    if (StringUtil.isDefined(documentId)) {
      String versionId = restRequest.getElementValue("versionId");
      VersioningUtil versioning = new VersioningUtil();
      DocumentVersionPK versionPK = new DocumentVersionPK(Integer.parseInt(versionId), "useless",
          componentId);
      DocumentVersion version = versioning.getDocumentVersion(versionPK);
      if (version != null) {
        if (isUserAuthorized(restRequest, componentId, version)) {
          String[] path = new String[1];
          path[0] = "Versioning";
          file = new OnlineFile(version.getMimeType(), version.getPhysicalName(),
              FileRepositoryManager.getRelativePath(path), componentId);
        } else {
          throw new IllegalAccessException("You can't access this file " + version.getLogicalName());
        }
      }
    }
    return file;
  }

  /**
   * This method writes the result of the preview action.
   *
   * @param res - The HttpServletResponse where the html code is write
   * @param htmlFilePath - the canonical path of the html document generated by the parser tools. if
   * this String is null that an exception had been catched the html document generated is empty !!
   * also, we display a warning html page
   */
  private void display(HttpServletResponse res, OnlineFile onlineFile) throws IOException {
    res.setContentType(onlineFile.getMimeType());
    OutputStream output = res.getOutputStream();
    SilverTrace.info("peasUtil", "OnlineFileServer.display()",
        "root.MSG_GEN_ENTER_METHOD", " htmlFilePath " + onlineFile.getSourceFile());
    try {
      onlineFile.write(output);
    } catch (IOException ioex) {
      SilverTrace.warn("peasUtil", "OnlineFileServer.doPost", "root.EX_CANT_READ_FILE", "file name="
          + onlineFile.getSourceFile(), ioex);
      displayWarningHtmlCode(res);
    } finally {
      IOUtils.closeQuietly(output);
    }
  }

  private void displayWarningHtmlCode(HttpServletResponse res) throws IOException {
    OutputStream output = res.getOutputStream();
    ResourceLocator resourceLocator = new ResourceLocator(
        "com.stratelia.webactiv.util.peasUtil.multiLang.fileServerBundle", "");
    StringReader message = new StringReader(resourceLocator.getString("warning"));
    try {
      IOUtils.copy(message, output);
    } catch (Exception e) {
      SilverTrace.warn("peasUtil", "OnlineFileServer.displayWarningHtmlCode",
          "root.EX_CANT_READ_FILE", "warning properties");
    } finally {
      IOUtils.closeQuietly(output);
      IOUtils.closeQuietly(message);
    }
  }

  private boolean isUserAuthorized(RestRequest request, String componentId, Object object)
      throws Exception {
    SilverpeasWebUtil util = new SilverpeasWebUtil();
    MainSessionController controller = util.getMainSessionController(request.getWebRequest());
    ComponentAccessController componentAccessController = new ComponentAccessController();
    if (controller != null && componentAccessController.isUserAuthorized(controller.getUserId(),
        componentId)) {
      if (componentAccessController.isRightOnTopicsEnabled(controller.getUserId(), componentId)) {
        if (object instanceof DocumentVersion) {
          return isDocumentVersionAuthorized(controller.getUserId(), (DocumentVersion) object);
        } else if (object instanceof AttachmentDetail) {
          return isAttachmentAuthorized(controller.getUserId(), (AttachmentDetail) object);
        }
        return false;
      }
      return true;
    }
    return false;
  }

  private boolean isAttachmentAuthorized(String userId, AttachmentDetail attachment) throws
      Exception {
    AttachmentAccessController accessController = new AttachmentAccessController();
    return accessController.isUserAuthorized(userId, attachment);
  }

  private boolean isDocumentVersionAuthorized(String userId, DocumentVersion version) throws
      Exception {
    DocumentVersionAccessController accessController = new DocumentVersionAccessController();
    return accessController.isUserAuthorized(userId, version);
  }
}