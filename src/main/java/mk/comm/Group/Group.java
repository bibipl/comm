package mk.comm.Group;

import lombok.Data;
import mk.comm.Circle.Circle;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "grup")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long idCommunity;
    private String name;
}
