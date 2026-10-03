package com.example.webservice.iot;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ItemService {
    private final ItemRepository repository;

    public ItemService(ItemRepository repository) {
        this.repository = repository;
    }

    public List<Item> findAll() {
        return repository.findAll();
    }

    public Item findById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
    }

    public Item findIotByMacaddress(String macaddress) {
        return repository.findByMacaddress(macaddress)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "IoT not found"));
    }

    public Item create(ItemRequest request) {
        return repository.save(new Item(
            request.name(), request.description(), request.macaddress(), request.jsonrange()));
    }

    public Item update(Integer id, ItemRequest request) {
        Item item = findById(id);
        item.setName(request.name());
        item.setDescription(request.description());
        item.setMacaddress(request.macaddress());
        item.setJsonrange(request.jsonrange());
        item.setDaterev(LocalDateTime.now());
        return repository.save(item);
    }

    public void delete(Integer id) {
        Item item = findById(id);
        repository.delete(item);
    }
}