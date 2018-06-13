package org.coredata.core.business.repositories;

import org.coredata.core.business.documents.PacketAnalys;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PacketAnalysResp extends ElasticsearchRepository<PacketAnalys, String> {

}
