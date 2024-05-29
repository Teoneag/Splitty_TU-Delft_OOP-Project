package client.services;

import com.google.inject.Inject;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

final public class EmailService {
    private final ConfigService configService;

    @Inject
    public EmailService(ConfigService configService) {
        this.configService = configService;
    }

    /**
     * Sends a default email to the email address in the config file
     *
     * @return - true if the email was sent, false otherwise
     */
    public boolean sendDefaultEmail() {
        String receiver = configService.getEmail();
        if(receiver == null || receiver.isEmpty()) {
            return false;
        }
        return sendMail(receiver, "Default mail", "You received the default email");
    }

    /**
     * Sends an email from the email address in the config file
     *
     * @param receiver - the receiver of the email
     * @param subject - the subject of the email
     * @param body    - the body of the email
     * @return - true if the email was sent, false otherwise
     */
    private boolean sendMail(String receiver, String subject, String body) {
        String sender = configService.getEmail();
        String password = configService.getPassword();
        if(sender == null || sender.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }

        String host  = "smtp." + sender.split("@")[1];

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(587);
        mailSender.setUsername(sender);
        mailSender.setPassword(password);

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth.plain.disable", "true");


        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(sender);
            message.setTo(receiver);
            message.setCc(sender);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sends an invitation to the email address
     *
     * @param email - the email address of the receiver
     * @param inviteCode - the invite code of the event
     * @param firstName - the first name of the receiver
     * @return - true if the email was sent, false otherwise
     */
    public boolean sendInviteEmail(String email, String inviteCode, String firstName) {
        String body = "Hello " + firstName + ",\n\n" +
                "You have been invited to an event. \n\n" +
                "Please use the following information to join the event. \n" +
                "Server url: " + configService.getServer() + "\n" +
                "Invite code:" + inviteCode +"\n\n" +
                "Best regards,\n" +
                "Team Splitty";
        return sendMail(email, firstName + ", you were invited to an event!", body);
    }
}
