package mk.comm.User;

import lombok.Data;
import mk.comm.Role.Role;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@Entity
@Table (name = "USER")

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // email used as login
    @Email
    @Size(max=245)
    @NotBlank
    @Column(name = "username", unique = true)
    private String username;

    @NotBlank
    private String name;
    @NotBlank
    private String surname;

    private String phone;

    @NotBlank
    private String password;
    @Transient
    private String passwordCheck;

    private int enabled = 0;
    
    private char sex;
    
    private Long married; // 0 - not married, 1 id of wife.houseband
    
    private int attendance; //  higher number - less times comes to meetings.

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;
 }

