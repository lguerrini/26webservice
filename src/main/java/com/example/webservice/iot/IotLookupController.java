package com.example.webservice.iot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/iot")
public class IotLookupController {
    private final ItemService service;

    public IotLookupController(ItemService service) {
        this.service = service;
    }

    @GetMapping
    public Item findIotByMacaddress(@RequestParam String macaddress) {
        return service.findIotByMacaddress(macaddress);
    }
}