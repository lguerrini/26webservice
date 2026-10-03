package com.example.webservice.data;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/iot/{iotId}/data")
public class IotDataByIotController {
    private final IotDataService service;

    public IotDataByIotController(IotDataService service) {
        this.service = service;
    }

    @GetMapping
    public List<IotData> findAllByIot(@PathVariable Integer iotId) {
        return service.findAll(iotId);
    }
}