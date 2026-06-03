package com.nhhoang.synexbackend.repository;

import com.nhhoang.synexbackend.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaRepository extends JpaRepository<Media, Long> {

    Optional<Media> findByPublicId(String publicId);

    Optional<Media> findByUrl(String url);
}