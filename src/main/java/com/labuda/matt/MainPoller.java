package com.labuda.matt;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Minutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by matt on 06/12/2015.
 */
@Component
public class MainPoller {

    private static Logger logger = LoggerFactory.getLogger(MainPoller.class);

    @Autowired
    DirectoryScanner ds;
    @Autowired
    RepositoryScanner rs;
    @Value("${alert.threshold.minutes.string}")
    private String threshold;
    @Autowired
    JavaMailSender jvm;
    private Executor executor = Executors.newSingleThreadExecutor();

    private Map<String, DateTime> unloadedFiles = new HashMap<>();

    @Scheduled(initialDelay = 1000, fixedDelay = 30000)
    public void poll(){
        Set<String> dirFiles = ds.listFilesInDirectory();
        Set<String> repFiles = rs.listLoadedFiles();

        for(String fileInTheDirectory : dirFiles){
            if(!repFiles.contains(fileInTheDirectory)){
                if(!unloadedFiles.containsKey(fileInTheDirectory)){
                    logger.warn("Spotted unloaded file: {} at {} , monitoring...", fileInTheDirectory,DateTime.now().toString("yyyy-MM-dd hh:mm:ss"));
                    unloadedFiles.put(fileInTheDirectory,DateTime.now());
                }
            }
        }

        updateMonitoredFiles(repFiles);
        checkForAlerts(unloadedFiles);

}

    private void checkForAlerts(Map<String, DateTime> unloadedFiles) {
        Iterator<Map.Entry<String,DateTime>> it = unloadedFiles.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, DateTime> entry = it.next();
            if(Minutes.minutesBetween(entry.getValue(),DateTime.now()).getMinutes() > Integer.valueOf(threshold)){
                sendEmailAlert(entry.getKey(),entry.getValue());
            }
        }
    }

    @Async
    private void sendEmailAlert(String name, DateTime spotted) {
        logger.error("FILE {} HAS NOT BEEN LOADED FOR {} MINUTES. SENDING E-MAIL ALERT...",name, Integer.valueOf(threshold));
        executor.execute(new EmailSendingTask(name,spotted,jvm));
    }

    public void updateMonitoredFiles(Set<String> loadedFiles){
        Iterator<Map.Entry<String,DateTime>> it = unloadedFiles.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String,DateTime> entry = it.next();

            if(loadedFiles.contains(entry.getKey())){
                logger.warn("File : {} was loaded after {} minutes and will longer be monitored.", entry.getKey(), Minutes.minutesBetween(entry.getValue(),DateTime.now()).getMinutes());
                it.remove();
            }
        }
    }
}
