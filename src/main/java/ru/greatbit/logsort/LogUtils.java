package ru.greatbit.logsort;

import ru.greatbit.logsort.beans.TestLog;
import ru.greatbit.logsort.mht.MHTParser;
import ru.greatbit.utils.serialize.XmlSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.UUID;

/**
 * Created by azee on 26.03.15.
 */
public class LogUtils {

    private final static String logFile = "testlog.xml";


    public static void getResolution(File mht, File sourceDir) throws Exception {
        File tempDir = decompressMht(mht, sourceDir);
        TestLog testLog = parseTestLog(tempDir);
        tempDir.delete();
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

        return XmlSerializer.unmarshal(sb.toString(), TestLog.class);
    }

    public static  File decompressMht(File mht, File sourceDir) throws Exception {
        String tempDirName = UUID.randomUUID().toString();
        File tempDir = new File(sourceDir.getAbsolutePath() + File.separator + tempDirName);
        tempDir.mkdir();
        new MHTParser(mht, tempDir).decompress();
        return tempDir;
    }
}
