package mk.comm.Event;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long circleId;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private String name;
    private String description;
}
