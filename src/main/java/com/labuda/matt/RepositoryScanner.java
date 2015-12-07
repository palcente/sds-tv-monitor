package com.labuda.matt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by matt on 06/12/2015.
 */
@Component
public class RepositoryScanner {

    private String SQL = "select INPUT_FILE from v_batch_job_execution where job_name = 'transit-valuator-job' and status = 'COMPLETED' and last_updated > (CURRENT_TIMESTAMP - INTERVAL '6' DAY)";

    @Autowired
    private JdbcTemplate template;

    public Set<String> listLoadedFiles(){
        Set<String> names = new HashSet<>();
        List<String> paths = template.query(SQL, new SingleColumnRowMapper<String>());

        for(String path : paths){
            names.add(path.substring(path.lastIndexOf("/")+1,path.length()));
        }

        return names;
    }

}
