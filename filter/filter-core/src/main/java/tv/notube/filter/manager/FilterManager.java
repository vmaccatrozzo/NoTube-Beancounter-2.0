package tv.notube.filter.manager;

import tv.notube.filter.model.Filter;
import tv.notube.filter.model.pattern.ActivityPattern;

import java.util.Collection;

/**
 * This interface defines the minimum contract to perform CRUD operations
 * on {@link Filter}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface FilterManager {

    public String register(
            String name,
            String description,
            String queue,
            ActivityPattern activityPattern
    ) throws FilterManagerException;

    public Filter get(String name) throws FilterManagerException;

    public void delete(String name) throws FilterManagerException;

    public void start(String name) throws FilterManagerException;

    public void stop(String name) throws FilterManagerException;

    public void update(Filter filter) throws FilterManagerException;

    public Collection<String> getRegisteredFilters() throws FilterManagerException;

}