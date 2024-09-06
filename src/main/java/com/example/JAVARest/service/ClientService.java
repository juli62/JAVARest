package com.example.JAVARest.service;

import com.example.JAVARest.dtos.BookDTO;
import com.example.JAVARest.model.Book;
import com.example.JAVARest.model.Client;
import com.example.JAVARest.repo.ClientRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

    @Autowired
    private ClientRepo clientRepo;

    public Client getClientById(Long id) {
        return clientRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found: " + id));
    }

    public Client addBookToClient(Long clientId, BookDTO bookDTO) {

        Client client = clientRepo.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        Book book = new Book();
        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor_name());
        book.setIsbn(bookDTO.getIsbn());

        client.addBook(book);

        return clientRepo.save(client);
    }
}