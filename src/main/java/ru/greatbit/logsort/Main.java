package ru.greatbit.logsort;

import java.io.File;

/**
 * Created by azee on 26.03.15.
 */
public class Main {

    public static void main(String... args){

        File outputDir = new File(System.getProperty("user.dir"));

        for (File mhtFile : outputDir.listFiles()){
            //Move logs
            try {
                LogUtils.moveLog(mhtFile, outputDir);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
