package dupd.hku.com.hkusap.mail;

import java.io.File;

public class SendMailUtil {

    //qq
    private static final String HOST = "smtp.qq.com";
    private static final String PORT = "587";
    private static final String FROM_ADD = "teprinciple@foxmail.com"; //发送方邮箱
    private static final String FROM_PSW = "lfrlpganzjrwbeci";//发送方邮箱授权码


    public static void send(final File file, String toAdd) {
        final MailInfo mailInfo = createMail(toAdd);
        final MailSender sms = new MailSender();
        new Thread(new Runnable() {
            @Override
            public void run() {
                sms.sendFileMail(mailInfo, file);
            }
        }).start();
    }

    public static void send(String toAdd) {
        final MailInfo mailInfo = createMail(toAdd);
        final MailSender sms = new MailSender();
        new Thread(() -> sms.sendTextMail(mailInfo)).start();
    }

    private static MailInfo createMail(String toAdd) {
        final MailInfo mailInfo = new MailInfo();
        mailInfo.setMailServerHost(HOST);
        mailInfo.setMailServerPort(PORT);
        mailInfo.setValidate(true);
        mailInfo.setUserName(FROM_ADD); // 你的邮箱地址
        mailInfo.setPassword(FROM_PSW);// 您的邮箱密码
        mailInfo.setFromAddress(FROM_ADD); // 发送的邮箱
        mailInfo.setToAddress(toAdd); // 发到哪个邮件去
        mailInfo.setSubject("Hello"); // 邮件主题
        mailInfo.setContent("Android 测试"); // 邮件文本
        return mailInfo;
    }

}
