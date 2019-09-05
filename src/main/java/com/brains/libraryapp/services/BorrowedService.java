package com.brains.libraryapp.services;

import java.util.List;

import com.brains.libraryapp.models.Book;
import com.brains.libraryapp.models.Borrowed;
import com.brains.libraryapp.models.User;

public interface BorrowedService {
	boolean hasUserBorrowedBook(User user, Book book);
    Borrowed borrowBook(User user, Book book);
    List<Book> findBookByUser(User user);
    boolean deleteBorrowed(User user, Book book);
    boolean isBookCheckedOut(Book book);
    int countByBook(Book book);
    int countByUser(User user);
}
