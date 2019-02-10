package mk.comm.Service;

import mk.comm.Circle.Circle;
import mk.comm.Repository.CircleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class CircleServiceImpl implements CircleService {

    @Autowired
    CircleRepository circleRepository;

    @Override
    public void save(Circle circle) {
        circleRepository.save(circle);
    }

    @Override
    public void delete(Circle circle) {
        circleRepository.delete(circle);
    }

    @Override
    public Circle findById(Long circleId) {
        return circleRepository.findById(circleId);
    }

}
