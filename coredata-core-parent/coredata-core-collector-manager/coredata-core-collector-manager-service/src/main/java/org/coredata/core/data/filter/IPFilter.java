package org.coredata.core.data.filter;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.data.Record;
import org.coredata.core.data.util.IPSeeker;
import org.coredata.core.data.util.IPSeeker.IPLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IPFilter extends BaseFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(IPFilter.class);

	@Override
	public void doFilter(int columnIndex, Record record) {
		if(StringUtils.isNoneBlank(actionType)) {
			try {
				String ipAddress = String.valueOf(record.get(columnIndex));
				if(StringUtils.isNoneBlank(ipAddress)) {
					IPLocation location = IPSeeker.getInstance().getLocation(ipAddress);
					if (location != null && !StringUtils.isEmpty(location.getCity())) {
						String cityValue = location.getCity();
						processResult(cityValue, columnIndex, record);
					}
				}
			} catch (Exception e) {
				logger.info(" transfor error:", e);
			}
		}
	}
}
