package org.coredata.core.sdk.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.input.CharSequenceReader;
import org.coredata.core.metric.documents.WebOpinion;
import org.coredata.core.metric.services.WebOpinionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/api/v1/opinion")
public class WebOpinionApi {

	@Autowired
	private WebOpinionService webOpinionService;

	private int batchSize = 500;

	private ObjectMapper mapper = new ObjectMapper();

	@PostMapping("/")
	public ResponseEntity<String> saveOpinions(@RequestParam("file") MultipartFile dataFile) {
		if (dataFile.isEmpty()) {
			return new ResponseEntity<String>("File is empty.", HttpStatus.OK);
		}
		try {
			saveUploadedFiles(Arrays.asList(dataFile));
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<String>("Save WebOinion successfully.", new HttpHeaders(), HttpStatus.OK);
	}

	private void saveUploadedFiles(List<MultipartFile> files) throws IOException {

		for (MultipartFile file : files) {

			if (file.isEmpty()) {
				continue;
			}

			List<WebOpinion> opnions = new ArrayList<>();

			byte[] bytes = file.getBytes();

			try (BufferedReader reader = new BufferedReader(new CharSequenceReader(new String(bytes)))) {

				String content = null;
				while ((content = reader.readLine()) != null) {
					WebOpinion opnion = mapper.readValue(content, WebOpinion.class);
					opnions.add(opnion);
					if (opnions.size() >= batchSize) {
						webOpinionService.save(opnions);
						opnions = new ArrayList<>();
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (!CollectionUtils.isEmpty(opnions)) {
				webOpinionService.save(opnions);
			}
		}

	}

}
