package ru.greatbit.logsort;

import org.apache.commons.io.FilenameUtils;
import ru.greatbit.logsort.beans.XlsRow;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by azee on 26.03.15.
 */
public class Main {

    public static void main(String... args){

        File outputDir = new File(System.getProperty("user.dir"));

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
            List<XlsRow> rows = logUtils.getXlsRows();
            Collections.sort(rows, new Comparator<XlsRow>() {
                @Override
                public int compare(XlsRow o1, XlsRow o2) {
                    return o1.getId().compareTo(o2.getId());
                }
            });
            new XLSUtils().createXls(outputDir, rows);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
