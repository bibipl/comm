package mk.comm.Comparators;

import mk.comm.Circle.Circle;

import java.util.Comparator;

public class CircleComparator implements Comparator<Circle>{

        @Override
        public int compare(Circle cr1, Circle cr2) {
            return cr2.getNumber() - cr1.getNumber();
        }

}
