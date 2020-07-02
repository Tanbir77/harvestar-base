package io.naztech.jobharvestar.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import io.naztech.jobharvestar.model.PropertyDetails;

public interface PropertyDetailsRepo extends JpaRepository<PropertyDetails, Long> {
}
