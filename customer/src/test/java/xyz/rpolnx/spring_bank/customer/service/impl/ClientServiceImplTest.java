package xyz.rpolnx.spring_bank.customer.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.rpolnx.spring_bank.common.exceptions.BadRequestException;
import xyz.rpolnx.spring_bank.common.exceptions.ConflictException;
import xyz.rpolnx.spring_bank.common.exceptions.NotFoundException;
import xyz.rpolnx.spring_bank.common.model.dto.CustomerEvent;
import xyz.rpolnx.spring_bank.customer.external.ClientPublisher;
import xyz.rpolnx.spring_bank.customer.external.ClientRepository;
import xyz.rpolnx.spring_bank.common.model.dto.CustomerDTO;
import xyz.rpolnx.spring_bank.customer.model.entity.Client;
import xyz.rpolnx.spring_bank.customer.model.factory.CustomerEventFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static xyz.rpolnx.spring_bank.common.model.enums.EventType.*;
import static xyz.rpolnx.spring_bank.customer.mocks.ClientMock.generateClients;
import static xyz.rpolnx.spring_bank.common.model.enums.CustomPersonType.PF;
import static xyz.rpolnx.spring_bank.common.model.enums.CustomPersonType.PJ;

@ExtendWith(MockitoExtension.class)
public class ClientServiceImplTest {

    private static final int MAX_SCORE_NUMBER = 10;

    @Mock
    private ClientRepository repository;
    @Mock
    private ClientPublisher publisher;

    @InjectMocks
    private ClientServiceImpl service;

    @Test
    @DisplayName("When database has clients, should return them all")
    public void shouldGetAllClients() {
        List<Client> clients = generateClients(15, MAX_SCORE_NUMBER);
        when(repository.findAll()).thenReturn(clients);

        List<CustomerDTO> actualClients = service.getAll();

        assertFalse(actualClients.isEmpty());
        assertEquals("Name #13", actualClients.get(13).getFullName());
        assertEquals("6", actualClients.get(6).getDocumentNumber());
    }

    @Test
    @DisplayName("When database has no clients, should return empty list")
    public void shouldGetAllClientsWithEmptyList() {
        List<Client> clients = generateClients(15, MAX_SCORE_NUMBER);
        when(repository.findAll()).thenReturn(new ArrayList<>());

        List<CustomerDTO> actualClients = service.getAll();

        assertTrue(actualClients.isEmpty());
    }

    @Test
    @DisplayName("When getting client by id, should return it")
    public void shouldGetSingleClient() {
        String documentNumber = "1234";

        Client client = new Client(documentNumber, "", PF, 8);
        when(repository.findById(anyString())).thenReturn(Optional.of(client));

        CustomerDTO actualClients = assertDoesNotThrow(() -> service.get(documentNumber));

        assertNotNull(actualClients);
        assertEquals("1234", actualClients.getDocumentNumber());
    }

    @Test
    @DisplayName("When getting client and it does not exist, should throw error")
    public void shouldThrowErrorWhenClientNotFound() {
        when(repository.findById(anyString())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.get("no-client"));

        assertTrue(exception.getMessage().contains("Client not found"));
    }

    @Test
    @DisplayName("When updating an existing client, should edit it")
    public void shouldUpdateExistingClient() {
        String documentNumber = "currentEdit";

        Client oldClient = new Client("currentEdit", "old name", PF, 2);

        when(repository.findById("currentEdit")).thenReturn(Optional.of(oldClient));

        Client newClient = new Client("currentEdit", "edit", PJ, 4);

        when(repository.save(oldClient)).thenReturn(newClient);

        CustomerDTO request = new CustomerDTO("01234567891012", "edit", PJ, 4);

        assertDoesNotThrow(() -> service.update(request, documentNumber));

        CustomerEvent event = CustomerEventFactory.generateCustomer(newClient, UPDATE);

        verify(repository, times(1)).save(newClient);
        verify(publisher).handleClientEvent(event);
    }

