package com.brains.libraryapp;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.brains.libraryapp.models.Book;
import com.brains.libraryapp.repositories.BookRepository;


@RunWith(SpringRunner.class)
@DataJpaTest
public class BooksRestControllerTest {

	
	@Autowired
	private BookRepository bookRepo;
	
	@Test
	public void findAllBooks() {
		Book book = new Book("Max", "Juwon", (long)1234, 1);
		
		bookRepo.save(book);
		
		Book found = bookRepo.findOneByIsbn((long)1234);
		
		assertThat(found.getBookName()).isEqualTo(book.getBookName());
	}

}
