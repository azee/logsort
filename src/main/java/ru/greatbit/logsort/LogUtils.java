package ru.greatbit.logsort;

import io.qameta.allure.model.Label;
import io.qameta.allure.model.Stage;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import ru.greatbit.logsort.beans.*;
import ru.greatbit.logsort.mht.MHTParser;
import ru.greatbit.utils.serialize.JsonSerializer;
import ru.greatbit.utils.serialize.XmlSerializer;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;
import static ru.greatbit.logsort.beans.Resolution.*;

/**
 * Created by azee on 26.03.15.
 */
public class LogUtils {

    final Logger logger = LoggerFactory.getLogger(LogUtils.class);

    private final static String logFile = "testlog.xml";
    private final static String rootData = "root.xml";

    private final static String MESSAGE_TYPE = "Message";

    List<XlsRow> xlsRows = new LinkedList<XlsRow>();

    public TestResult getAllureTestResult(File mht, File sourceDir, String uuid) throws Exception {
        logger.debug(String.format("Decompressing %s", mht.getName()));
        File tempDir = decompressMht(mht, sourceDir);

        logger.debug(String.format("Parsing %s", mht.getName()));
        TestLog testLog = parseTestLog(tempDir);

        Resolution resolution = getResolution(FilenameUtils.removeExtension(mht.getName()), testLog, getCaseName(tempDir));
        logger.debug(String.format("Got resolution %s for %s", resolution, mht.getName()));

        TestResult testResult = new TestResult();
        testResult.setUuid(uuid);
        testResult.setStage(Stage.FINISHED);

        testResult.setStart(getDateTime(testLog.getTestLogItems().get(0).getTime()));
        testResult.setStop(getDateTime(testLog.getTestLogItems().get(testLog.getTestLogItems().size() - 1).getTime()));

        testResult.setStatus(convertStatusToAllure(resolution));

        testResult.setName(getCaseName(tempDir));
        testResult.setFullName(testResult.getName());
        String[] testcaseNameTokens = testResult.getName().split("_");
        testResult.setTestCaseId(testcaseNameTokens.length == 1 ? testcaseNameTokens[0] : testcaseNameTokens[1]);

        Label framework = new Label();
        framework.setName("framework");
        framework.setValue("TestComplete");
        testResult.getLabels().add(framework);

        Label language = new Label();
        language.setName("language");
        language.setValue("VisualBasicScript");
        testResult.getLabels().add(language);

        testResult.getSteps().addAll(
                testLog.getTestLogItems().stream().
                        filter(logItem -> MESSAGE_TYPE.equals(logItem.getTypeDescription())).
                        map(LogUtils::convertToStepResult).collect(toList())
        );

        File destinDir = new File(sourceDir.getAbsolutePath() + File.separator + "allure");
        if(!destinDir.exists()){
            destinDir.mkdir();
        }
        File outputFile = new File(destinDir.getAbsolutePath()  + File.separator + testResult.getFullName() + ".json");
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        writer.write(JsonSerializer.marshal(testResult));
        writer.close();

        FileUtils.deleteDirectory(tempDir);
        return testResult;
    }

    private Status convertStatusToAllure(Resolution resolution) {
        switch(resolution) {
            case FAIL:
                return Status.FAILED;
            case WARNING:
                return Status.FAILED;
            case PASS:
                return Status.PASSED;
        }
        return null;
    }

    public static StepResult convertToStepResult(TestLogItem logItem){
        StepResult stepResult = new StepResult();
        stepResult.setName(logItem.getMessage());
        stepResult.setStart(getDateTime(logItem.getTime()));
        stepResult.setStop(stepResult.getStart() + logItem.getTimeDiffsec() * 1000);

        return stepResult;
    }

    private static Long getDateTime(String time) {
        // 3/7/2020 2:55:07 PM format
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        try {
            return formatter.parse(time).getTime();
        } catch (ParseException e) {
            return null;
        }
    }

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
        logger.debug(String.format("Decompressing %s", mht.getName()));
        File tempDir = decompressMht(mht, sourceDir);

        logger.debug(String.format("Parsing %s", mht.getName()));
        TestLog testLog = parseTestLog(tempDir);

        Resolution resolution = getResolution(FilenameUtils.removeExtension(mht.getName()), testLog, getCaseName(tempDir));
        logger.debug(String.format("Got resolution %s for %s", resolution, mht.getName()));

        logger.debug(String.format("Removing %s", tempDir.getName()));
        FileUtils.deleteDirectory(tempDir);
        return resolution;
    }

    public TestLog parseTestLog(File tempDir) throws Exception {
        String logPath = tempDir.getAbsolutePath() + File.separator + getXmlLogFileName(tempDir);
        //filename is filepath string
        BufferedReader br = new BufferedReader(new FileReader(new File(logPath)));
        String line;
        StringBuilder sb = new StringBuilder();

        while((line = br.readLine())!= null){
            sb.append(line.trim());
        }

        String log = sb.toString()
                .replace("<TestLog>", "<ns2:TestLog xmlns:ns2=\"beans.logsort.greatbit.ru\" xmlns=\"urn:beans.logsort.greatbit.ru\">")
                .replace("</TestLog>", "</ns2:TestLog>");


        br.close();
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
            xlsRows = new LinkedList<XlsRow>();
        }
        return xlsRows;
    }

    private Resolution getResolution(String testId, TestLog testLog, String testName) {
        boolean warningExists = false;
        TestLogItem warnLogItem = null;


        for (TestLogItem logItem : testLog.getTestLogItems()){
            if ("Error".equals(logItem.getTypeDescription())){
                addXlsRow(testId, testName, logItem, FAIL);
                return FAIL;
            }
            if ("Warning".equals(logItem.getTypeDescription())){
                warningExists = true;
                if (warnLogItem == null){
                    warnLogItem = logItem;
                }
            }
        }

        if (warningExists){
            addXlsRow(testId, testName, warnLogItem, WARNING);
            return WARNING;
        }

        addXlsRow(testId, testName, null, PASS);
        return PASS;
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

        br.close();

        return logData.getName().split("\\\\")[1].replace("]", "");
    }

    private String getXmlLogFileName(File tempDir) {
        File dir = new File(tempDir + File.separator);
        for (File file : dir.listFiles()) {
            if (file.isFile() && logXmlFileNameMatch(file.getName())) {
                return file.getName();
            }
        }
        return logFile;
    }

    private boolean logXmlFileNameMatch(String name) {
        Pattern pattern = Pattern.compile("_testlog.xml$");
        Matcher matcher = pattern.matcher(name);
        return matcher.find();
    }

}
