package com.example.JAVARest.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ClientDTO {
    private Long id;
    private String name;
    private List<BookDTO> books;

}
