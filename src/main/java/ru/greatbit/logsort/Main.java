package ru.greatbit.logsort;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.greatbit.logsort.beans.XlsRow;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Created by azee on 26.03.15.
 */
public class Main {

    public static void main(String... args) throws Exception {

        final Logger logger = LoggerFactory.getLogger(Main.class);

        File outputDir = new File(System.getProperty("user.dir"));

        LogUtils logUtils = new LogUtils();
        String runUuid = UUID.randomUUID().toString();
        for (File mhtFile : outputDir.listFiles()){
            logger.info("Processing " + mhtFile.getName());

            if (!"mht".equals(FilenameUtils.getExtension(mhtFile.getName()))){
                continue;
            }

            logUtils.getAllureTestResult(mhtFile, outputDir, runUuid);

            //Move logs
            try {
                logger.info("Moving " + mhtFile.getName());
                logUtils.moveLog(mhtFile, outputDir);
            } catch (Exception e) {
                logger.error(String.format("Couldn't move file %s", mhtFile.getName()), e);
            }
        }

        try {
            logger.info("Xls file creation started");
            List<XlsRow> rows = logUtils.getXlsRows();
            Collections.sort(rows, new Comparator<XlsRow>() {
                @Override
                public int compare(XlsRow o1, XlsRow o2) {
                    return o1.getId().compareTo(o2.getId());
                }
            });
            new XLSUtils().createXls(outputDir, rows);
        } catch (IOException e) {
            logger.error("Couldn't create xls file", e);
        }
    }
}
