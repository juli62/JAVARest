package com.example.JAVARest.unitTest;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.JAVARest.dtos.BookDTO;
import com.example.JAVARest.service.BookService;

public class BookServiceTest {

    @InjectMocks
    private BookService bookService;

    @Mock
    private Client mockClient;

    @Mock
    private WebTarget mockWebTarget;

    @Mock
    private Invocation.Builder mockInvocationBuilder;

    @Mock
    private Response mockResponse;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFetchBook_Success() {

        String jsonResponse = "{ \"docs\": [{ \"title\": \"Book\", \"author_name\": [\"Author\"], \"isbn\": [\"1234567890123\"] }] }";

        when(mockClient.target(anyString())).thenReturn(mockWebTarget);
        when(mockWebTarget.request(anyString())).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResponse.readEntity(String.class)).thenReturn(jsonResponse);

        BookDTO book = bookService.fetchBook("Test");

        assertNotNull(book);
        assertEquals("Book", book.getTitle());
        assertEquals("Author", book.getAuthor_name());
        assertEquals("1234567890123", book.getIsbn());
    }

    @Test
    public void testFetchBook_NoBooksFound() {

        String emptyResponse = "{ \"docs\": [] }";

        when(mockClient.target(anyString())).thenReturn(mockWebTarget);
        when(mockWebTarget.request(anyString())).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResponse.readEntity(String.class)).thenReturn(emptyResponse);

        BookDTO book = bookService.fetchBook("Test");

        assertNull(book);
    }

    @Test
    public void testFetchBook_ErrorResponse() {

        when(mockClient.target(anyString())).thenReturn(mockWebTarget);
        when(mockWebTarget.request(anyString())).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(500);

        BookDTO book = bookService.fetchBook("Test");

        assertNull(book);
    }
}
