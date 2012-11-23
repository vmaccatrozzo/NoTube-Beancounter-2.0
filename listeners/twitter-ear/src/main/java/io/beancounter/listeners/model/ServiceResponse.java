package io.beancounter.listeners.model;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface ServiceResponse<T> {

    public T getResponse() throws ServiceResponseException;

}
