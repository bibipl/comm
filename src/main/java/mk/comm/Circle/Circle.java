package mk.comm.Circle;

import lombok.Data;
import mk.comm.Member.Member;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "circle")
public class Circle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long groupId;
    private int number;
    private Long responsible;

    @ManyToMany (cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(name = "circle_member", joinColumns = @JoinColumn(name = "circle_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id"))
    List<Member> members = new ArrayList<>();

    public static Circle SortByName (Circle circle) {

        if (circle != null && circle.getMembers() != null && circle.getMembers().size() >1) {
            //get out all married women
            List<Member> marriedWomen = new ArrayList<>();
            List<Member> theRest = new ArrayList<>();
            for (Member member : circle.getMembers()) {
                if (member.getMarried() > 0 && member.getSex()=='K') {
                    boolean found = false;
                    for (Member husband : circle.getMembers()) {
                        if (husband.getMarried() == member.getId()) {
                            marriedWomen.add(member);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {theRest.add(member);}
                } else {
                    theRest.add(member);
                }
            }
            // *** only men and women without husband in the circle. lest sort.
            int size = theRest.size();
            while (size > 1) {
                Member member1 = theRest.get(0);
                for (int j = 1; j < size ; j++) {
                    Member member2 = theRest.get(j);
                    if ((member1.getSurname().compareTo(member2.getSurname()) < 0 && member2.getId() != circle.getResponsible())
                            || member1.getId() == circle.getResponsible())
                    {
                        theRest.remove(member1);
                        theRest.add(j, member1);
                        member1 = member2;
                    } else {
                        member1 = member2;


                    }
                }
                size--;
            }
            // sorted the Rest We now have to add women to husbands
            List <Member> all = new ArrayList<>();
            for (Member member : theRest) {
                if (member.getMarried() > 0) {
                    for (Member wife : marriedWomen) {
                        if (wife.getMarried() == member.getId()) {
                            all.add(wife);
                            break;
                        }
                    }
                    all.add(member);
                } else {
                    all.add(member);
                }
            }
            circle.setMembers(all);
        }
        return circle;
    }
}

