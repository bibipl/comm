package mk.comm.Email;

import lombok.Data;

@Data
public class Email {
    String emailTo;
    String emailHead;
    String emailText;
    boolean selfSend;
}
