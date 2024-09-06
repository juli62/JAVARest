package com.example.JAVARest.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.example.JAVARest.dtos.BookDTO;
import com.example.JAVARest.model.Client;
import com.example.JAVARest.repo.ClientRepo;
import com.example.JAVARest.service.BookService;
import com.example.JAVARest.service.ClientService;

import jakarta.inject.Inject;

@Path("/clients")
public class ClientController {

    @Inject
    private ClientRepo clientRepo;

    @Inject
    private BookService bookService;

    @Autowired
    private ClientService clientService;

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

        BookDTO book = bookService.fetchBook(title);

        clientService.addBookToClient(id, book);

        return Response.ok(book).build();
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
