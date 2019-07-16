package com.fav.common.msg;

import java.io.File;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MailService implements IMailService {

  @Autowired
  private JavaMailSender mailSender;
  @Value("${spring.mail.from}")
  private String from;

  @Override
  public void sendSimpleMail(String to, String subject, String content) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(from);
      message.setTo(to);
      message.setSubject(subject);
      message.setText(content);
      mailSender.send(message);
    } catch (Exception e) {
      log.error("邮件发送失败："+e.getMessage());
    }
  }

  @Override
  public void sendSimpleMailWithCC(String to, String subject, String content, String... cc) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(from);
      message.setTo(to);
      message.setCc(cc);
      message.setSubject(subject);
      message.setText(content);
      mailSender.send(message);
    } catch (Exception e) {
      log.error("邮件发送失败："+e.getMessage());
    }
  }

  @Override
  public void sendHtmlMail(String to, String subject, String content) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setFrom(from);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(content, true);
      mailSender.send(message);
    } catch (Exception e) {
      log.error("邮件发送失败："+e.getMessage());
    }
  }

  @Override
  public void sendHtmlMailWithCC(String to, String subject, String content, String... cc) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setFrom(from);
      helper.setTo(to);
      helper.setCc(cc);
      helper.setSubject(subject);
      helper.setText(content, true);
      mailSender.send(message);
    } catch (Exception e) {
      log.error("邮件发送失败："+e.getMessage());
    }
  }

  @Override
  public void sendAttachmentsMail(String to, String subject, String content, String filePath) {
    try {
      MimeMessage message = mailSender.createMimeMessage();

      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setFrom(from);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(content, true);

      FileSystemResource file = new FileSystemResource(new File(filePath));
      String fileName = filePath.substring(filePath.lastIndexOf(File.separator));
      helper.addAttachment(fileName, file);
      mailSender.send(message);
    } catch (Exception e) {
      log.error("邮件发送失败："+e.getMessage());
    }
  }

  @Override
  public void sendAttachmentsMailWithCC(String to, String subject, String content, String filePath, String... cc) {
    try {
      MimeMessage message = mailSender.createMimeMessage();

      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setFrom(from);
      helper.setTo(to);
      helper.setCc(cc);
      helper.setSubject(subject);
      helper.setText(content, true);

      FileSystemResource file = new FileSystemResource(new File(filePath));
      String fileName = filePath.substring(filePath.lastIndexOf(File.separator));
      helper.addAttachment(fileName, file);
      mailSender.send(message);
    } catch (Exception e) {
      log.error("邮件发送失败："+e.getMessage());
    }
  }

  @Override
  public void sendResourceMail(String to, String subject, String content, String rscPath, String rscId) {
    try {
      MimeMessage message = mailSender.createMimeMessage();

      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setFrom(from);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(content, true);

      FileSystemResource res = new FileSystemResource(new File(rscPath));
      helper.addInline(rscId, res);
      mailSender.send(message);
    } catch (Exception e) {
      log.error("邮件发送失败："+e.getMessage());
    }
  }

  @Override
  public void sendResourceMailWithCC(String to, String subject, String content, String rscPath, String rscId, String... cc) {
    try {
      MimeMessage message = mailSender.createMimeMessage();

      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setFrom(from);
      helper.setTo(to);
      helper.setCc(cc);
      helper.setSubject(subject);
      helper.setText(content, true);

      FileSystemResource res = new FileSystemResource(new File(rscPath));
      helper.addInline(rscId, res);
      mailSender.send(message);
    } catch (Exception e) {
      log.error("邮件发送失败："+e.getMessage());
    }
  }

}
