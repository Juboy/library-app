package com.brains.libraryapp.services;

import com.brains.libraryapp.models.Book;

public interface BookService {
	Book save(Book book);
	Book edit(Book book);
	boolean delete(Book book);
	Iterable<Book> findAll();
	Book isIsbnExist(Long ISBN);
	Book getBookById(Long Id);
}
