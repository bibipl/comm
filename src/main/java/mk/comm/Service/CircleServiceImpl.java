package mk.comm.Service;

import mk.comm.Circle.Circle;
import mk.comm.Repository.CircleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class CircleServiceImpl implements CircleService {

    @Autowired
    CircleRepository circleRepository;

    @Override
    public void save(Circle circle) {
        circle = Circle.SortByName(circle);
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

    @Override
    public List<Circle> findAllByGroupIdOrderByNumberAsc(Long idGroup) {
        return circleRepository.findAllByGroupIdOrderByNumberAsc(idGroup);
    }

    @Override
    public int countAllByGroupId(Long idGroup) {
        return circleRepository.countAllByGroupId( idGroup);
    }
}


