package com.labuda.matt;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Created by matt on 06/12/2015.
 */

public class EmailSendingTask implements Runnable{

    private static Logger logger = LoggerFactory.getLogger(EmailSendingTask.class);

    String fileName;
    DateTime dateTime;
    JavaMailSender jvm;

    public EmailSendingTask(String fileName, DateTime dateTime, JavaMailSender jvm) {
        this.fileName = fileName;
        this.dateTime = dateTime;
        this.jvm=jvm;
    }

    @Override
    public void run() {
        StringBuilder sb = new StringBuilder();
        sb.append("File ").append(fileName)
                .append(" spotted at ")
                .append(dateTime.toString("yyyy-MM-dd hh:mm:ss"))
                .append(" has not been loaded for ")
                .append(Minutes.minutesBetween(dateTime,DateTime.now()).getMinutes())
                .append(" minutes. \n")
                .append("Please investigate.");
        SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo("mateuszlabuda@hotmail.co.uk");
            mail.setFrom("someone@localhost");
            mail.setSubject("TV MONITOR ALERT");
            mail.setText(sb.toString());
            logger.info("Email alert for the file {} sent to {} at ",fileName,"mateuszlabuda@hotmail.co.uk",DateTime.now().toString());
            jvm.send(mail);
    }
}
