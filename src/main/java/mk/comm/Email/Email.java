package mk.comm.Email;

import lombok.Data;

@Data
public class Email {
    String emailTo;
    String emailText;
    boolean selfSend;
}
