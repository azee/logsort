package ru.greatbit.logsort;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.greatbit.logsort.beans.Resolution;
import ru.greatbit.logsort.beans.XlsRow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by azee on 28.03.15.
 */
public class XLSUtils {

    public void createXls(File destinationDir, List<XlsRow> rows) throws IOException {
        final Logger logger = LoggerFactory.getLogger(XLSUtils.class);

        logger.info("Creating a workbook");
        Workbook wb = new HSSFWorkbook();

        Sheet sheet = wb.createSheet("Results");

        //Add static headers
        logger.info("Creating headers");
        List<String> header = new ArrayList<String>();
        header.addAll(Arrays.asList("Id", "Name", "Resolution", "Message",
                "Line"));

        //Header row
        Row headerRow = sheet.createRow(0);
        Cell headerCell;
        for (int i = 0; i < header.size(); i++) {
            headerCell = headerRow.createCell(i);
            headerCell.setCellValue(header.get(i));
        }

        logger.info("Filling rows");
        for (int i = 0; i < rows.size(); i++){
            createRow(wb, sheet, i + 1, rows.get(i));
        }
        FileOutputStream fileOut = new FileOutputStream(destinationDir.getAbsolutePath() + File.separator + "Report.xls");
        wb.write(fileOut);
        fileOut.flush();
        fileOut.close();
    }

    private void createRow(Workbook workbook, Sheet sheet, int rowNum, XlsRow xlsRow) {
        Row row = sheet.createRow(rowNum);

        CellStyle style = workbook.createCellStyle();
        if (xlsRow.getResolution() == Resolution.FAIL){
            style.setFillForegroundColor(HSSFColor.RED.index);
            style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        }
        if (xlsRow.getResolution() == Resolution.WARNING){
            style.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
            style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        }
        if (xlsRow.getResolution() == Resolution.PASS){
            style.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
            style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        }
        
        Cell cell = row.createCell(0);
        cell.setCellStyle(style);
        cell.setCellValue(xlsRow.getId());

        cell = row.createCell(1);
        cell.setCellStyle(style);
        cell.setCellValue(xlsRow.getName());

        cell = row.createCell(2);
        cell.setCellStyle(style);
        cell.setCellValue(xlsRow.getResolution().toString());

        cell = row.createCell(3);
        cell.setCellStyle(style);
        cell.setCellValue(xlsRow.getMessage());

        cell = row.createCell(4);
        cell.setCellStyle(style);
        cell.setCellValue(xlsRow.getLine());
    }
}
