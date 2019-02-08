package mk.comm.Member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberMailTo {
    private Long id;
    private String longName;
    private String mail;
    private boolean action = false;

}
