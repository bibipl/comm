package mk.comm.HelpfClasses;

import java.util.Arrays;
import java.util.List;

public class MemberAttr {

    public static List<String> married () {
        List<String> civilState = Arrays.asList(
                "Małżonek we wspólnocie",
                "Małżonek poza wspólnotą",
                "Stan Wolny");
        return civilState;

    }

    public static List<Character> sex () {
        List<Character> gender = Arrays.asList(
                'M',
                'K');
        return gender;
    }
    public static List<String> attendance () {
        List<String> attends = Arrays.asList(
                "często",
                "czasem",
                "rzadko",
                "nie przychodzi");
        return attends;
    }
}
