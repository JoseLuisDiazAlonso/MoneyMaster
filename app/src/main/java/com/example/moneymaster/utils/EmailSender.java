package com.example.moneymaster.utils;

import android.util.Log;

import java.util.Properties;

// ⚠️ Estos imports son CRÍTICOS — deben ser javax.mail, NO java.net
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

    private static final String TAG = "EmailSender";
    private static final String FROM_EMAIL = "tucorreo@gmail.com";
    private static final String APP_PASSWORD = "xxxx xxxx xxxx xxxx";

    public static boolean sendRecoveryCode(String toEmail, String code) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("MoneyMaster - Código de recuperación");

            String htmlBody = "<!DOCTYPE html>" +
                    "<html><body style='font-family: Arial, sans-serif; max-width: 500px; margin: auto;'>" +
                    "<div style='background: #6366f1; padding: 20px; border-radius: 12px 12px 0 0; text-align: center;'>" +
                    "<h1 style='color: white; margin: 0;'>💰 MoneyMaster</h1>" +
                    "</div>" +
                    "<div style='background: #f8f9fa; padding: 30px; border-radius: 0 0 12px 12px;'>" +
                    "<h2 style='color: #333;'>Recuperación de contraseña</h2>" +
                    "<p style='color: #666;'>Tu código de verificación es:</p>" +
                    "<div style='background: white; border: 2px solid #6366f1; border-radius: 8px; " +
                    "padding: 20px; text-align: center; margin: 20px 0;'>" +
                    "<span style='font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #6366f1;'>" +
                    code +
                    "</span>" +
                    "</div>" +
                    "<p style='color: #888; font-size: 13px;'>⏰ Este código expira en <strong>2 horas</strong>.</p>" +
                    "<p style='color: #888; font-size: 13px;'>Si no solicitaste esto, ignora este correo.</p>" +
                    "</div></body></html>";

            message.setContent(htmlBody, "text/html; charset=UTF-8");
            Transport.send(message);
            Log.d(TAG, "Email enviado exitosamente a: " + toEmail);
            return true;

        } catch (MessagingException e) {
            Log.e(TAG, "Error enviando email: " + e.getMessage());
            return false;
        }
    }
}