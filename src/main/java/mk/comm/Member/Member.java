package mk.comm.Member;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Entity
@Table (name = "MEMBER")

public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @Size(max=245)
    @Column(name = "email", unique = true)
    private String email;

    @NotBlank
    private String name;
    @NotBlank
    private String surname;

    private String phone;

    private char sex;

    // married = 0 if no wife/husband in the community.
    private Long married; // 0 - not married, 1 id of wife.husband

    private Long communityId;

    private int attendance; //  higher number - less times comes to meetings.

    private  String token; // eventually to give acess to group data.

 }

