package tv.notube.platform;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import tv.notube.activities.ActivityStore;
import tv.notube.activities.ActivityStoreException;
import tv.notube.applications.ApplicationsManager;
import tv.notube.applications.ApplicationsManagerException;
import tv.notube.commons.model.OAuthToken;
import tv.notube.commons.model.User;
import tv.notube.commons.model.UserProfile;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.auth.OAuthAuth;
import tv.notube.platform.responses.*;
import tv.notube.profiles.Profiles;
import tv.notube.profiles.ProfilesException;
import tv.notube.usermanager.AtomicSignUp;
import tv.notube.usermanager.UserManager;
import tv.notube.usermanager.UserManagerException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Path("rest/user")
@Produces(MediaType.APPLICATION_JSON)
public class UserService extends JsonService {

    private ApplicationsManager applicationsManager;

    private UserManager userManager;

    private ActivityStore activities;

    private Profiles profiles;

    @Inject
    public UserService(
            final ApplicationsManager am,
            final UserManager um,
            final ActivityStore activities,
            final Profiles ps
    ) {
        this.applicationsManager = am;
        this.userManager = um;
        this.activities = activities;
        this.profiles = ps;
    }

    @POST
    @Path("/register")
    public Response signUp(
            @FormParam("name") String name,
            @FormParam("surname") String surname,
            @FormParam("username") String username,
            @FormParam("password") String password,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "signUp",
                    name,
                    surname,
                    username,
                    password,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        try {
            UUID.fromString(apiKey);
        } catch (IllegalArgumentException e) {
            return error(e, "Your apikey is not well formed");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    UUID.fromString(apiKey),
                    ApplicationsManager.Action.CREATE,
                    ApplicationsManager.Object.USER
            );
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authorizing your application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "application with key [" + apiKey + "] is not authorized")
            );
            return rb.build();
        }
        try {
            if (userManager.getUser(username) != null) {
                final String errMsg = "username [" + username + "] is already taken";
                Response.ResponseBuilder rb = Response.serverError();
                rb.entity(new StringPlatformResponse(
                        StringPlatformResponse.Status.NOK,
                        errMsg)
                );
                return rb.build();
            }
        } catch (UserManagerException e) {
            final String errMsg = "Error while calling the UserManager";
            return error(e, errMsg);
        }
        User user = new User();
        user.setName(name);
        user.setSurname(surname);
        user.setUsername(username);
        user.setPassword(password);
        try {
            userManager.storeUser(user);
        } catch (UserManagerException e) {
            final String errMsg = "Error while storing user [" + user + "]";
            return error(e, errMsg);
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new UUIDPlatformResponse(
                UUIDPlatformResponse.Status.OK,
                "user successfully registered",
                user.getId())
        );
        return rb.build();
    }

    @GET
    @Path("/{username}")
    public Response getUser(
            @PathParam("username") String username,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "getUser",
                    username,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        try {
            UUID.fromString(apiKey);
        } catch (IllegalArgumentException e) {
            return error(e, "Your apikey is not well formed");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    UUID.fromString(apiKey),
                    ApplicationsManager.Action.RETRIEVE,
                    ApplicationsManager.Object.USER
            );
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authenticating your application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "Sorry. You're not allowed to do that.")
            );
            return rb.build();
        }
        User user;
        try {
            user = userManager.getUser(username);
        } catch (UserManagerException e) {
            final String errMsg = "Error while getting user [" + username + "]";
            return error(e, errMsg);
        }
        if (user == null) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(
                    new StringPlatformResponse(
                            StringPlatformResponse.Status.NOK,
                            "user [" + username + "] not found"
                    )
            );
            return rb.build();
        }

        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new UserPlatformResponse(
                UserPlatformResponse.Status.OK,
                "user [" + username + "] found",
                user)
        );
        return rb.build();
    }

    @GET
    @Path("/{username}/activities")
    public Response getActivities(
            @PathParam("username") String username,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "getActivities",
                    username,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        try {
            UUID.fromString(apiKey);
        } catch (IllegalArgumentException e) {
            return error(e, "Your apikey is not well formed");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    UUID.fromString(apiKey),
                    ApplicationsManager.Action.RETRIEVE,
                    ApplicationsManager.Object.USER
            );
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authenticating you application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "Sorry. You're not allowed to do that.")
            );
            return rb.build();
        }
        User user;
        try {
            user = userManager.getUser(username);
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user [" + username + "]");
        }
        if (user == null) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(
                    new StringPlatformResponse(
                            StringPlatformResponse.Status.NOK,
                            "user with username [" + username + "] not found"
                    )
            );
            return rb.build();
        }
        // TODO (med) only the activities done today but should be
        // configurable as a query parameter
        DateTime today = org.joda.time.DateTime.now();
        DateTime yesterday = today.minusDays(1);
        Collection<Activity> userActivities;
        try {
            userActivities = activities.getByUserAndDateRange(
                    user.getId(),
                    yesterday,
                    today
            );
        } catch (ActivityStoreException e) {
            return error(
                    e,
                    "Error while getting user [" + username + "] activities"
            );
        }
        Response.ResponseBuilder rb = Response.ok();
        if (userActivities.size() == 0) {
            rb.entity(
                    new ActivitiesPlatformResponse(
                            ActivitiesPlatformResponse.Status.OK,
                            "user '" + username + "' has no activities",
                            userActivities
                    )
            );
        } else {
            rb.entity(
                    new ActivitiesPlatformResponse(
                            ActivitiesPlatformResponse.Status.OK,
                            "user '" + username + "' activities found",
                            userActivities
                    )
            );
        }
        return rb.build();
    }

    @DELETE
    @Path("/{username}")
    public Response deleteUser(
            @PathParam("username") String username,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "deleteUser",
                    username,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        try {
            UUID.fromString(apiKey);
        } catch (IllegalArgumentException e) {
            return error(e, "Your apikey is not well formed");
        }
        User user;
        try {
            user = userManager.getUser(username);
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user [" + username + "]");
        }
        if (user == null) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(
                    new StringPlatformResponse(
                            StringPlatformResponse.Status.NOK,
                            "user with username [" + username + "] not found")
            );
            return rb.build();
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    UUID.fromString(apiKey),
                    ApplicationsManager.Action.DELETE,
                    ApplicationsManager.Object.USER
            );
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authorizing your application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "Sorry, you're not allowed to do that")
            );
            return rb.build();
        }
        try {
            userManager.deleteUser(user.getUsername());
        } catch (UserManagerException e) {
            throw new RuntimeException(
                    "Error while deleting user [" + username + "]",
                    e
            );
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new StringPlatformResponse(
                StringPlatformResponse.Status.OK,
                "user with username [" + username + "] deleted")
        );
        return rb.build();
    }

    @GET
    @Path("/{username}/{service}/check")
    public Response checkToken(
            @PathParam("username") String username,
            @PathParam("service") String service,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "checkToken",
                    username,
                    service,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        try {
            UUID.fromString(apiKey);
        } catch (IllegalArgumentException e) {
            return error(e, "Your apikey is not well formed");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    UUID.fromString(apiKey),
                    ApplicationsManager.Action.RETRIEVE,
                    ApplicationsManager.Object.USER
            );
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authenticating your application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "Sorry, you're not allowed to do that")
            );
            return rb.build();
        }
        User user;
        try {
            user = userManager.getUser(username);
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user [" + username + "]");
        }
        if (user == null) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "user with username [" + username + "] not found")
            );
            return rb.build();
        }
        OAuthAuth auth = (OAuthAuth) user.getAuth(service);
        if(auth == null) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "user with username [" + username + "] has not a token for service [" + service + "]")
            );
            return rb.build();
        }
        if (auth.isExpired()) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "[" + service + "] token for [" + username + "] has expired")
            );
            return rb.build();
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new StringPlatformResponse(
                StringPlatformResponse.Status.OK,
                "[" + service + "] token for [" + username + "] is valid")
        );
        return rb.build();
    }

    @POST
    @Path("/{username}/authenticate")
    public Response authenticate(
            @PathParam("username") String username,
            @FormParam("password") String password,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "authenticate",
                    username,
                    password,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        try {
            UUID.fromString(apiKey);
        } catch (IllegalArgumentException e) {
            return error(e, "Your apikey is not well formed");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    UUID.fromString(apiKey),
                    ApplicationsManager.Action.RETRIEVE,
                    ApplicationsManager.Object.USER
            );
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authenticating your application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "Sorry, you're not allowed to do that")
            );
            return rb.build();
        }
        User user;
        try {
            user = userManager.getUser(username);
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user [" + username + "]");
        }
        if (user == null) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "user with username [" + username + "] not found")
            );
            return rb.build();
        }
        if (!user.getPassword().equals(password)) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "password for [" + username + "] incorrect")
            );
            return rb.build();
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new StringPlatformResponse(
                StringPlatformResponse.Status.OK,
                "user [" + username + "] authenticated")
        );
        return rb.build();
    }

    @GET
    @Path("/register/{service}")
    public Response signUpWithService(
            @PathParam("service") String service
    ) {
        // get a token for an anonymous user
        OAuthToken oAuthToken;
        try {
            oAuthToken = userManager.getOAuthToken(service);
        } catch (UserManagerException e) {
            return error(
                    e,
                    "Error while getting token from [" + service + "]"
            );
        }
        // token asked, let's redirect
        URL redirect = oAuthToken.getRedirectPage();
        try {
            return Response.temporaryRedirect(redirect.toURI()).build();
        } catch (URISyntaxException e) {
            return error(e, "Malformed redirect URL");
        }
    }

    @GET
    @Path("/oauth/token/{service}/{username}")
    public Response getOAuthToken(
            @PathParam("service") String service,
            @PathParam("username") String username,
            @QueryParam("redirect") String finalRedirect
    ) {
        User userObj;
        try {
            userObj = userManager.getUser(username);
            if (userObj == null) {
                Response.ResponseBuilder rb = Response.serverError();
                rb.entity(new StringPlatformResponse(
                        StringPlatformResponse.Status.NOK,
                        "user with username [" + username + "] not found")
                );
                return rb.build();
            }
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user [" + username + "]");
        }
        OAuthToken oAuthToken;
        try {
            oAuthToken = userManager.getOAuthToken(service, userObj.getUsername());
        } catch (UserManagerException e) {
            return error(
                    e,
                    "Error while getting token for user [" + username + "] on service [" + service + "]"
            );
        }
        URL finalRedirectUrl;
        try {
            finalRedirectUrl = new URL(finalRedirect);
        } catch (MalformedURLException e) {
            return error(
                    e,
                    "Final redirect URL [" + finalRedirect + "] is ill-formed"
            );
        }
        try {
            userManager.setUserFinalRedirect(userObj.getUsername(), finalRedirectUrl);
        } catch (UserManagerException e) {
            return error(
                    e,
                    "Error while setting temporary final redirect URL for user '" + username + "' " + "on service '" + service + "'"
            );
        }
        URL redirect = oAuthToken.getRedirectPage();
        try {
            return Response.temporaryRedirect(redirect.toURI()).build();
        } catch (URISyntaxException e) {
            return error(e, "Malformed redirect URL");
        }
    }

    @GET
    @Path("/oauth/atomic/callback/{service}/")
    public Response handleAtomicFacebookAuthCallback(
            @PathParam("service") String service,
            @QueryParam("code") String verifier
    ) {
        AtomicSignUp signUp;
        try {
            signUp = userManager.storeUserFromOAuth(service, verifier);
        } catch (UserManagerException e) {
            return error(e, "Error while OAuth exchange for service: [" + service + "]");
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new AtomicSignUpResponse(
                        PlatformResponse.Status.OK,
                        "user with user name [" + signUp.getUsername() + "] logged in with service [" + signUp.getService() + "]",
                        signUp
                )
        );
        return rb.build();
    }

    @GET
    @Path("/oauth/callback/facebook/{username}/")
    public Response handleFacebookAuthCallback(
            @PathParam("username") String username,
            @QueryParam("code") String verifier
    ) {
        // Facebook OAuth exchange quite different from Twitter's one.
        return handleOAuthCallback("facebook", username, null, verifier);
    }


    @GET
    @Path("/oauth/callback/{service}/{username}/")
    public Response handleOAuthCallback(
            @PathParam("service") String service,
            @PathParam("username") String username,
            @QueryParam("oauth_token") String token,
            @QueryParam("oauth_verifier") String verifier
    ) {
        User userObj;
        try {
            userObj = userManager.getUser(username);
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user '" + username + "'");
        }
        try {
            userManager.registerOAuthService(service, userObj, token, verifier);
        } catch (UserManagerException e) {
            return error(e, "Error while OAuth-like exchange for service: '" + service + "'");
        }
        URL finalRedirectUrl;
        try {
            finalRedirectUrl = userManager.consumeUserFinalRedirect(userObj.getUsername());
        } catch (UserManagerException e) {
            return error(e, "Error while getting final redirect URL for user '" + username + "' for service '" + service + "'");
        }
        try {
            return Response.temporaryRedirect(finalRedirectUrl.toURI()).build();
        } catch (URISyntaxException e) {
            return error(e, "Malformed redirect URL");
        }
    }

    @GET
    @Path("/auth/callback/{service}/{username}/{redirect}")
    public Response handleAuthCallback(
            @PathParam("service") String service,
            @PathParam("username") String username,
            @PathParam("redirect") String redirect,
            @QueryParam("token") String token
    ) {
        User userObj;
        try {
            userObj = userManager.getUser(username);
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user [" + username + "]");
        }
        try {
            userManager.registerService(service, userObj, token);
        } catch (UserManagerException e) {
            return error(
                    e,
                    "Error while OAuth-like exchange for service: [" + service + "]"
            );
        }
        URL finalRedirectUrl;
        try {
            finalRedirectUrl = new URL(
                    "http://" + URLDecoder.decode(redirect, "UTF-8")
            );
        } catch (MalformedURLException e) {
            return error(
                    e,
                    "Error while getting token for user [" + username + " ] on service [" + service + "]"
            );
        } catch (UnsupportedEncodingException e) {
            return error(
                    e,
                    "Error while getting token for user [" + username + "] on service [" + service + "]"
            );
        }
        try {
            return Response.temporaryRedirect(finalRedirectUrl.toURI()).build();
        } catch (URISyntaxException e) {
            return error(e, "Malformed redirect URL");
        }
    }

    @DELETE
    @Path("source/{username}/{service}")
    public Response removeSource(
            @PathParam("username") String username,
            @PathParam("service") String service,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "removeSource",
                    username,
                    service,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        try {
            UUID.fromString(apiKey);
        } catch (IllegalArgumentException e) {
            return error(e, "Your apikey is not well formed");
        }
        User userObj;
        try {
            userObj = userManager.getUser(username);
            if (userObj == null) {
                return error(new NullPointerException(), "User [" + username + "] not found!");
            }
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user '" + username + "'");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    UUID.fromString(apiKey),
                    ApplicationsManager.Action.UPDATE,
                    ApplicationsManager.Object.USER
            );
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while asking for permissions");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "You're not allow to do that. Sorry.")
            );
            return rb.build();
        }

        try {
            userManager.deregisterService(service, userObj);
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user [" + username + "]");
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new StringPlatformResponse(
                StringPlatformResponse.Status.OK,
                "service [" + service + "] removed from user [" + username + "]")
        );
        return rb.build();
    }

    @GET
    @Path("/{username}/profile")
    public Response getProfile(
            @PathParam("username") String username,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "getProfile",
                    username,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        try {
            UUID.fromString(apiKey);
        } catch (IllegalArgumentException e) {
            return error(e, "Your apikey is not well formed");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    UUID.fromString(apiKey),
                    ApplicationsManager.Action.RETRIEVE,
                    ApplicationsManager.Object.PROFILE
            );
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authenticating you application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "Sorry. You're not allowed to do that.")
            );
            return rb.build();
        }
        User userObj;
        try {
            userObj = userManager.getUser(username);
        } catch (UserManagerException e) {
            return error(e, "Error while retrieving user '" + username + "'");
        }
        if (userObj == null) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "Sorry. User [" + username + "] has not been found")
            );
            return rb.build();
        }
        UserProfile up;
        try {
            up = profiles.lookup(userObj.getId());
        } catch (ProfilesException e) {
            return error(e, "Error while retrieving profile for user [" + username + "]");
        }
        if (up == null) {
            return error(new RuntimeException(), "Profile for user [" + username + "] not found");
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new UserProfilePlatformResponse(
                UserProfilePlatformResponse.Status.OK,
                "profile for user [" + username + "] found",
                up
        )
        );
        return rb.build();
    }

}