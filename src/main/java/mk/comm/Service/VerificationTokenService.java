package mk.comm.Service;


import mk.comm.verificationToken.VerificationToken;

import java.time.LocalDate;
import java.util.List;

public interface VerificationTokenService {
    List<VerificationToken> findAllByUserId(Long userId);
    VerificationToken findById(Long id);
    void save(VerificationToken verificationToken);
    void delete(VerificationToken verificationToken);
    List<VerificationToken> findAllByDateLessThan (LocalDate date);
    VerificationToken findByToken (String token);

}
