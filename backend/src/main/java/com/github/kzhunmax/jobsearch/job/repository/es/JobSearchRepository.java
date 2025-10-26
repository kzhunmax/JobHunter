package com.github.kzhunmax.jobsearch.job.repository.es;

import com.github.kzhunmax.jobsearch.job.model.es.JobDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobSearchRepository extends ElasticsearchRepository<JobDocument, Long> {
}
