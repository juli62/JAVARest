package com.example.JAVARest.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.example.JAVARest.DTO.BookDTO;
import com.example.JAVARest.DTO.ClientDTO;
import com.example.JAVARest.model.Book;
import com.example.JAVARest.model.Client;
import com.example.JAVARest.repo.ClientRepo;
import com.example.JAVARest.service.BookService;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@Path("/clients")
public class ClientController {

    @Inject
    private ClientRepo clientRepo;

    @Inject
    private BookService bookService;

    @GET
    @Path("/getAllClients")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllClients() {
        try {
            List<Client> clientList = new ArrayList<>();
            clientRepo.findAll().forEach(clientList::add);

            if (clientList.isEmpty()) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }

            return Response.ok(clientList).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClientById(@PathParam("id") Long id) {
        try {
            Optional<Client> clientData = clientRepo.findById(id);
            if (clientData.isPresent()) {
                return Response.ok(clientData.get()).build();
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/addClient")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addClient(Client client) {
        try {
            Client clientObj = clientRepo.save(client);
            return Response.ok(clientObj).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/{id}/addBook")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addBooksToClient(@PathParam("id") Long id, @QueryParam("title") String title) {
        try {
            Optional<Client> clientData = clientRepo.findById(id);
            if (!clientData.isPresent()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            Client client = clientData.get();

            List<BookDTO> fetchedBooks = bookService.fetchBooks(title);

            List<Book> clientBooks = client.getBooks();
            if (clientBooks == null) {
                clientBooks = new ArrayList<>();
            }

            if (fetchedBooks != null) {
                for (BookDTO bookDTO : fetchedBooks) {
                    if (bookDTO != null) {

                        Book book = new Book();
                        book.setTitle(bookDTO.getTitle());
                        book.setAuthor(bookDTO.getAuthor());
                        book.setIsValidISBN(bookDTO.getIsbn());

                        clientBooks.add(book);
                    }
                }
            }

            client.setBooks(clientBooks);
            clientRepo.save(client);

            ClientDTO clientDTO = new ClientDTO();
            clientDTO.setId(client.getId());
            clientDTO.setName(client.getName());
            clientDTO.setBooks(convertBooksToDTOs(client.getBooks()));

            return Response.ok(clientDTO).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private List<BookDTO> convertBooksToDTOs(List<Book> books) {
        List<BookDTO> bookDTOs = new ArrayList<>();
        for (Book book : books) {
            BookDTO dto = new BookDTO();
            dto.setTitle(book.getTitle());
            dto.setAuthor(book.getAuthor());
            dto.setIsbn(book.getIsValidISBN());
            bookDTOs.add(dto);
        }
        return bookDTOs;
    }

    @PUT
    @Path("/updateClientById/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateClientById(@PathParam("id") Long id, Client newClientData) {
        try {
            Optional<Client> clientData = clientRepo.findById(id);

            if (clientData.isPresent()) {
                Client updatedClient = clientData.get();
                updatedClient.setName(newClientData.getName());
                updatedClient.setBooks(newClientData.getBooks());

                Client clientObj = clientRepo.save(updatedClient);
                return Response.ok(clientObj).build();
            }

            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DELETE
    @Path("/deleteClientById/{id}")
    public Response deleteClientById(@PathParam("id") Long id) {
        try {
            clientRepo.deleteById(id);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
