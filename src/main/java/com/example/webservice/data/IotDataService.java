package com.example.webservice.data;

import java.util.List;

import com.example.webservice.iot.ItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class IotDataService {
    private static final Pageable LATEST_DATA = PageRequest.of(
            0, 100, Sort.by(Sort.Direction.DESC, "id"));

    private final IotDataRepository repository;
    private final ItemRepository iotRepository;

    public IotDataService(IotDataRepository repository, ItemRepository iotRepository) {
        this.repository = repository;
        this.iotRepository = iotRepository;
    }

    public List<IotData> findAll(Integer iotId) {
        if (iotId == null) {
            return repository.findAll(LATEST_DATA).getContent();
        }
        requireExistingIot(iotId);
        return repository.findAllByIdIot(iotId, LATEST_DATA).getContent();
    }

    public IotData findById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Data not found"));
    }

    public IotData create(IotDataRequest request) {
        requireExistingIot(request.idIot());
        return repository.save(new IotData(request.idIot(), request.jsondata()));
    }

    public IotData update(Integer id, IotDataRequest request) {
        IotData data = findById(id);
        requireExistingIot(request.idIot());
        data.setIdIot(request.idIot());
        data.setJsondata(request.jsondata());
        return repository.save(data);
    }

    public void delete(Integer id) {
        repository.delete(findById(id));
    }

    private void requireExistingIot(Integer iotId) {
        if (!iotRepository.existsById(iotId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "IoT not found");
        }
    }
}