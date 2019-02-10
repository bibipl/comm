package mk.comm.Service;

import mk.comm.Circle.Circle;


public interface CircleService {
    void save (Circle circle);
    void delete (Circle circle);
    Circle findById (Long id);

}
