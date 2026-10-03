package com.example.webservice.iot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ItemServiceTest {
    @Test
    void findsIotByMacaddress() {
        String macaddress = "12:23:34:45";
        Item expected = new Item("Sensor", null, macaddress, null);
        ItemRepository repository = mock(ItemRepository.class);
        when(repository.findByMacaddress(macaddress)).thenReturn(Optional.of(expected));
        ItemService service = new ItemService(repository);

        assertThat(service.findIotByMacaddress(macaddress)).isSameAs(expected);
    }

    @Test
    void returnsNotFoundWhenMacaddressDoesNotExist() {
        String macaddress = "12:23:34:45";
        ItemRepository repository = mock(ItemRepository.class);
        when(repository.findByMacaddress(macaddress)).thenReturn(Optional.empty());
        ItemService service = new ItemService(repository);

        assertThatThrownBy(() -> service.findIotByMacaddress(macaddress))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}