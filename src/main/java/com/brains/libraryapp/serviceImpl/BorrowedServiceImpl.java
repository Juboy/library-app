package com.brains.libraryapp.serviceImpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.brains.libraryapp.models.Book;
import com.brains.libraryapp.models.Borrowed;
import com.brains.libraryapp.models.User;
import com.brains.libraryapp.repositories.BookRepository;
import com.brains.libraryapp.repositories.BorrowedRepository;

import com.brains.libraryapp.services.BorrowedService;


@Service(value="borrowedService")
public class BorrowedServiceImpl implements BorrowedService{

	@Autowired
	private BorrowedRepository borrowedRepository;
	
	@Autowired
	private BookRepository bookRepository;
	
	@Override
	public boolean hasUserBorrowedBook(User user, Book book) {
		Borrowed b = borrowedRepository.getBookBorrorwedByUser(book.getId(), user.getId());
		if(b!=null) {
			return true;
		}
		return false;
	}

	@Override
	public Borrowed borrowBook(User user, Book book) {
		book.setTotalNumber(book.getTotalNumber()-1);
		Book bk = bookRepository.save(book);
		return borrowedRepository.save(new Borrowed(bk.getId(), user.getId()));
	}

	@Override
	public List<Book> findBookByUser(User user) {
		List<Borrowed> b = borrowedRepository.findAllByUserId(user.getId());
		List<Book> allBooks = new ArrayList<Book>();
		for(Borrowed borrowed: b) {
			allBooks.add(bookRepository.findOneById(borrowed.getBookId()));
		}
		return allBooks;
	}

	@Override
	public boolean deleteBorrowed(User user, Book book) {
		book.setTotalNumber(book.getTotalNumber()+1);
		bookRepository.save(book);
		borrowedRepository.deleteOneByUserIdAndBookId(user.getId(), book.getId());
		return true;
	}

	@Override
	public boolean isBookCheckedOut(Book book) {
		Borrowed b = borrowedRepository.findOneByBookId(book.getId());
		if(b!=null) {
			return true;
		}
		return false;
	}

	@Override
	public int countByBook(Book book) {
		// TODO Auto-generated method stub
		return borrowedRepository.countByBookId(book.getId());
	}

	@Override
	public int countByUser(User user) {
		// TODO Auto-generated method stub
		return borrowedRepository.countByUserId(user.getId());
	}


}
