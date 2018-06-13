package org.coredata.core.util.nlp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hankcs.hanlp.HanLP;

@Service
public class NLPService {

	public List<String> extractSummary(String content, int size) {
		return HanLP.extractSummary(content, size);
	}
	
	public List<String> extractKeyword(String content, int size) {
		return HanLP.extractKeyword(content, size);
	}

}
