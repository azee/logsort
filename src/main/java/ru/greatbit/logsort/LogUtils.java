package ru.greatbit.logsort;

import org.apache.commons.io.FileUtils;
import ru.greatbit.logsort.beans.Resolution;
import ru.greatbit.logsort.beans.TestLog;
import ru.greatbit.logsort.beans.TestLogItem;
import ru.greatbit.logsort.mht.MHTParser;
import ru.greatbit.utils.serialize.XmlSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.UUID;

/**
 * Created by azee on 26.03.15.
 */
public class LogUtils {

    private final static String logFile = "testlog.xml";


    public static void moveLog(File mht, File sourceDir) throws Exception {
        Resolution resolution = getResolution(mht, sourceDir);
        File destinDir = new File(sourceDir.getAbsolutePath() + File.separator + resolution);
        if(!destinDir.exists()){
            destinDir.mkdir();
        }

        File destinFile = new File(destinDir.getAbsolutePath() + File.separator + mht.getName());
        mht.renameTo(destinFile);
    }

    public static Resolution getResolution(File mht, File sourceDir) throws Exception {
        File tempDir = decompressMht(mht, sourceDir);
        TestLog testLog = parseTestLog(tempDir);
        FileUtils.deleteDirectory(tempDir);
        return getResolution(testLog);
    }

    public static  TestLog parseTestLog(File tempDir) throws Exception {
        String logPath = tempDir.getAbsolutePath() + File.separator + logFile;
        //filename is filepath string
        BufferedReader br = new BufferedReader(new FileReader(new File(logPath)));
        String line;
        StringBuilder sb = new StringBuilder();

        while((line=br.readLine())!= null){
            sb.append(line.trim());
        }

        String log = sb.toString()
                .replace("<TestLog>", "<ns2:TestLog xmlns:ns2=\"beans.logsort.greatbit.ru\" xmlns=\"urn:beans.logsort.greatbit.ru\">")
                .replace("</TestLog>", "</ns2:TestLog>");

        return XmlSerializer.unmarshal(log, TestLog.class);
    }

    public static  File decompressMht(File mht, File sourceDir) throws Exception {
        String tempDirName = UUID.randomUUID().toString();
        File tempDir = new File(sourceDir.getAbsolutePath() + File.separator + tempDirName);
        tempDir.mkdir();
        new MHTParser(mht, tempDir).decompress();
        return tempDir;
    }

    private static Resolution getResolution(TestLog testLog) {
        boolean warningExists = false;

        for (TestLogItem logItem : testLog.getTestLogItems()){
            if ("Error".equals(logItem.getTypeDescription())){
                return Resolution.FAIL;
            }
            if ("Warning".equals(logItem.getTypeDescription())){
                warningExists = true;
            }
        }

        if (warningExists){
            return Resolution.WARNING;
        }

        return Resolution.PASS;
    }
}
