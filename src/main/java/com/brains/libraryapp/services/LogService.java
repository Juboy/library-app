package com.brains.libraryapp.services;

import java.sql.Date;
import java.util.List;

import com.brains.libraryapp.models.Book;
import com.brains.libraryapp.models.Log;
import com.brains.libraryapp.models.User;

public interface LogService {
	Log add(Book book, User user, String action);
	List<Log> getAllByUser(User user);
	List<Log> getAllByBook(Book book);
	List<Log> getAllByAction(String action);
	List<Log> findAll();
	List<Log> getAllWithinTime(Date startTime, Date endTime);
	
}
