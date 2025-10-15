package com.github.kzhunmax.jobsearch.repository.es;

import com.github.kzhunmax.jobsearch.model.es.JobDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobSearchRepository extends ElasticsearchRepository<JobDocument, Long> {
}
