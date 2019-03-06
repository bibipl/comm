package mk.comm.Service;

import mk.comm.Repository.VerificationTokenRepository;
import mk.comm.verificationToken.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class VerificationTokenServiceImpl implements VerificationTokenService{

    @Autowired
    VerificationTokenRepository verificationTokenRepository;

    @Override
    public List<VerificationToken> findAllByUserId(Long userId) {
        return verificationTokenRepository.findAllByUserId(userId);
    }

    @Override
    public VerificationToken findById(Long id) {
        return verificationTokenRepository.findById(id);
    }

    @Override
    public void save(VerificationToken verificationToken) {
        LocalDate date = LocalDate.now();
        verificationToken.setDate(date);
        verificationTokenRepository.save(verificationToken);
        // below we delete all tokens created more than 1-2 days ago. just to keep order
        List<VerificationToken> tokens = findAllByDateLessThan(date.minusDays(2));
        if (tokens != null) {
            for (VerificationToken token : tokens) {
                verificationTokenRepository.delete(token);
            }
        }
    }

    @Override
    public void delete(VerificationToken verificationToken) {
        verificationTokenRepository.delete(verificationToken);
    }

    @Override
    public List<VerificationToken> findAllByDateLessThan(LocalDate date) {
        return verificationTokenRepository.findAllByDateLessThan(date);
    }

    @Override
    public VerificationToken findByToken(String token) {
        return verificationTokenRepository.findByToken(token);
    }
}
