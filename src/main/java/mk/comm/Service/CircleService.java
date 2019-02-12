package mk.comm.Service;

import mk.comm.Circle.Circle;

import java.util.List;


public interface CircleService {
    void save (Circle circle);
    void delete (Circle circle);
    Circle findById (Long id);
    List<Circle> findAllByGroupIdOrderByNumberAsc (Long idGroup);
    int countAllByGroupId (Long idGroup);

}
