package com.brains.libraryapp.controllers;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.brains.libraryapp.TokenInfo;
import com.brains.libraryapp.models.Book;
import com.brains.libraryapp.models.Borrowed;
import com.brains.libraryapp.models.Log;
import com.brains.libraryapp.services.BookService;
import com.brains.libraryapp.services.BorrowedService;
import com.brains.libraryapp.services.LogService;
import com.brains.libraryapp.services.UserService;

@RequestMapping("/api")
@RestController
public class UserRestController {
	@Autowired
	private HttpSession session;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private BookService bookService;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private BorrowedService borrowedService;
	
	@RequestMapping(value="/all-books")
	public List<Book> viewBooks(){
		List<Book> myBooks = bookService.findAll();
		return myBooks;
	}
	
	@RequestMapping(value="/checkout/{id}")
	public ModelAndView checkBookOut(@PathVariable("id") int id, RedirectAttributes redir){
		TokenInfo info = (TokenInfo) session.getAttribute("toks");
		Book book = bookService.getBookById((long)id);
		ModelAndView m = new ModelAndView("redirect:/user/viewAllBooks?access_token="+info.getAccessToken());
		if(book.getTotalNumber()<=1) {
			redir.addFlashAttribute("error", "All copies of '"+book.getBookName()+"' have been checked out at the moment");
			return m;
		}
		if(borrowedService.hasUserBorrowedBook(userService.getLoggedInUser(), book)) {
			redir.addFlashAttribute("error", "You have already checked out this book. Kindly check ur checked out books");
			return m;
		}
		Borrowed added = borrowedService.borrowBook(userService.getLoggedInUser(), book);
		Log addedLog = logService.add(book, userService.getLoggedInUser(), "Check Out");
		if(added!=null) {
			if(addedLog!=null) {
				redir.addFlashAttribute("success", "You have successfully checked out this book");
				return m;
			}else {
				redir.addFlashAttribute("warning", "Book checked out successfully but couldn't be added to log");
				return m;
			}
		}
		redir.addFlashAttribute("error", "Could't Check out. Meet the libarian for further questions");
		return m;
		
	}

}