package mk.comm.Circle;

import mk.comm.Member.Member;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CircleTest {

    @Test
    public void sortingOfEmptyCircleShouldReturnEmptyCircle() {
        Circle unsorted = new Circle();
        Circle result = Circle.SortByName(unsorted);
        assertTrue(result.getMembers().isEmpty());
    }

    @Test
    public void sortingOfOneMemberShouldReturnOneElement() {
        Circle unsorted = createTestCircle(new Member[] {
                createMember(1L, "Andrzej", "Wspolnotowy", 'M', 0L)
        });

        Circle expected = createTestCircle(new Member[] {
                createMember(1L, "Andrzej", "Wspolnotowy", 'M', 0L)
        });

        Circle result = Circle.SortByName(unsorted);
        assertEquals(expected.getMembers(), result.getMembers());
    }

    @Test
    public void sortingOfNotMarriedMan() {
        Circle unsorted = createTestCircle(new Member[] {
                createMember(1L, "Andrzej", "Andrzejewski", 'M', 0L),
                createMember(4L, "Cezary",  "Cosik", 'M', 0L),
                createMember(5L, "Daniel",  "Dabrowski", 'M', 0L),
                createMember(6L, "Bogumil", "Baranowski", 'M', 0L),
                createMember(3L, "Adam",    "Andrzejkiewicz", 'M', 0L),
                createMember(2L, "Zbyszek", "Andrzejczak", 'M', 0L),
        });

        Circle expected = createTestCircle(new Member[] {
                createMember(5L, "Daniel",  "Dabrowski", 'M', 0L),
                createMember(4L, "Cezary",  "Cosik", 'M', 0L),
                createMember(6L, "Bogumil", "Baranowski", 'M', 0L),
                createMember(3L, "Adam",    "Andrzejkiewicz", 'M', 0L),
                createMember(1L, "Andrzej", "Andrzejewski", 'M', 0L),
                createMember(2L, "Zbyszek", "Andrzejczak", 'M', 0L),
        });

        Circle result = Circle.SortByName(unsorted);
        assertEquals(expected.getMembers(), result.getMembers());
    }

    @Test
    public void sortingOfNotMarriedManAndWoman() {
        Circle unsorted = createTestCircle(new Member[] {
                createMember(1L, "Andrzej", "Andrzejewski", 'M', 0L),
                createMember(4L, "Joanna",  "Baranowska", 'K', 0L),
                createMember(5L, "Danuta",  "Dabrowska", 'K', 0L),
                createMember(6L, "Leszek",  "Baranowski", 'M', 0L),
                createMember(3L, "Adam",    "Andrzejkiewicz", 'M', 0L),
                createMember(2L, "Zbyszek", "Andrzejczak", 'M', 0L),
        });

        Circle expected = createTestCircle(new Member[] {
                createMember(5L, "Danuta",  "Dabrowska", 'K', 0L),
                createMember(6L, "Leszek",  "Baranowski", 'M', 0L),
                createMember(4L, "Joanna",  "Baranowska", 'K', 0L),
                createMember(3L, "Adam",    "Andrzejkiewicz", 'M', 0L),
                createMember(1L, "Andrzej", "Andrzejewski", 'M', 0L),
                createMember(2L, "Zbyszek", "Andrzejczak", 'M', 0L),
        });

        Circle result = Circle.SortByName(unsorted);
        assertEquals(expected.getMembers(), result.getMembers());
    }

    @Test
    public void sortingOfMarriedManAndWoman() {
        Circle unsorted = createTestCircle(new Member[] {
                createMember(1L, "Andrzej", "Andrzejewski", 'M', 0L),
                createMember(4L, "Joanna",  "Baranowska", 'K', 0L),
                createMember(5L, "Danuta",  "Dabrowska", 'K', 3L),
                createMember(6L, "Leszek",  "Baranowski", 'M', 0L),
                createMember(3L, "Adam",    "Andrzejkiewicz", 'M', 5L),
                createMember(2L, "Zbyszek", "Andrzejczak", 'M', 0L),
        });

        Circle expected = createTestCircle(new Member[] {
                createMember(6L, "Leszek",  "Baranowski", 'M', 0L),
                createMember(4L, "Joanna",  "Baranowska", 'K', 0L),
                createMember(5L, "Danuta",  "Dabrowska", 'K', 3L),
                createMember(3L, "Adam",    "Andrzejkiewicz", 'M', 5L),
                createMember(1L, "Andrzej", "Andrzejewski", 'M', 0L),
                createMember(2L, "Zbyszek", "Andrzejczak", 'M', 0L),
        });

        Circle result = Circle.SortByName(unsorted);
        assertEquals(expected.getMembers(), result.getMembers());
    }

    private Circle createTestCircle(Member[] members) {
        Circle circle = new Circle();
        circle.setMembers(new ArrayList<>(Arrays.asList(members)));
        return circle;
    }

    private Member createMember(Long id, String name, String surname, Character sex, Long married) {
        String email = "";
        String phone = "";
        Long communityId = 0L;
        String attendance = "";
        String token = "";
        boolean doSomeAction = false;
        return new Member(id, email, name, surname, phone, sex, married, communityId, attendance, token, doSomeAction);
    }

}