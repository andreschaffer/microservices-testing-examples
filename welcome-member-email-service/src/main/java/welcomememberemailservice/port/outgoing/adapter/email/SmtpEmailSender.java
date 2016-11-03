package welcomememberemailservice.port.outgoing.adapter.email;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.mail.Message.RecipientType.TO;

public class SmtpEmailSender {

    private static final String FROM = "special.membership@example.com";
    private final Session session;

    public SmtpEmailSender(Session session) {
        this.session = checkNotNull(session);
    }

    public void send(String email) throws SmtpEmailSenderException {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM));
            message.addRecipient(TO, new InternetAddress(email));
            message.setSubject("Welcome!");
            message.setText("It's our pleasure to have you as a member :)");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new SmtpEmailSenderException(e);
        }
    }
}
