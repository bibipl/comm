package mk.comm.Service;

public interface EmailSender {
    void sendEmail(String to, String subject, String content);
}
