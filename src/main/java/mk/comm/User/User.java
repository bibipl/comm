package mk.comm.User;

import lombok.Data;
import mk.comm.Community.Community;
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

    @NotBlank
    private String password;
    @Transient
    private String passwordCheck;

    private int enabled = 0;

    @OneToOne (cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "community_id", unique=true)
    private Community community;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;
 }
