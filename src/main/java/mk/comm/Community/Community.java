package mk.comm.Community;

import lombok.Data;
import mk.comm.Member.Member;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Table (name = "COMMUNITY")
public class Community {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Long userId; // not needed but just for safety to check with adminid.

}
