package org.coredata.core.data.readers.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.coredata.core.data.DefaultRecord;
import org.coredata.core.data.Fields;
import org.coredata.core.data.OutputFieldsDeclarer;
import org.coredata.core.data.PluginConfig;
import org.coredata.core.data.Reader;
import org.coredata.core.data.Record;
import org.coredata.core.data.RecordCollector;
import org.coredata.core.data.debugger.JobDetail;
import org.coredata.core.data.debugger.JobStatus;
import org.coredata.core.data.exception.DataException;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;

@Service(value = "excelReader")
@Scope("prototype")
public class ExcelReader extends Reader {

	private Workbook workbook = null;
	private boolean includeColumnNames = false;
	private Fields fields = new Fields();
	private int startRow = 1;

	@Override
	public void prepare(PluginConfig readerConfig) {

		super.prepare(readerConfig);

		startRow = readerConfig.getInt(ExcelProperties.START_ROW, 1);
		String path = readerConfig.getString(ExcelProperties.PATH);
		Preconditions.checkNotNull(path, "Excel reader required property: path");

		try {
			if (path.endsWith(".xlsx")) {
				workbook = new XSSFWorkbook(new File(path));
			} else {
				workbook = new HSSFWorkbook(new FileInputStream(new File(path)));
			}
		} catch (IOException | InvalidFormatException e) {
			throw new DataException(e);
		}

		includeColumnNames = readerConfig.getBoolean(ExcelProperties.INCLUDE_COLUMN_NAMES, false);
	}

	@Async
	@Override
	public CompletableFuture<JobDetail> execute(RecordCollector recordCollector) {

		if (workbook.getNumberOfSheets() > 0) {
			Sheet sheet = workbook.getSheetAt(0);

			if (includeColumnNames && sheet.getPhysicalNumberOfRows() > 0) {
				Row row = sheet.getRow(0);
				for (int cellIndex = row.getFirstCellNum(), cellLength = row
						.getPhysicalNumberOfCells(); cellIndex < cellLength; cellIndex++) {
					fields.add(row.getCell(cellIndex).toString());
				}
			}

			long index = 1L;
			int startRowCloumns = includeColumnNames ? 1 : 0;
			long currentRow = 0;
			for (int rowIndex = startRowCloumns, rowLength = sheet
					.getPhysicalNumberOfRows(); rowIndex < rowLength; rowIndex++) {
				currentRow++;
				if (currentRow >= startRow) {
					Row row = sheet.getRow(rowIndex);
					Record record = new DefaultRecord(row.getPhysicalNumberOfCells());
					for (int cellIndex = row.getFirstCellNum(), cellLength = row
							.getPhysicalNumberOfCells(); cellIndex < cellLength; cellIndex++) {
						if (row.getCell(cellIndex) == null) {
							record.add("");
						} else {
							int type = row.getCell(cellIndex).getCellType();
							switch (type) {
							case Cell.CELL_TYPE_STRING:
								record.add(row.getCell(cellIndex).getStringCellValue());
								break;
							case Cell.CELL_TYPE_NUMERIC:
								record.add(row.getCell(cellIndex).getNumericCellValue());
								break;
							case Cell.CELL_TYPE_BOOLEAN:
								record.add(row.getCell(cellIndex).getBooleanCellValue());
								break;
							case Cell.CELL_TYPE_BLANK:
								record.add("");
								break;
							case Cell.CELL_TYPE_FORMULA:
								record.add(row.getCell(cellIndex).getNumericCellValue());
								break;
							case Cell.CELL_TYPE_ERROR:
								record.add(row.getCell(cellIndex).getErrorCellValue());
								break;
							default:
								record.add(row.getCell(cellIndex).toString());
							}
						}
					}

					// Do filters
					doFilter(record);
					// get selected columns
					doSelect(record);
					recordCollector.send(record);

					if (getRecordLimit() > 0) {
						if (index >= getRecordLimit()) {
							break;
						}
						index++;
					}
				}
			}
		}

		jobDetail.setStatus(JobStatus.Success);
		return CompletableFuture.completedFuture(jobDetail);
	}

	@Override
	public void close() {
		if (workbook != null) {
			try {
				workbook.close();
			} catch (IOException e) {
				throw new DataException(e);
			}
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(fields);
	}

}
