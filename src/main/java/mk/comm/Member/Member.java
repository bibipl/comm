package mk.comm.Member;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Entity
@Table (name = "MEMBER")
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @Size(max=245)
    @Column(name = "email")
    private String email;

    @NotBlank (message = "Imię nie może być puste")
    private String name;
    @NotBlank
    private String surname;

    private String phone;

    private Character sex;

    // married = 0 if no wife/husband in the community.
    private Long married = (long)0; // 0 - not married, 1 id of wife.husband

    private Long communityId;

    private String attendance; //  higher number - less times comes to meetings.

    private  String token; // eventually to give acess to group data.

    @Transient
    boolean doSomeAction = false;

    public boolean isDoSomeAction() {
        return doSomeAction;
    }
    public void setDoSomeAction(boolean doSomeAction) {
        this.doSomeAction = doSomeAction;
    }

}


