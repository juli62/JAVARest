package com.example.JAVARest.model;

import java.util.List;

import java.util.ArrayList;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "Clients")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private List<String> books = new ArrayList<String>();

    public List<String> getBooks() {
        return books;
    }

    public void addBook(Book book) {
        if (books == null) {
            books = new ArrayList<String>();
        }
        this.books.add(book.getTitle() + "-" + book.getAuthor() + "  ISBN:" + book.getIsbn().toString()
                + " Is ValidISBN:" + String.valueOf(book.getValid()));
    }

}
