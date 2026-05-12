package com.cinemax.backend.repository;

import com.cinemax.backend.model.entity.SaleSnackDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleSnackDetailRepository extends JpaRepository<SaleSnackDetail,Integer> {
}
