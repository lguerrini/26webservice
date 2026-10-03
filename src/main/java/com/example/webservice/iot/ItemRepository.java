package com.example.webservice.iot;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Integer> {
	Optional<Item> findByMacaddress(String macaddress);
}