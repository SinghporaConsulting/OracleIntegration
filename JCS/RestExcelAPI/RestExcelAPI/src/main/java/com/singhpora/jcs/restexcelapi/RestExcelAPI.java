package com.singhpora.jcs.restexcelapi;

import java.io.ByteArrayOutputStream;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import oracle.adf.share.logging.ADFLogger;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

@Path("restexcelapi")
@Produces("application/octet-stream")
public class RestExcelAPI {
    public RestExcelAPI() {
        super();
    }
    
    private ADFLogger logger = ADFLogger.createADFLogger("com.singhpora.jcs.restexcelapi");

    @GET
    @Produces("application/octet-stream")
    @Path("excel")
    public Response getData(@QueryParam("callerReference") String callerReference) {
        try {
            ByteArrayOutputStream content = this.getExcelStream(callerReference, getConnection());
            ResponseBuilder response = Response.ok(content.toByteArray());
            
            
            response.header("Content-Type", "application/octet-stream;charset=UTF-8");
            response.header("Content-Disposition", "attachment; filename=OutputExcel.xls");
            logger.info("<<<<<<<Responding with success with OutputExcel.xls for caller reference: " + callerReference);
            return response.build(); 
        } catch (Exception e) {
            logger.severe(e);
            e.printStackTrace();
            ResponseBuilder response = Response.ok("{\"errorMessage\":\"" + e.getMessage() + "\"}");
            response.status(Response.Status.INTERNAL_SERVER_ERROR);
            response.header("Content-Type", "application/json");

            return response.build();
        }
     
    }
    
    /**
     *Generate the Excel content and return it as a Stream
     * @param callerRef
     * @param connection
     * @return
     */
    protected ByteArrayOutputStream getExcelStream(String callerRef, 
                                                 Object connection) throws Exception{
        if("ERROR".equalsIgnoreCase(callerRef))
            throw new RuntimeException("MockError - received callerReference 'ERROR'.");
        
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("OutputWorksheet1");
        
        
        // Style for header cells
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short)16);
        headerStyle.setFont(font);
        
        
        // Create Title (merged cell) 
        Row reportHeaderRow = sheet.createRow(1);
        Cell headerCell = reportHeaderRow.createCell(1);
        headerCell.setCellValue((String)" Sample Excel returned from Rest API ");
        headerCell.setCellStyle(headerStyle);
        reportHeaderRow.createCell(3).setBlank();
        Row reportHeaderRow2 = sheet.createRow(2);
        reportHeaderRow2.createCell(1).setBlank();
        reportHeaderRow2.createCell(2).setBlank();
        reportHeaderRow2.createCell(3).setBlank();
        sheet.addMergedRegion(new CellRangeAddress(1,2,1,6));
            
        sheet.createRow(3);
        Row dateRow = sheet.createRow(4);
        dateRow.createCell(1).setCellValue((String) " Date: ");
        dateRow.createCell(2).setCellValue((new Date()).toString());
        
        
        //style and formatting for table header cells
        CellStyle tableHeaderStyle = workbook.createCellStyle(); 
        tableHeaderStyle.setBorderTop(BorderStyle.THIN);
        tableHeaderStyle.setBorderBottom(BorderStyle.THIN);
        tableHeaderStyle.setBorderLeft(BorderStyle.THIN);
        tableHeaderStyle.setBorderRight(BorderStyle.THIN);
        tableHeaderStyle.setFillBackgroundColor(IndexedColors.GREEN.getIndex());
        tableHeaderStyle.setWrapText(true);
        Font tableHeaderFont = workbook.createFont();
        tableHeaderFont.setBold(true);
        tableHeaderStyle.setFont(tableHeaderFont);
    
        tableHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        tableHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
        
        Row tableHeaderRow = sheet.createRow(6);
        tableHeaderRow.createCell(1).setCellValue("Mock Column");
        tableHeaderRow.createCell(2).setCellValue("Address Line1");
        tableHeaderRow.createCell(3).setCellValue("Address Line2");
        tableHeaderRow.createCell(4).setCellValue("Description");
        tableHeaderRow.createCell(5).setCellValue("Last updated");
        tableHeaderRow.createCell(6).setCellValue("More detail");
        

        //style and formatting for data cells
        CellStyle dataCellStyle = workbook.createCellStyle(); 
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);
        
        dataCellStyle.setWrapText(true);
        
    
        int dataRowIndex = 7;
        while (dataRowIndex < 12) {
            Row row = sheet.createRow(dataRowIndex++);
            row.createCell(1).setCellValue((String) "mock1."+dataRowIndex);           
            row.createCell(2).setCellValue((String) "mock2."+dataRowIndex);
         
            row.createCell(3).setCellValue((String) "mock3."+dataRowIndex);
            row.createCell(4).setCellValue((String) "mock4."+dataRowIndex);
            row.createCell(5).setCellValue((String) "mock5."+dataRowIndex);
            row.createCell(6).setCellValue((String) "mock6."+dataRowIndex);
        }
        
        applyStyleToCellRange(6,6, 1,6,tableHeaderStyle,sheet);
        applyStyleToCellRange(7,dataRowIndex-1, 1,6,dataCellStyle,sheet);
        sheet.setColumnWidth(1, 20*256); //20 chars
        sheet.setColumnWidth(2, 20*256);
        sheet.setColumnWidth(3, 48*256);
        sheet.setColumnWidth(4, 48*256);
        sheet.setColumnWidth(5, 16*256);
        sheet.setColumnWidth(6, 24*256);
        ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
        
        workbook.write(responseStream);


        return responseStream;
    }
    
    
    /**
     *Apply a particular style to a range of rows and columns (typically a table of data)
     * @param rowStart
     * @param rowEnd
     * @param colStart
     * @param colEnd
     * @param style
     * @param sheet
     */
    private void applyStyleToCellRange( int rowStart, int rowEnd, int colStart, int colEnd, CellStyle style, HSSFSheet sheet){
        for(int i=rowStart;i<=rowEnd;i++){
            Row row = sheet.getRow(i);
            for(int j=colStart; j<=colEnd; j++){
                Cell cell = row.getCell(j);
                cell.setCellStyle(style);
            }
        }
        
    }

    /**
     * Placeholder operation - can be enhanced to return a DB connection or another type 
     * of connection to fetch data
     */
    private Object getConnection() {
        return null;
    }
       
}
