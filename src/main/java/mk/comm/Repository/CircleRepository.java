package mk.comm.Repository;

import mk.comm.Circle.Circle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CircleRepository extends JpaRepository <Circle, Integer> {
    Circle findById (Long id);
}
