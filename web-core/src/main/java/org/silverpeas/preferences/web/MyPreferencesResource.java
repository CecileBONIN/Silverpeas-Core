package org.silverpeas.preferences.web;

import com.silverpeas.SilverpeasServiceProvider;
import com.silverpeas.annotation.Authenticated;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.web.RESTWebService;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * The preferences of the current user in Silverpeas. This web service provides a way to get and
 * to change its preferences. Currently, only the change of the language is implemented.
 * @author mmoquillon
 */
@Service
@RequestScoped
@Path("mypreferences")
@Authenticated
public class MyPreferencesResource extends RESTWebService {
  @Override
  public String getComponentId() {
    return null;
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public UserPreferencesEntity setMyPreferences(final UserPreferencesEntity preferences) {
    UserPreferences userPref = getUserPreferences();
    userPref.setLanguage(preferences.getLanguage());
    SilverpeasServiceProvider.getPersonalizationService().saveUserSettings(userPref);
    preferences.setURI(getUriInfo().getRequestUri());
    return preferences;
  }
}