    @Test
    @DisplayName("When updating with unused document number, should create it")
    public void shouldUpdateWhenThereIsNoReferenceWithThisDocumentNumber() {
        String documentNumber = "98765432101";

        CustomerDTO request = new CustomerDTO("12345678910", "new user", PF, 8);
        Client client = new Client("98765432101", "new user", PF, 8);

        when(repository.findById(documentNumber)).thenReturn(Optional.empty());
        when(repository.save(client)).thenReturn(client);

        assertDoesNotThrow(() -> service.update(request, documentNumber));

        CustomerEvent event = CustomerEventFactory.generateCustomer(client, UPDATE);

        verify(repository, times(1)).save(client);
        verify(publisher).handleClientEvent(event);
    }

    @Test
    @DisplayName("When deleting client, should not throw exception")
    public void shouldDeleteClient() {
        String documentNumber = "1234";
        Client client = new Client().withDocumentNumber(documentNumber);
        CustomerEvent event = CustomerEventFactory.generateCustomer(client, DELETE);

        assertDoesNotThrow(() -> service.delete(documentNumber));
        verify(repository, times(1)).delete(client);
        verify(publisher, times(1)).handleClientEvent(event);
    }

    @Test
    @DisplayName("When creating PF client, should returns your id successfully")
    public void shouldCreatePFClient() {
        String documentNumber = "12345678910";
        CustomerDTO request = new CustomerDTO(documentNumber, "Creating test", PF);
        Client created = new Client(documentNumber, "Creating test", PF, 7);
        CustomerEvent event = CustomerEventFactory.generateCustomer(created, CREATION);

        when(repository.findById(documentNumber)).thenReturn(Optional.empty());
        when(repository.save(any(Client.class))).thenReturn(created);

        CustomerDTO createdClient = assertDoesNotThrow(() -> service.create(request));

        assertEquals(documentNumber, createdClient.getDocumentNumber());
        assertNull(createdClient.getFullName());
        assertNull(createdClient.getPersonType());
        assertNull(createdClient.getScore());

        verify(publisher, times(1)).handleClientEvent(event);
    }

    @Test
    @DisplayName("When creating PJ client, should returns your id successfully")
    public void shouldCreatePJClient() {
        String documentNumber = "12345678910123";

        CustomerDTO request = new CustomerDTO(documentNumber, "Creating test", PJ);
        Client created = new Client(documentNumber, "Creating test", PJ, 6);
        CustomerEvent event = CustomerEventFactory.generateCustomer(created, CREATION);

        when(repository.findById(documentNumber)).thenReturn(Optional.empty());
        when(repository.save(any(Client.class))).thenReturn(created);

        CustomerDTO createdClient = assertDoesNotThrow(() -> service.create(request));

        assertEquals(documentNumber, createdClient.getDocumentNumber());
        assertNull(createdClient.getFullName());
        assertNull(createdClient.getPersonType());
        assertNull(createdClient.getScore());

        verify(publisher, times(1)).handleClientEvent(event);
    }

    @Test
    @DisplayName("When creating PF with wrong document number length, should throw exception")
    public void shouldThrowExceptionWhenCreatingPFWithWrongSize() {
        String documentNumberPf = "wrong-pf";
        String documentNumberPj = "wrong-pj";

        CustomerDTO requestPf = new CustomerDTO(documentNumberPf, "Creating test PF", PF);
        CustomerDTO requestPj = new CustomerDTO(documentNumberPj, "Creating test PJ", PJ);

        BadRequestException exceptionPf = assertThrows(BadRequestException.class, () -> service.create(requestPf));
        BadRequestException exceptionPj = assertThrows(BadRequestException.class, () -> service.create(requestPj));

        assertTrue(exceptionPf.getMessage().contains(String.format("must has document size of %d", PF.getDocumentSize())));
        assertTrue(exceptionPj.getMessage().contains(String.format("must has document size of %d", PJ.getDocumentSize())));
    }

    @Test
    @DisplayName("When creating a client with an existing document number, should throw exception")
    public void shouldThrowExceptionWhenUsingExistingDocumentNumber() {
        String documentNumberPf = "12345678910";

        CustomerDTO request = new CustomerDTO(documentNumberPf, "Creating test PF", PF);
        when(repository.findById(anyString())).thenReturn(Optional.of(new Client()));

        ConflictException exception = assertThrows(ConflictException.class, () -> service.create(request));

        assertTrue(exception.getMessage().contains("Duplicated client"));
    }
}
