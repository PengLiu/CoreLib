package org.coredata.core.metric;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.coredata.core.TestApp;
import org.coredata.core.metric.documents.WebOpinion;
import org.coredata.core.metric.repositories.WebOpinionResp;
import org.coredata.core.metric.services.WebOpinionService;
import org.coredata.util.query.Fuzzy;
import org.coredata.util.query.Operation;
import org.coredata.util.query.TimeRange;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class WebOpinionTest {

	@Autowired
	private WebOpinionService webOpinionService;

	@Autowired
	private WebOpinionResp webOpinionResp;

	@Before
	public void init() throws InterruptedException {

		for (int i = 0; i < 100; i++) {
			WebOpinion opinion = new WebOpinion();
			opinion.setCategory(i % 2 == 0 ? "生活" : "学习");
			opinion.setCreatedTime(new Date(System.currentTimeMillis()));
			opinion.setKeywords("健康 生活 学习");
			opinion.setPolarity(i % 2 == 0 ? -1 : 1);
			opinion.setSrc("搜狐新闻");
			opinion.setUrl("http://www.sohu.com/a/231584217_116988");
			opinion.setTitle("现在的社会把这些小姐妹都逼成啥样了");
			opinion.setContent(
					"有人的地方，就有江湖。有江湖的地方，就有“武林霸主”。如果把A股上市银行也比作一个江湖，那么，谁是2017年的“武林霸主”呢？谁又是进步神速的江湖“后起之秀”呢？近日，26家A股上市银行2017年年报披露完毕。从总资产看，五大行中，工行仍以26.09万亿元的资产规模居首；而在股份行中，兴业、招行、浦发资产规模位列前三，分别为6.42万亿、6.3万亿和6.14万亿元；北京银行则是唯一一家资产规模超过2万亿元的城商行。而从营收来看，五大行中，工行增长率最高，2017年同比增长7.49%，达到7265.02亿元；招行以2208.97亿元位列股份行第一，且超过交行248.86亿元。另外，地方性银行中，贵阳银行以22.82%的增速领跑。值得一提的是，成都银行以逾51%的增速领");
			webOpinionResp.save(opinion);
			Thread.sleep(5);
		}
	}

	@Test
	public void queryTest() {

		
		Map<String,Object> props = new HashMap<>();
		
		TimeRange timeRange = new TimeRange(System.currentTimeMillis() - 60 * 1000, System.currentTimeMillis());
		Map<String, Object> result = webOpinionService.countByCondition(props, Operation.And, Fuzzy.None, timeRange);
		assertEquals(100L,result.get("count"));
		
		props.put("src", "搜狐新闻");
		result = webOpinionService.countByCondition(props, Operation.And, Fuzzy.None, timeRange);
		assertEquals(100L,result.get("count"));
		
		props.put("src", "搜狐新闻");
		props.put("title", "小姐妹");
		result = webOpinionService.countByCondition(props, Operation.And, Fuzzy.None, timeRange);
		assertEquals(0L,result.get("count"));
		
		props.put("title", "小姐妹");
		result = webOpinionService.countByCondition(props, Operation.And, Fuzzy.Match, timeRange);
		assertEquals(100L,result.get("count"));
		
		props.put("src", "搜狐");
		props.put("title", "小姐妹");
		result = webOpinionService.countByCondition(props, Operation.And, Fuzzy.None, timeRange);
		assertEquals(0L,result.get("count"));
		
		props.put("src", "搜狐新闻");
		props.put("polarity", 1);
		props.put("title", "*小姐妹*");
		result = webOpinionService.countByCondition(props, Operation.And, Fuzzy.Match, timeRange);
		assertEquals(50L,result.get("count"));

	}

	@After
	public void cleanup() {
		webOpinionResp.deleteAll();
	}

}
