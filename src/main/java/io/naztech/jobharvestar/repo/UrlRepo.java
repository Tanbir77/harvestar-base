package io.naztech.jobharvestar.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.naztech.jobharvestar.model.Url;

public interface UrlRepo extends JpaRepository<Url, Long> {
	List<Url> findAllByIsScrapped(boolean isScrapped);
}
