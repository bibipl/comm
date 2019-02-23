package mk.comm.Comparators;

import mk.comm.Member.Member;

import java.util.Comparator;

public class MemberComparator implements Comparator<Member> {
    @Override
    public int compare(Member m1, Member m2) {
        int result = 0;
        if (m1 != null && m2 != null) {
            result = m1.getSurname().compareTo(m2.getSurname());
            if (result == 0) result = m1.getName().compareTo(m2.getName());
        }
        return result;
    }
}
