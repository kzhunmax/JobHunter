package com.github.kzhunmax.jobsearch.service;

import com.github.kzhunmax.jobsearch.model.es.JobDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public List<JobDocument> searchJobs(String query, String location, String company) {
        var nativeQueryBuilder = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> {
                            // Full-text search on title and description
                            b.must(m -> m
                                    .multiMatch(mm -> mm
                                            .query(query)
                                            .fields("title", "description")
                                    )
                            );

                            // Filter by location if provided
                            if (location != null && !location.isBlank()) {
                                b.filter(f -> f.term(t -> t.field("location").value(location)));
                            }

                            // Filter by company if provided
                            if (company != null && !company.isBlank()) {
                                b.filter(f -> f.term(t -> t.field("company").value(company)));
                            }

                            // Always filter for active jobs
                            b.filter(f -> f.term(t -> t.field("active").value(true)));

                            return b;
                        })
                );

        Query esQuery = nativeQueryBuilder.build();
        SearchHits<JobDocument> searchHits = elasticsearchOperations.search(esQuery, JobDocument.class);

        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }
}
