package mk.comm.Circle;

import lombok.Data;
import mk.comm.Member.Member;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "circle")
public class Circle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int number;
    private Long idGrup;
    boolean responsible;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(name = "circle_member", joinColumns = @JoinColumn(name = "circle_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id"))
    private List<Member> circleMembers;

}

