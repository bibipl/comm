package mk.comm.verificationToken;

import lombok.Data;
import mk.comm.User.User;

import javax.persistence.*;

@Data
@Entity
public class VerificationToken {
    private static final int EXPIRATION = 60 * 24;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    // standard constructors, getters and setters - not needed due to lombok
}

