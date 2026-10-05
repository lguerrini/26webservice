package com.example.webservice.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IotDataRepository extends JpaRepository<IotData, Integer> {
    Page<IotData> findAllByIdIot(Integer idIot, Pageable pageable);
}