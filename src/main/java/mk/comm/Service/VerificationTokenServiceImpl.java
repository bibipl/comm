package mk.comm.Service;

import mk.comm.Repository.VerificationTokenRepository;
import mk.comm.verificationToken.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        verificationTokenRepository.save(verificationToken);
    }

    @Override
    public void delete(VerificationToken verificationToken) {
        verificationTokenRepository.delete(verificationToken);
    }
}
