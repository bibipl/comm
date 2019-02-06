package mk.comm;

import lombok.Data;

@Data
public class Email {
    String emailTo;
    String emailText;
    boolean selfSend;
}
