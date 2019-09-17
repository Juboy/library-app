package com.brains.libraryapp.repositories;

import java.sql.Date;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.brains.libraryapp.models.Book;
import com.brains.libraryapp.models.Log;
import com.brains.libraryapp.models.User;

public interface LogRepository extends CrudRepository<Log, Long> {
	List<Log> findAllByUserOrderByTimestampDesc(User user);
	List<Log> findAllByBook(Book book);
	List<Log> findAllByAction(String action);
	List<Log> findAllByTimestampBetween(Date startDate, Date endDate);
	List<Log> findAllByTimestampBetweenAndUser(Date startDate, Date endDate, User user);
	
}
