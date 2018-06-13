package org.coredata.core.framework.action.processer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.coredata.core.model.action.model.Controller;
import org.coredata.core.model.collection.Param;
import org.coredata.core.model.constants.ClientConstant;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 斯美特物联设备动作指令的处理
 * @author ChengYongfei
 *
 */
@Service
public class SmartisysActionProcesser extends ActionProcesser {

	private static final String paramExp = "\\$\\{(.*?)\\}";
	private static final Pattern paramPattern = Pattern.compile(paramExp);
	private ObjectMapper mapper = new ObjectMapper();
	
	@Override
	protected String getCmdString(String cmd, JsonNode instance, Controller controller) {
		String result=cmd;
		List<Param> param = controller.getParam();
		Map<String, String> paramMap = new HashMap<String, String>();
		param.forEach(p -> {
			paramMap.put(p.getKey(), p.getValue());
		});
		Matcher dsMatcher = paramPattern.matcher(result);
		String code=instance.get("index").asText();
		String roomCode="";
		JsonNode properties = null;
		try {
			properties = mapper.readTree(instance.get(ClientConstant.INSTANCE_PROPERTIES).asText());
			Iterator<JsonNode> iterator =properties.elements();
			while(iterator.hasNext()){
				JsonNode prop=iterator.next();
				if("parentIndex".equals(prop.get("propertyId").textValue())){
					roomCode=prop.get("propertyValue").textValue();
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (dsMatcher.find()) {
			String paramStr = dsMatcher.group(1);
			switch (paramStr) {
			case "index":
		        result=result.replace(dsMatcher.group(), code);
				break;
			case "roomIndex":
				result=result.replace(dsMatcher.group(), roomCode);
				break;
			default:
				break;
			}
		}
		return result;
	}

}
