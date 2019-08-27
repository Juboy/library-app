package com.brains.libraryapp.repositories;

import org.springframework.data.repository.CrudRepository;

import com.brains.libraryapp.models.Book;

public interface BookRepository extends CrudRepository<Book, Long> {
	Book findOneByIsbn(Long Isbn);
	Book findOneById(Long Id);
}
