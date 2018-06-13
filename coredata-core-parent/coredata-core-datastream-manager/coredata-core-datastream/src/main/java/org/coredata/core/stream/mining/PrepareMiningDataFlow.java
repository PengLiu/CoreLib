package org.coredata.core.stream.mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coredata.core.model.mining.DataminingModel;
import org.coredata.core.model.mining.Datasource;
import org.coredata.core.stream.mining.entity.MiningData;
import org.coredata.core.stream.service.StreamService;
import org.coredata.core.stream.util.ModelExpHelper;
import org.coredata.core.stream.vo.CMDInfo;
import org.coredata.core.stream.vo.CMDInfo.SourceType;
import org.coredata.core.stream.vo.PrepareMiningData;
import org.coredata.core.stream.vo.TransformData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import akka.stream.Attributes;
import akka.stream.FlowShape;
import akka.stream.Inlet;
import akka.stream.Outlet;
import akka.stream.stage.AbstractInHandler;
import akka.stream.stage.AbstractOutHandler;
import akka.stream.stage.GraphStage;
import akka.stream.stage.GraphStageLogic;

public class PrepareMiningDataFlow<A, B> extends GraphStage<FlowShape<A, B>> {

	private static Logger logger = LoggerFactory.getLogger(PrepareMiningDataFlow.class);

	private static final String STREAM = "stream";

	public final Inlet<A> in = Inlet.create("PrepareMiningDataFlow.in");

	public final Outlet<B> out = Outlet.create("PrepareMiningDataFlow.out");

	private final FlowShape<A, B> flow = FlowShape.of(in, out);

	private StreamService streamService;

	private ObjectMapper mapper = new ObjectMapper();

	public PrepareMiningDataFlow(StreamService streamService) {
		this.streamService = streamService;
	}

	@Override
	public FlowShape<A, B> shape() {
		return flow;
	}

	@Override
	public GraphStageLogic createLogic(Attributes att) throws Exception {
		return new GraphStageLogic(shape()) {
			{
				setHandler(in, new AbstractInHandler() {
					@SuppressWarnings("unchecked")
					@Override
					public void onPush() throws Exception {
						A grab = grab(in);
						if (logger.isDebugEnabled())
							logger.debug("Receiving transformdata is :" + grab.toString());
						TransformData transformData = mapper.readValue(grab.toString(), TransformData.class);
						DataminingModel model = transformData.getDataminingModel();
						List<MiningData> results = new ArrayList<>();
						if (model != null) {
							List<MiningData> miningData = prepareMiningData(model, transformData);
							if (!CollectionUtils.isEmpty(miningData))
								results.addAll(miningData);
						}
						push(out, (B) results);
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

	private List<MiningData> prepareMiningData(DataminingModel model, TransformData transformData) {
		List<PrepareMiningData> datas = new ArrayList<>();
		model.getMining().forEach(mining -> {
			Map<String, String> alias = new HashMap<>();
			Map<String, String> instAlias = new HashMap<>();
			boolean needInstance = false;
			switch (mining.getCategory()) {
			case STREAM:
				for (Datasource ds : mining.getDatasource()) {
					String source = ds.getSourcecmd();
					CMDInfo cmdInfo = ModelExpHelper.processMiningCmd(source);
					if (cmdInfo != null) {
						alias.put(cmdInfo.getCmd(), ds.getId());
						if (!SourceType.cmd.toString().equals(cmdInfo.getSourceType())) {
							needInstance = true;
							instAlias.put(cmdInfo.getCmd(), ds.getId());
						}
					} else {
						logger.error(mining.getId() + " Cmd format not support " + source);
						continue;
					}
				}
				break;
			default:
				// off line data
			}
			PrepareMiningData cacheData = new PrepareMiningData();
			cacheData.setAlias(alias);
			cacheData.setInstAlias(instAlias);
			cacheData.setNeedInstance(needInstance);
			cacheData.setDatamining(mining);
			datas.add(cacheData);
		});
		return streamService.process(datas, transformData);
	}

}
