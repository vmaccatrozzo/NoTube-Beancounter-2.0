package tv.notube.usermanager;

import tv.notube.commons.model.OAuthToken;
import tv.notube.commons.model.User;
import tv.notube.usermanager.services.auth.ServiceAuthorizationManager;

import java.net.URL;

/**
 * Defines main responsabilities of a class handling users in
 * the <i>beancounter.io</i> ecosystem.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface UserManager {

    /**
     * Store a {@link User} on the Beancounter.
     *
     * @param
     * @throws UserManagerException
     */
    public void storeUser(User user) throws UserManagerException;

    /**
     * Retrieve a {@link User} from the Beancounter.
     *
     * @param username
     * @return
     * @throws UserManagerException
     */
    public User getUser(String username) throws UserManagerException;

    /**
     * Completely flushes out all the {@link User} data.
     *
     * @param username
     * @throws UserManagerException
     */
    public void deleteUser(String username) throws UserManagerException;

    /**
     * Get the user <a href="http://oauth.net">OAuth</> token.
     *
     * @param service
     * @param username
     * @return
     */
    OAuthToken getOAuthToken(String service, String username)
            throws UserManagerException;

    /**
     * It handles all the <i>OAuth-like</i> protocols handshaking.
     *
     * @param service
     * @param user
     * @param token   @throws UserManagerException
     */
    public void registerService(
            String service,
            User user,
            String token
    ) throws UserManagerException;

    /**
     * It handles all the <i>OAuth</i> protocol handshaking.
     *
     * @param service
     * @param user
     * @param token   @throws UserManagerException
     */
    public void registerOAuthService(
            String service,
            User user,
            String token,
            String verifier
    ) throws UserManagerException;

    /**
     * Returns the {@link ServiceAuthorizationManager} concrete implementation
     * binding with this user manager.
     *
     * @return
     * @throws UserManagerException
     */
    public ServiceAuthorizationManager getServiceAuthorizationManager()
            throws UserManagerException;

    /**
     * Removes from the provided {@link User} a service with the name
     * provided as input.
     *
     * @param service
     * @param userObj
     */
    void deregisterService(String service, User userObj) throws UserManagerException;

    /**
     * a temporary in-memory store for the final url where a user will be
     * redirected at the very end of all the authorization exchanges.
     *
     * @param username
     * @param url
     */
    public void setUserFinalRedirect(
            String username,
            URL url
    ) throws UserManagerException;

    /**
     * Get the user temporary final url where the user will be redirected
     * at the end of all the authorization exchange process. Once the url has
     * been consumed he needs to be set again.
     *
     * @param username
     * @return
     * @throws UserManagerException
     */
    public URL consumeUserFinalRedirect(String username) throws UserManagerException;

}
