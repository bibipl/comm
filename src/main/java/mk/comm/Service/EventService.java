package mk.comm.Service;

import mk.comm.Event.Event;

import java.util.List;

public interface EventService {
    void save (Event event);
    void delete (Event event);
    Event findById (Long id);
    List<Event> findAllByCircleIdOrderByDate (Long idCircle);
}
