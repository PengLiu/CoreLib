package org.coredata.core.data.util;

import java.util.HashMap;
import java.util.Map;

import org.coredata.core.data.filter.GrokFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.thekraken.grok.api.Grok;
import io.thekraken.grok.api.Match;
import io.thekraken.grok.api.exception.GrokException;

public class GrokUtils {
	
	private static Logger logger = LoggerFactory.getLogger(GrokUtils.class);
	
	private GrokUtils() {
		
    }
	
	private Grok init() {
		Grok grok = null;
		try {
			grok = Grok.create(GrokFilter.class.getClassLoader().getResource("pattern").getPath());
		} catch (GrokException e) {
			logger.error(" grokutils init error ", e);
		}
		return grok;
	}

    private static class GrokUtilsHolder {

            private static final Grok INSTANCE = new GrokUtils().init();

     }

    public static Grok getInstance() {
            return GrokUtilsHolder.INSTANCE;
    }
    
    /**
     * 使用系统提供的规则
     * @param pattern
     * @param text
     * @return
     */
    public static Map<String, Object> match(String pattern,String text) {
    	Map<String, Object> map = new HashMap<>();
    	Grok grok = getInstance();
    	try {
			grok.compile(pattern);
			Match match = grok.match(text);
			match.captures();
			if(!match.isNull()) {
				return match.toMap();
			}
		} catch (GrokException e) {
			logger.info(" grok match error", e);
		}
    	return map;
    }
    public static String matchToJson(String pattern,String text) {
    	String returnJson = "";
    	Grok grok = getInstance();
    	try {
    		grok.compile(pattern);
    		Match match = grok.match(text);
    		match.captures();
    		if(!match.isNull()) {
    			return match.toJson();
    		}
    	} catch (GrokException e) {
    		logger.info(" grok match error", e);
    	}
    	return returnJson;
    }
    
    /**
     * 可以临时增加规则查找匹配
     * @param pattern
     * @param text
     * @param newPatternMap
     * @return
     */
    public static Map<String, Object> match(String pattern,String text,Map<String, String> newPatternMap) {
    	Map<String, Object> map = new HashMap<>();
    	Grok grok = getInstance();
    	//增加新的规则
    	newPatternMap.forEach((k,v)->{
    		try {
				grok.addPattern(k, v);
			} catch (GrokException e) {
				logger.info(" add grok pattern error:", e);
			}
    	});
    	try {
			grok.compile(pattern);
			Match match = grok.match(text);
			match.captures();
			if(!match.isNull()) {
				return match.toMap();
			}
		} catch (GrokException e) {
			logger.info(" grok match error", e);
		}
    	return map;
    }
    public static String matchToJson(String pattern,String text,Map<String, String> newPatternMap) {
    	String returnJson = "";
    	Grok grok = getInstance();
    	//增加新的规则
    	newPatternMap.forEach((k,v)->{
    		try {
    			grok.addPattern(k, v);
    		} catch (GrokException e) {
    			logger.info(" add grok pattern error:", e);
    		}
    	});
    	try {
    		grok.compile(pattern);
    		Match match = grok.match(text);
    		match.captures();
    		if(!match.isNull()) {
    			return match.toJson();
    		}
    	} catch (GrokException e) {
    		logger.info(" grok match error", e);
    	}
    	return returnJson;
    }
}
