package com.example.webservice.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.example.webservice.iot.ItemRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

class IotDataServiceTest {
    @Test
    void listsAtMostOneHundredRowsOrderedByDescendingId() {
        IotDataRepository repository = mock(IotDataRepository.class);
        ItemRepository iotRepository = mock(ItemRepository.class);
        when(repository.findAll(org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        IotDataService service = new IotDataService(repository, iotRepository);

        service.findAll(null);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(pageableCaptor.capture());
        assertPageable(pageableCaptor.getValue());
    }

    @Test
    void appliesSameLimitAndOrderWhenFilteringByIot() {
        IotDataRepository repository = mock(IotDataRepository.class);
        ItemRepository iotRepository = mock(ItemRepository.class);
        when(iotRepository.existsById(7)).thenReturn(true);
        when(repository.findAllByIdIot(org.mockito.ArgumentMatchers.eq(7),
                org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        IotDataService service = new IotDataService(repository, iotRepository);

        service.findAll(7);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAllByIdIot(org.mockito.ArgumentMatchers.eq(7), pageableCaptor.capture());
        assertPageable(pageableCaptor.getValue());
    }

    @Test
    private void assertPageable(Pageable pageable) {
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(100);
        assertThat(pageable.getSort().getOrderFor("id").getDirection().name()).isEqualTo("DESC");
    }
}