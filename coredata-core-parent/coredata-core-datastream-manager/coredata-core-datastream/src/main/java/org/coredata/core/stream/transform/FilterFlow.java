package org.coredata.core.stream.transform;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.model.transform.Datasource;
import org.coredata.core.model.transform.Datatype;
import org.coredata.core.model.transform.Transform;
import org.coredata.core.model.transform.TransformModel;
import org.coredata.core.stream.service.StreamService;
import org.coredata.core.stream.transform.filters.DefaultFilterChain;
import org.coredata.core.stream.transform.filters.FilterChain;
import org.coredata.core.stream.transform.filters.FilterChainBuilder;
import org.coredata.core.stream.transform.filters.HeaderFilter;
import org.coredata.core.stream.util.ModelExpHelper;
import org.coredata.core.stream.vo.CMDInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;

import akka.stream.Attributes;
import akka.stream.FlowShape;
import akka.stream.Inlet;
import akka.stream.Outlet;
import akka.stream.stage.AbstractInHandler;
import akka.stream.stage.AbstractOutHandler;
import akka.stream.stage.GraphStage;
import akka.stream.stage.GraphStageLogic;

/**
 * 用于过滤器清洗数据流程
 * @author sue
 *
 */
public class FilterFlow<A, B> extends GraphStage<FlowShape<A, B>> {

	private static final Logger logger = LoggerFactory.getLogger(FilterFlow.class);

	public final Inlet<A> in = Inlet.create("FilterFlow.in");

	public final Outlet<B> out = Outlet.create("FilterFlow.out");

	private final FlowShape<A, B> flow = FlowShape.of(in, out);

	private ObjectMapper mapper = new ObjectMapper();

	private StreamService service;

	private static final String WITH_HEADER_TRUE = "yes";

	public FilterFlow(StreamService service) {
		this.service = service;
	}

	@Override
	public FlowShape<A, B> shape() {
		return flow;
	}

	@Override
	public GraphStageLogic createLogic(Attributes attr) throws Exception {
		return new GraphStageLogic(shape()) {
			{
				setHandler(in, new AbstractInHandler() {
					@SuppressWarnings("unchecked")
					@Override
					public void onPush() throws Exception {
						A msg = grab(in);
						if (logger.isDebugEnabled())
							logger.debug("Receive collect data add transform : " + msg.toString());
						Map<String, Object> json = mapper.readValue(msg.toString(), Map.class);
						Map<String, Object> modelMap = (Map<String, Object>) json.get("transformModel");
						TransformModel model = mapper.readValue(JSON.toJSONString(modelMap), TransformModel.class);
						if (logger.isDebugEnabled())
							logger.debug("TransformModel is : " + model.getId());
						Object cmd = json.get("name");
						String result = null;
						if (model != null && cmd != null) {
							FilterChain chain = createFilters(model, cmd.toString());
							try {
								result = service.processTransform(chain, json);
							} catch (Throwable e) {
								logger.error("Transform data error.", e);
								logger.error(e.getMessage());
								result = "";
							}
						}
						if (logger.isDebugEnabled())
							logger.debug("Send Transform Data to Kafka:" + result);
						if (!StringUtils.isEmpty(result))
							push(out, (B) result);
						else
							pull(in);
					}
				});

				setHandler(out, new AbstractOutHandler() {
					@Override
					public void onPull() throws Exception {
						pull(in);
					}
				});
			}
		};
	}

	/**
	 * 用于组装过滤器链
	 * @param model
	 * @return
	 */
	private FilterChain createFilters(TransformModel model, String cmd) {
		FilterChain filters = new DefaultFilterChain();
		for (Transform transform : model.getTransform()) {
			Datasource ds = transform.getDatasource();
			String datasource = ds.getSource();
			CMDInfo cmdInfo = ModelExpHelper.processCmd(datasource);
			if (cmdInfo == null || !cmd.equals(cmdInfo.getCmd()))
				continue;
			initFilterChain(filters, transform);
			break;
		}
		return filters;
	}

	private void initFilterChain(FilterChain chain, Transform transform) {
		// 首先需要添加转换表头处理
		Datasource datasource = transform.getDatasource();
		if (datasource != null) {
			Datatype dataType = datasource.getDatatype();
			if (dataType != null && dataType.getWithheader() != null && !WITH_HEADER_TRUE.equals(dataType.getWithheader()))
				chain.registFilter(new HeaderFilter());
		}
		if (transform.getFilter() != null) {
			FilterChainBuilder.buildChain(chain, transform.getFilter());
		}
	}

}
