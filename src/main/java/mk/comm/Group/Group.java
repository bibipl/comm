package mk.comm.Group;

import lombok.Data;

import javax.persistence.*;

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
