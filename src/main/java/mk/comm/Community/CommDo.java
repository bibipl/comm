package mk.comm.Community;

import mk.comm.Member.Member;

import java.util.ArrayList;
import java.util.List;

// Here static methods user by Community
public class CommDo {

    public static List<Member> getMarriageOrder (List<Member> members) {
        if (members != null && members.size() > 1) {
            int s = 0; // s - in case of wrong data to limit loop repeat.
            List<Member> inOrderList = new ArrayList<>();
            Member[] orderArray = members.toArray(new Member[members.size()]);
            int i=0;
            for (i=0; i<orderArray.length-1; i++) {
                if (orderArray[i] != null) {
                    Member tempMember = orderArray[i];
                    Long marriedId = tempMember.getMarried();
                    if (marriedId > 0) {
                        if (tempMember.getSex() == ('M')) { // we look for wife and add after.
                            for (int j = i + 1; j < orderArray.length; j++) {
                                if (marriedId == orderArray[j].getId()) {
                                    if (tempMember.getSex() == 'M') {
                                        inOrderList.add(tempMember);
                                        inOrderList.add(orderArray[j]);
                                    } else {
                                        inOrderList.add(orderArray[j]);
                                        inOrderList.add(tempMember);
                                    }
                                    orderArray[j] = null;
                                }
                            }
                        } else { // we have to move wife after a husband - buuble move
                            int s1 = s; // to check if husband not found we should not change anything - especially i;
                            for (int j = i; j<orderArray.length-1; j++) {
                                orderArray[j] = orderArray[j+1];
                                if (orderArray[j+1].getMarried() == tempMember.getId()) {
                                    s = s+1;
                                    orderArray[j+1] = tempMember;
                                    break; // we found a husband and placed a wife after him
                                }
                            }
                            if (s > s1) {i--;} //wife moved we have to check new memeber on the i position as "for" will soon i++;
                            // s > s1 shows, that husband was found. Other situation should not happen but who knows ?
                        }
                    } else { // not married, add to list in order without changes.
                        inOrderList.add(tempMember);
                    }
                }
                if (s > orderArray.length) break; // just a safe button not to go into infinite loop with wrong data.
                // eg all women and all married but not any husband. should not happen but who knows ????
            }
            if (orderArray[i] != null) {
                inOrderList.add(orderArray[i]);
            }
            return inOrderList;
        }
        return members;
    }
}
