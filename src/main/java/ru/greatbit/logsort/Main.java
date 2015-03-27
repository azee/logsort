package ru.greatbit.logsort;

import org.xml.sax.SAXException;
import ru.greatbit.logsort.beans.TestLog;
import ru.greatbit.utils.serialize.XmlSerializer;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

/**
 * Created by azee on 26.03.15.
 */
public class Main {

    public static void main(String... args){
//        FIleUtils.createTestDirs("/Users/azee/Downloads/29/tmp");
//
//        File outputDir = new File("/Users/azee/Downloads/29/tmp");
//        File mhtFile = new File("/Users/azee/Downloads/29/Red/1341.mht");
//        try {
//            LogUtils.getResolution(mhtFile, outputDir);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        TestLog testLog = null;
        try {
            testLog = LogUtils.parseTestLog(
                    new File("/Users/azee/Downloads/29/tmp/d8d43204-5d8f-4a96-9f84-0248a426dd4b")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.print(testLog);


    }
}
