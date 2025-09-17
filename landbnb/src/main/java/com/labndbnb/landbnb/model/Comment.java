package com.labndbnb.landbnb.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
public class Comment {

    @Id
    private Long id;

    private Integer rating;

    private String content;

    private LocalDate createdAt;


}
