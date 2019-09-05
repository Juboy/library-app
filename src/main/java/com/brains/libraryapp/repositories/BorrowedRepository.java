package com.brains.libraryapp.repositories;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.brains.libraryapp.models.Borrowed;


public interface BorrowedRepository extends CrudRepository<Borrowed, Long>{
	
	@Query(nativeQuery=true,value="select * from borrowed where book_id = ?1 and user_id = ?2")
    Borrowed getBookBorrorwedByUser(Long book_id, Long user_id);
	
//	@Query(nativeQuery=true,value="select * from borrowed where user_id = ?1")
//	Iterable<Borrowed> getAllBooksBorrowedByuser(Long user_id);
	
	List<Borrowed> findAllByUserId(Long userId);
	
	Borrowed findOneByBookId(Long bookId);
	
	int countByBookId(Long bookId);
	int countByUserId(Long userId);
	
	@Transactional
	void deleteOneByUserIdAndBookId(Long userId, Long bookId);
}
