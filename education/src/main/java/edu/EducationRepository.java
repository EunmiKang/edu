package edu;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="educations", path="educations")
public interface EducationRepository extends PagingAndSortingRepository<Education, Long>{
    Education findByAppId(Long appId);

}
