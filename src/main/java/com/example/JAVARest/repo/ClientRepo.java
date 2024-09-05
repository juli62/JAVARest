package com.example.JAVARest.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.JAVARest.model.Client;

@Repository
public interface ClientRepo extends JpaRepository<Client, Long> {

}
