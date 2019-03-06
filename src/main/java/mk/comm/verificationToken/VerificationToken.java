package mk.comm.verificationToken;

import lombok.Data;
import mk.comm.User.User;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity
public class VerificationToken {
    private static final int EXPIRATION = 24*60; // in days.

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String token;
    // old ones not user we delete
    private LocalDate date;
    // to keep new email until confirmed
    private String email;
    // action - registration, change email.
    private int action;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    // standard constructors, getters and setters - not needed due to lombok
}

