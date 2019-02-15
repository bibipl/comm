package mk.comm.Repository;

import mk.comm.verificationToken.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer> {
    List<VerificationToken> findAllByUserId(Long userId);
    VerificationToken findById(Long id);
}
