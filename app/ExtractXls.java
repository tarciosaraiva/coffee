import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.DateTime;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;

public class ExtractXls {

    public static void main(String[] args) throws IOException, InvalidFormatException {
        InputStream inp = new FileInputStream("/Users/tarcio/Documents/blackvelvet.xls");

        Workbook wb = WorkbookFactory.create(inp);

        for (int s = 0; s < wb.getNumberOfSheets(); s++) {
            Sheet sheet = wb.getSheetAt(s);
            String sheetName = sheet.getSheetName().split("-")[0];
            String[] personName = sheetName.split(" ");
            String firstName = personName[0];
            String lastName = "";
            if (personName.length == 2) {
                lastName = personName[1];
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

//            System.out.println("insert into client(first_name, last_name, balance) values ('" + firstName + "', '" + lastName + "', 0);");

            int rowC = 0;
            Iterator<Row> rowIterator = sheet.rowIterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                if (rowC > 0) {

                    String[] cellArgs = new String[]{null, null, null};

                    int cellC = 0;
                    Iterator<Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();

                        switch (cellC) {
                            case 1:
                                try {
                                    cellArgs[0] = sdf.format(cell.getDateCellValue());
                                    if (StringUtils.isBlank(cellArgs[0]) || "null".equals(cellArgs[0])) {
                                        cellArgs[0] = sdf.format(DateTime.now().toDate());
                                    }
                                } catch (Exception e) {
                                    if (StringUtils.isBlank(cellArgs[0]) || "null".equals(cellArgs[0])) {
                                        cellArgs[0] = sdf.format(DateTime.now().toDate());
                                    }
                                }
                                break;
                            case 2:
                                try {
                                    cellArgs[1] = "true";
                                    cellArgs[2] = NumberFormat.getInstance().format(cell.getNumericCellValue());
                                } catch (Exception e) {
//                                    System.out.println(e.getMessage());
                                }
                                break;
                            case 3:
                                if (cellArgs[2] == null || "0".equals(cellArgs[2])) {
                                    try {
                                        cellArgs[1] = "false";
                                        cellArgs[2] = NumberFormat.getInstance().format(cell.getNumericCellValue());
                                    } catch (Exception e) {
//                                    System.out.println(e.getMessage());
                                    }
                                }
                                break;
                        }

                        cellC++;

                    }

                    cellC = 0;
                    if (!"0".equals(cellArgs[2])) {
                        System.out.println("insert into transaction (transaction_date, credit, amount, client_id) values " +
                                "(TIMESTAMP '" + cellArgs[0] + "', " + cellArgs[1] + ", " + cellArgs[2] + ", " +
                                "(select id from client where first_name = '" + firstName + "' and last_name = '" + lastName + "'));");
                    }

                }

                rowC++;
            }
        }

    }

}
