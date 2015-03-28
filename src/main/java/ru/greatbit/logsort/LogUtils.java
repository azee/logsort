package ru.greatbit.logsort;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;
import ru.greatbit.logsort.beans.*;
import ru.greatbit.logsort.mht.MHTParser;
import ru.greatbit.utils.serialize.XmlSerializer;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by azee on 26.03.15.
 */
public class LogUtils {

    private final static String logFile = "testlog.xml";
    private final static String rootData = "root.xml";

    List<XlsRow> xlsRows = new LinkedList<>();

    public void moveLog(File mht, File sourceDir) throws Exception {
        Resolution resolution = getResolution(mht, sourceDir);
        File destinDir = new File(sourceDir.getAbsolutePath() + File.separator + resolution);
        if(!destinDir.exists()){
            destinDir.mkdir();
        }

        File destinFile = new File(destinDir.getAbsolutePath() + File.separator + mht.getName());
        mht.renameTo(destinFile);
    }

    public Resolution getResolution(File mht, File sourceDir) throws Exception {
        File tempDir = decompressMht(mht, sourceDir);
        TestLog testLog = parseTestLog(tempDir);
        Resolution resolution = getResolution(FilenameUtils.removeExtension(mht.getName()), testLog, getCaseName(tempDir));
        FileUtils.deleteDirectory(tempDir);
        return resolution;
    }

    public TestLog parseTestLog(File tempDir) throws Exception {
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

    public File decompressMht(File mht, File sourceDir) throws Exception {
        String tempDirName = UUID.randomUUID().toString();
        File tempDir = new File(sourceDir.getAbsolutePath() + File.separator + tempDirName);
        tempDir.mkdir();
        new MHTParser(mht, tempDir).decompress();
        return tempDir;
    }

    public List<XlsRow> getXlsRows() {
        if (xlsRows == null){
            xlsRows = new LinkedList<>();
        }
        return xlsRows;
    }

    private Resolution getResolution(String testId, TestLog testLog, String testName) {
        boolean warningExists = false;
        TestLogItem warnLogItem = null;


        for (TestLogItem logItem : testLog.getTestLogItems()){
            if ("Error".equals(logItem.getTypeDescription())){
                addXlsRow(testId, testName, logItem, Resolution.FAIL);
                return Resolution.FAIL;
            }
            if ("Warning".equals(logItem.getTypeDescription())){
                warningExists = true;
                if (warnLogItem == null){
                    warnLogItem = logItem;
                }
            }
        }

        if (warningExists){
            addXlsRow(testId, testName, warnLogItem, Resolution.WARNING);
            return Resolution.WARNING;
        }

        addXlsRow(testId, testName, null, Resolution.PASS);
        return Resolution.PASS;
    }

    private void addXlsRow(String testId, String testName, TestLogItem logItem, Resolution resolution){
        XlsRow xlsRow = new XlsRow()
                .withId(testId)
                .withName(testName)
                .withResolution(resolution);
        if (logItem != null){
            List<CallStackItem> stackItems = logItem.getCallStack().getCallStackItems();
            String line = stackItems.get(stackItems.size() - 1).getLineNo();
            xlsRow
                    .withMessage(logItem.getMessage())
                    .withLine(line);
        }
        xlsRows.add(xlsRow);
    }

    private String getCaseName(File tempDir) throws IOException, JAXBException, SAXException {
        String logPath = tempDir.getAbsolutePath() + File.separator + rootData;
        //filename is filepath string
        BufferedReader br = new BufferedReader(new FileReader(new File(logPath)));
        String line;
        StringBuilder sb = new StringBuilder();

        while((line = br.readLine())!= null){
            sb.append(line.trim());
        }

        String log = sb.toString()
                .replace("<LogData", "<ns2:LogData xmlns:ns2=\"beans.logsort.greatbit.ru\" xmlns=\"urn:beans.logsort.greatbit.ru\"")
                .replace("</LogData>", "</ns2:LogData>");

        LogData logData = XmlSerializer.unmarshal(log, LogData.class);

        return logData.getName().split("\\\\")[1].replace("]", "");
    }

}
