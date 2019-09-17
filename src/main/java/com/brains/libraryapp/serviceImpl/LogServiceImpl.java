package com.brains.libraryapp.serviceImpl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.brains.libraryapp.models.Book;
import com.brains.libraryapp.models.Log;
import com.brains.libraryapp.models.User;
import com.brains.libraryapp.repositories.LogRepository;
import com.brains.libraryapp.services.LogService;

@Service(value="logService")
public class LogServiceImpl implements LogService{
	@Autowired
	private LogRepository logRepository;
	
	@Override
	public Log add(Book book, User user, String action) {
		Log log = new Log(book, user, action);
		return logRepository.save(log);
	}
	
	@Override
	public List<Log> getAllByUser(User user) {
		return logRepository.findAllByUserOrderByTimestampDesc(user);
	}

	@Override
	public List<Log> getAllByBook(Book book) {
		return logRepository.findAllByBook(book);
	}

	@Override
	public List<Log> getAllByAction(String action) {
		return logRepository.findAllByAction(action);
	}

	@Override
	public List<Log> getAllWithinTime(Date startTime, Date endTime) {
		return logRepository.findAllByTimestampBetween(startTime, endTime);
	}

	@Override
	public List<Log> findAll() {
		List<Log> list = new ArrayList<>();
		logRepository.findAll().iterator().forEachRemaining(list::add);
		return list;
	}

	@Override
	public List<Log> getAllByUserWithinTime(User user, Date startDate, Date endDate) {
		return logRepository.findAllByTimestampBetweenAndUser(startDate, endDate, user);
	}
	
}
