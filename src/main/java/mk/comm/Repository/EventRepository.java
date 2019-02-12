package mk.comm.Repository;

import mk.comm.Event.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {
    Event findById (Long id);
    List<Event>  findAllByCircleIdOOrderByDate (Long idCircle);
}
