package com.brains.libraryapp.serviceImpl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.brains.libraryapp.models.Book;
import com.brains.libraryapp.repositories.BookRepository;
import com.brains.libraryapp.services.BookService;

@Service(value="bookService")
public class BookServiceImpl implements BookService{

	@Autowired
	private BookRepository bookRepository;
	
	@Override
	public Book save(Book book) {
		Book added = bookRepository.save(book);
		return added;
	}

	@Override
	public Book edit(Book book) {
		boolean isExist=bookRepository.existsById(book.getId());
		if(isExist) {
			return bookRepository.save(book);
		}
		return null;
	}

	@Override
	public boolean delete(Book book) {
		bookRepository.delete(book);
		return true;
	}

	@Override
	public Iterable<Book> findAll() {
		Iterable<Book> books = bookRepository.findAll();
		return books;
	}

	@Override
	public Book isIsbnExist(Long ISBN) {
		Book book=bookRepository.findOneByIsbn(ISBN);
		return book;
	}

	@Override
	public Book getBookById(Long Id) {
		return bookRepository.findOneById(Id);
	}
	
}
