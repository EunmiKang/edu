package edu;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="eduApplications", path="eduApplications")
public interface EduApplicationRepository extends PagingAndSortingRepository<EduApplication, Long>{
    EduApplication findByAppId(Long appId);

}
