package ru.greatbit.logsort;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by azee on 26.03.15.
 */
public class Main {

    public static void main(String... args){

        //File outputDir = new File(System.getProperty("user.dir"));
        File outputDir = new File("/Users/azee/Downloads/29/tmp");

        LogUtils logUtils = new LogUtils();
        for (File mhtFile : outputDir.listFiles()){
            if (!"mht".equals(FilenameUtils.getExtension(mhtFile.getName()))){
                continue;
            }

            //Move logs
            try {
                logUtils.moveLog(mhtFile, outputDir);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            new XLSUtils().createXls(outputDir, logUtils.getXlsRows());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
