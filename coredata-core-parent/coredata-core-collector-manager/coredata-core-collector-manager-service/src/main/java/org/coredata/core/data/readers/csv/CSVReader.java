package org.coredata.core.data.readers.csv;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.coredata.core.data.DefaultRecord;
import org.coredata.core.data.OutputFieldsDeclarer;
import org.coredata.core.data.PluginConfig;
import org.coredata.core.data.Reader;
import org.coredata.core.data.Record;
import org.coredata.core.data.RecordCollector;
import org.coredata.core.data.debugger.JobDetail;
import org.coredata.core.data.debugger.JobStatus;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service(value = "csvReader")
@Scope("prototype")
public class CSVReader extends Reader {

	private String path = null;
	private int startRow = 1;
	private String encoding = null;
	private String format;
	private String delimiter;
	private String recordSeparator;
	private CSVFormat csvFormat = CSVFormat.TDF;

	private boolean stop = false;

	@Override
	public void prepare(PluginConfig readerConfig) {
		super.prepare(readerConfig);
		path = readerConfig.getString(CSVReaderProperties.PATH);
		startRow = readerConfig.getInt(CSVReaderProperties.START_ROW, 1);
		encoding = readerConfig.getString(CSVReaderProperties.ENCODING, "UTF-8");
		format = readerConfig.getString(CSVReaderProperties.FORMAT, "");
		delimiter = readerConfig.getString(CSVReaderProperties.DELIMITER, "");
		recordSeparator = readerConfig.getString(CSVReaderProperties.RECORDSEPATATOR, "");
		csvFormat = FormatConf.confCsvFormat(format);
		if (StringUtils.isNotEmpty(delimiter)) {
			csvFormat.withDelimiter(delimiter.charAt(0));
		}
		if (StringUtils.isNotEmpty(recordSeparator)) {
			csvFormat.withRecordSeparator(recordSeparator);
		}
	}

	@Async
	@Override
	public CompletableFuture<JobDetail> execute(RecordCollector recordCollector) {

		long currentRow = 0;

		try (java.io.Reader in = new InputStreamReader(new FileInputStream(path), encoding)) {

			Iterable<CSVRecord> records = csvFormat.parse(in);

			long index = 1L;

			for (CSVRecord csvRecord : records) {
				if (stop) {
					break;
				}
				currentRow++;
				if (currentRow >= startRow) {
					Record hdataRecord = new DefaultRecord(csvRecord.size());
					for (int i = 0, len = csvRecord.size(); i < len; i++) {
						hdataRecord.add(csvRecord.get(i));
					}
					doFilter(hdataRecord);
					//get selected columns
					doSelect(hdataRecord);
					recordCollector.send(hdataRecord);
					if (getRecordLimit() > 0) {
						if (index >= getRecordLimit()) {
							break;
						}
						index++;
					}
				}
			}
			jobDetail.setStatus(JobStatus.Success);
		} catch (IOException e) {
			jobDetail.setStatus(JobStatus.ReaderErr);
		}

		return CompletableFuture.completedFuture(jobDetail);

	}

	@Async
	@Override
	public void close() {
		stop = true;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {

	}

}
