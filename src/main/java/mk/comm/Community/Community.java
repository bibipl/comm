package mk.comm.Community;

import lombok.Data;
import mk.comm.Member.Member;
import mk.comm.User.User;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table (name = "COMMUNITY")
public class Community {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne (fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", unique=true)
    private User user;

    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name="id_member")
    private List<Member> members = new ArrayList<>();
}
