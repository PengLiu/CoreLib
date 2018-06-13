package org.coredata.core.stream;

import org.springframework.stereotype.Component;

import akka.NotUsed;
import akka.kafka.ConsumerMessage.CommittableMessage;
import akka.kafka.javadsl.Consumer.Control;
import akka.stream.ClosedShape;
import akka.stream.Graph;
import akka.stream.Outlet;
import akka.stream.SinkShape;
import akka.stream.UniformFanOutShape;
import akka.stream.javadsl.Balance;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

/**
 * 告警中心流程图构建类
 * @author sue
 *
 */
@Component
public class AlarmStreamGraph {

	private Source<CommittableMessage<String, String>, Control> source;

	@SuppressWarnings("rawtypes")
	private Graph processAlarmSink;

	@SuppressWarnings({ "rawtypes" })
	public RunnableGraph spliceGraph(int parallelNum) {
		//首先拼接并行flow图
		Sink<Object, NotUsed> sinks = joinParallelFlows(parallelNum);
		RunnableGraph graph = RunnableGraph.fromGraph(GraphDSL.create(b -> {
			Source<String, Control> source = this.source.map(f -> f.record().value());
			Outlet<String> out = b.add(source).out();
			/**
			 * 并行+串行写法
			 */
			b.from(out).to(b.add(sinks));
			return ClosedShape.getInstance();
		}));
		return graph;
	}

	@SuppressWarnings({ "unchecked" })
	private <In> Sink<In, NotUsed> joinParallelFlows(int parallelNum) {
		Sink<In, NotUsed> sinkGraph = (Sink<In, NotUsed>) Sink.fromGraph(GraphDSL.create(b -> {
			final UniformFanOutShape<String, String> fanIn = b.add(Balance.create(parallelNum));
			for (int i = 0; i < parallelNum; i++) {
				b.from(fanIn.out(i)).to((SinkShape<? super String>) b.add(processAlarmSink));//to(b.add(Sink.ignore()));//
			}
			return SinkShape.of(fanIn.in());
		}));
		return sinkGraph;
	}

	public void setSource(Source<CommittableMessage<String, String>, Control> source) {
		this.source = source;
	}

	@SuppressWarnings("rawtypes")
	public void setProcessAlarmSink(Graph processAlarmSink) {
		this.processAlarmSink = processAlarmSink;
	}

}
