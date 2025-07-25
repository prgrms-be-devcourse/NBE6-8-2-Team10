package com.back.domain.files.files.repository;

import com.back.domain.files.files.entity.Files;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FilesRepository extends JpaRepository<Files, Long> {
    List<Files> findByPostIdOrderBySortOrderAsc(Long postId);

}
