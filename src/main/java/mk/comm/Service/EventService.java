package mk.comm.Service;

import mk.comm.Event.Event;

public interface EventService {
    void save (Event event);
    void delete (Event event);
}
