package com.app.incidentManagement.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.app.incidentManagement.entity.Environment;
import com.app.incidentManagement.entity.Incident;
import com.app.incidentManagement.entity.IncidentStatus;
import com.app.incidentManagement.entity.Priority;
import com.app.incidentManagement.entity.Severity;

import jakarta.persistence.criteria.Predicate;

public class IncidentSpecification {

    public static Specification<Incident> filterIncidents(
            String search,
            IncidentStatus status, Severity severity,
            Priority priority,
            Environment environment) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(
            	    criteriaBuilder.equal(
            	        root.get("deleted"),
            	        false
            	    )
            	);
            if (search != null && !search.trim().isEmpty()) {

                String searchValue =
                        "%" + search.trim().toLowerCase() + "%";
                Predicate title =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("title")
                                ),
                                searchValue
                        );
                Predicate serviceName =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("serviceName")
                                ),
                                searchValue
                        );

                Predicate description =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("description")
                                ),
                                searchValue
                        );

                predicates.add(
                        criteriaBuilder.or(
                        		title,
                                serviceName,
                                description
                        )
                );
            }

            if (status != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("status"),
                                status
                        )
                );
            }
            if (severity != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("severity"),
                                severity
                        )
                );
            }

            // PRIORITY
            if (priority != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("priority"),
                                priority
                        )
                );
            }
            if (environment != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("environment"),
                                environment
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        
        };
        
}
}
