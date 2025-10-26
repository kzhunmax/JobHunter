package com.github.kzhunmax.jobsearch.job.service.search;

import com.github.kzhunmax.jobsearch.job.model.es.JobDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public PagedModel<EntityModel<JobDocument>> searchJobs(String query, String location, String company, Pageable pageable, PagedResourcesAssembler<JobDocument> pagedAssembler) {
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
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> pagedAssembler.toModel(
                                new PageImpl<>(list, pageable, searchHits.getTotalHits())
                        )
                ));
    }
}
