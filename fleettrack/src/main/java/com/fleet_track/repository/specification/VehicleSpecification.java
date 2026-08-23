package com.fleet_track.repository.specification;

import com.fleet_track.entity.VehicleEntity;
import com.fleet_track.enums.VehicleStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VehicleSpecification {

    private VehicleSpecification() {}

    public static Specification<VehicleEntity> withFilters(VehicleStatus status,
                                                           Integer year,
                                                           UUID assignedDriverId,
                                                           String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (year != null) {
                predicates.add(cb.equal(root.get("year"), year));
            }
            if (assignedDriverId != null) {
                predicates.add(cb.equal(root.get("assignedDriver").get("id"), assignedDriverId));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("make")), pattern),
                        cb.like(cb.lower(root.get("model")), pattern),
                        cb.like(cb.lower(root.get("licensePlate")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}