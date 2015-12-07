package com.labuda.matt;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by matt on 06/12/2015.
 */
@Component
public class DirectoryScanner {


    @Value("${watched.directory}")
    private String directoryPath;

    private File directory;

    public Set<String> listFilesInDirectory(){
        Set<String> names = new HashSet<>();
        if(directory==null) {
            directory = new File(directoryPath);
        }

        for(File file : directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                DateTime lastModified = new DateTime(pathname.lastModified());
                return lastModified.isAfter(DateTime.now().minusHours(6));
            }
        }))
            names.add(file.getName());
       return names;
    }
}
