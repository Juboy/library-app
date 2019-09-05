package com.brains.libraryapp.services;

import java.util.List;

import com.brains.libraryapp.models.Book;

public interface BookService {
	Book save(Book book);
	Book edit(Book book);
	boolean delete(Book book);
	List<Book> findAll();
	Book isIsbnExist(Long ISBN);
	Book getBookById(Long Id);
}
