package com.example.webservice.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IotDataRepository extends JpaRepository<IotData, Integer> {
    List<IotData> findAllByIdIot(Integer idIot);
}