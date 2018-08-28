package org.incode.eurocommercial.ecpcrm.dom.event;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.repository.RepositoryService;

@DomainService(
        nature = NatureOfService.DOMAIN,
        repositoryFor = Event.class
)
public class EventRepository {

    @Programmatic
    public List<Event> listAll() {
        return repositoryService.allInstances(Event.class);
    }

    @Programmatic
    public List<Event> findByData(
            final String data
    ) {
        return repositoryService.allMatches(
                new QueryDefault<>(
                        Event.class,
                        "findByData",
                        "data", data));
    }

    @Programmatic
    public List<Event> findBySource(
            final EventSource source
    ) {
        return repositoryService.allMatches(
                new QueryDefault<>(
                        Event.class,
                        "findBySource",
                        "source", source));
    }


    @Programmatic
    public List<Event> findByDataContains(
            final String data
    ) {
        return repositoryService.allMatches(
                new QueryDefault<>(
                        Event.class,
                        "findByDataContains",
                        "data", data));
    }

    @Programmatic
    public Event findBySourceAndData(
            final EventSource source,
            final String data
    ) {
        return repositoryService.uniqueMatch(
                new QueryDefault<>(
                        Event.class,
                        "findBySourceAndData",
                        "source", source,
                        "data", data));
    }

    @Programmatic
    public Event create(
            final EventSource source,
            final String data
    ) {
        final Event event = repositoryService.instantiate(Event.class);
        event.setSource(source);
        event.setData(data);
        repositoryService.persist(event);
        event.createAspects();
        return event;
    }

    @Programmatic
    public Event findOrCreate(
            final EventSource source,
            final String data
    ) {
        Event event = findBySourceAndData(source, data);
        if (event == null) {
            event = create(source, data);
        }
        return event;
    }

    @Inject RepositoryService repositoryService;
}
