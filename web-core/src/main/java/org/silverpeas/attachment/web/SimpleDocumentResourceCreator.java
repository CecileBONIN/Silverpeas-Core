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
package org.silverpeas.attachment.web;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.MetaData;
import com.silverpeas.util.MetadataExtractor;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import org.apache.commons.io.FileUtils;
import org.silverpeas.attachment.ActifyDocumentProcessor;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.HistorisedDocument;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.attachment.model.UnlockContext;
import org.silverpeas.attachment.model.UnlockOption;
import org.silverpeas.importExport.versioning.DocumentVersion;
import org.silverpeas.servlet.RequestParameterDecoder;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;

import static org.silverpeas.web.util.IFrameAjaxTransportUtil.AJAX_IFRAME_TRANSPORT;
import static org.silverpeas.web.util.IFrameAjaxTransportUtil.packObjectToJSonDataWithHtmlContainer;

/**
 *
 * @author ehugonnet
 */
@Service
@RequestScoped
@Path("documents/{componentId}/document/create")
@Authorized
public class SimpleDocumentResourceCreator extends AbstractSimpleDocumentResource {

  /**
   * Create the document identified by the requested URI and from the content and some additional
   * parameters passed within the request.
   *
   * A {@link SimpleDocumentUploadData} is extracted from request parameters.
   *
   * @return an HTTP response embodied an entity in a format expected by the client (that is
   * identified by the <code>xRequestedWith</code> parameter).
   * @throws IOException if an error occurs while updating the document.
   */
  @POST
  @Path("{filename}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response createDocument(final @PathParam("filename") String filename) throws IOException {
    SimpleDocumentUploadData uploadData =
        RequestParameterDecoder.decode(getHttpRequest(), SimpleDocumentUploadData.class);

    // Create the attachment
    SimpleDocumentEntity entity = createSimpleDocument(uploadData, filename);

    if (AJAX_IFRAME_TRANSPORT.equals(uploadData.getXRequestedWith())) {

      // In case of file upload performed by Ajax IFrame transport way,
      // the expected response type is text/html
      // (when FormData API doesn't exist on client side)
      return Response.ok().type(MediaType.TEXT_HTML_TYPE)
          .entity(packObjectToJSonDataWithHtmlContainer(entity)).build();
    } else {

      // Otherwise JSON response type is expected
      return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(entity).build();
    }
  }

  protected SimpleDocumentEntity createSimpleDocument(SimpleDocumentUploadData uploadData,
      String filename) throws IOException {
    try {
      if (uploadData.getRequestFile() == null) {
        return null;
      }
      String uploadedFilename = filename;
      if (StringUtil.isNotDefined(filename)) {
        uploadedFilename = uploadData.getRequestFile().getName();
      }
      InputStream uploadedInputStream = uploadData.getRequestFile().getInputStream();
      if (uploadedInputStream != null && StringUtil.isDefined(uploadedFilename)) {
        File tempFile = File.createTempFile("silverpeas_", uploadedFilename);
        FileUtils.copyInputStreamToFile(uploadedInputStream, tempFile);

        //check the file
        checkUploadedFile(tempFile);

        String lang = I18NHelper.checkLanguage(uploadData.getLanguage());
        String title = uploadData.getTitle();
        String description = uploadData.getDescription();
        if (!StringUtil.isDefined(title)) {
          MetadataExtractor extractor = MetadataExtractor.getInstance();
          MetaData metadata = extractor.extractMetadata(tempFile);
          if (StringUtil.isDefined(metadata.getTitle())) {
            title = metadata.getTitle();
          } else {
            title = "";
          }
          if (!StringUtil.isDefined(description) && StringUtil.isDefined(metadata.getSubject())) {
            description = metadata.getSubject();
          }
        }
        if (!StringUtil.isDefined(description)) {
          description = "";
        }

        DocumentType attachmentContext;
        if (!StringUtil.isDefined(uploadData.getContext())) {
          attachmentContext = DocumentType.attachment;
        } else {
          attachmentContext = DocumentType.valueOf(uploadData.getContext());
        }
        SimpleDocumentPK pk = new SimpleDocumentPK(null, getComponentId());
        String userId = getUserDetail().getId();
        SimpleDocument document;
        boolean needCreation = true;
        boolean publicDocument = true;
        if (uploadData.getVersionType() != null) {
          document = AttachmentServiceFactory.getAttachmentService()
              .findExistingDocument(pk, uploadedFilename,
                  new ForeignPK(uploadData.getForeignId(), getComponentId()), lang);
          publicDocument = uploadData.getVersionType() == DocumentVersion.TYPE_PUBLIC_VERSION;
          needCreation = document == null;
          if (document == null) {
            document = new HistorisedDocument(pk, uploadData.getForeignId(), 0, userId,
                new SimpleAttachment(uploadedFilename, lang, title, description,
                    uploadData.getRequestFile().getSize(), FileUtil.getMimeType(tempFile.getPath()),
                    userId, new Date(), null));
            document.setDocumentType(attachmentContext);
          }
          document.setPublicDocument(publicDocument);
          document.setComment(uploadData.getComment());
        } else {
          document = new SimpleDocument(pk, uploadData.getForeignId(), 0, false, null,
              new SimpleAttachment(uploadedFilename, lang, title, description,
                  uploadData.getRequestFile().getSize(), FileUtil.getMimeType(tempFile.getPath()),
                  userId, new Date(), null));
          document.setDocumentType(attachmentContext);
        }
        document.setLanguage(lang);
        document.setTitle(title);
        document.setDescription(description);
        document.setSize(tempFile.length());
        InputStream content = new BufferedInputStream(new FileInputStream(tempFile));
        if (needCreation) {
          document = AttachmentServiceFactory.getAttachmentService()
              .createAttachment(document, content, uploadData.getIndexIt(), publicDocument);
        } else {
          document.edit(userId);
          AttachmentServiceFactory.getAttachmentService().lock(document.getId(), userId, lang);
          AttachmentServiceFactory.getAttachmentService()
              .updateAttachment(document, content, uploadData.getIndexIt(), true);
          UnlockContext unlockContext = new UnlockContext(document.getId(), userId, lang);
          unlockContext.addOption(UnlockOption.UPLOAD);
          if (!publicDocument) {
            unlockContext.addOption(UnlockOption.PRIVATE_VERSION);
          }
          AttachmentServiceFactory.getAttachmentService().unlock(unlockContext);
        }
        content.close();
        FileUtils.deleteQuietly(tempFile);
        
        // in the case the document is a CAD one, process it for Actify
        ActifyDocumentProcessor.getProcessor().process(document);
        
        URI attachmentUri = getUriInfo().getRequestUriBuilder().path("document").path(document.
            getLanguage()).build();
        return SimpleDocumentEntity.fromAttachment(document).withURI(attachmentUri);
      }
      return null;
    } catch (RuntimeException re) {
      performRuntimeException(re);
    }
    throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
  }
}