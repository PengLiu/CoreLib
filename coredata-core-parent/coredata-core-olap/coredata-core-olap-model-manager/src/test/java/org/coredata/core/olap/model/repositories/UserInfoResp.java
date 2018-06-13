package org.coredata.core.olap.model.repositories;

import org.coredata.core.olap.model.entities.UserInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserInfoResp extends ElasticsearchRepository<UserInfo, String> {

}
