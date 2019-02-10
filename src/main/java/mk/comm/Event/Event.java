package mk.comm.Event;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Data
@Entity
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long idCircle;
    private LocalDate date;
    private String name;
    private String description;

    public List<String> getEventNames (int id) {
        List<String> events =Arrays.asList("Litugia Słowa","Eucharystia", "Konwiwencja", "Inne");
        return events;
    }
}
