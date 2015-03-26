package ru.greatbit.logsort;

import java.io.File;

/**
 * Created by azee on 26.03.15.
 */
public class FIleUtils {

    public static void createTestDirs(String rootDir){
        createDir(rootDir + File.pathSeparator + "Green");
        createDir(rootDir + File.pathSeparator + "Yellow");
        createDir(rootDir + File.pathSeparator + "Red");
    }

    public static void createDir(String name){
        File dir = new File(name);
        dir.mkdir();
    }
}
