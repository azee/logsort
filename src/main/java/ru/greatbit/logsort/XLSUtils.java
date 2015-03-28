package ru.greatbit.logsort;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
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
        Workbook wb = new HSSFWorkbook();

        Sheet sheet = wb.createSheet("Results");

        //Add static headers
        List<String> header = new ArrayList<>();
        header.addAll(Arrays.asList("Id", "Name", "Resulution", "Messafe",
                "Line"));

        //Header row
        Row headerRow = sheet.createRow(0);
        Cell headerCell;
        for (int i = 0; i < header.size(); i++) {
            headerCell = headerRow.createCell(i);
            headerCell.setCellValue(header.get(i));
        }

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
            style.setFillForegroundColor(HSSFColor.YELLOW.index);
            style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        }
        if (xlsRow.getResolution() == Resolution.PASS){
            style.setFillForegroundColor(HSSFColor.GREEN.index);
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
