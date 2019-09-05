package com.brains.libraryapp.controllers;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.brains.libraryapp.TokenInfo;
import com.brains.libraryapp.models.Book;
import com.brains.libraryapp.models.Borrowed;
import com.brains.libraryapp.models.Log;
import com.brains.libraryapp.models.User;
import com.brains.libraryapp.services.BookService;
import com.brains.libraryapp.services.BorrowedService;
import com.brains.libraryapp.services.LogService;
import com.brains.libraryapp.services.UserService;

@RequestMapping("/user")
@Controller
public class UserController {
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
	
	@RequestMapping("/dashboard")
	public ModelAndView userDashboard() {
		List<Book> books = bookService.findAll();
		ModelAndView m = new ModelAndView("userDashboard");
		m.addObject("allBooks", books);
		return m;
	}

	@ModelAttribute
	public void addCommonObjects(Model model1) {
		if(session.getAttribute("toks")!=null) {
			Long id = userService.getUserId();
			User user = userService.findOneUser(id);
			TokenInfo info = (TokenInfo) session.getAttribute("toks");
			model1.addAttribute("sess", info);
			model1.addAttribute("user", user);
		}
	}
	
	@RequestMapping(value="/viewAllBooks")
	public ModelAndView viewBooks(){
		List<Book> myBooks = bookService.findAll();
		ModelAndView modelandview = new ModelAndView("userViewBooks");
		modelandview.addObject("allBooks", myBooks);
		return modelandview;
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
	
	@RequestMapping(value="/borrowed")
	public ModelAndView viewBorrowedBooks(Borrowed books) {
		List<Book> myBooks = borrowedService.findBookByUser(userService.getLoggedInUser());
		ModelAndView modelandview = new ModelAndView("userViewBorrowedBooks");
		modelandview.addObject("allBooks", myBooks);
		return modelandview;
		
	}
	
	@RequestMapping(value="/checkin/{id}")
	public ModelAndView checkBookin(@PathVariable("id") int id, RedirectAttributes redir){
		TokenInfo info = (TokenInfo) session.getAttribute("toks");
		Book book = bookService.getBookById((long)id);
		ModelAndView m = new ModelAndView("redirect:/user/borrowed?access_token="+info.getAccessToken());
		if(!borrowedService.hasUserBorrowedBook(userService.getLoggedInUser(), book)) {
			redir.addFlashAttribute("error", "This book is not in the list of your checked out book");
			return m;
		}
		boolean removed = borrowedService.deleteBorrowed(userService.getLoggedInUser(), book);
		Log addedLog = logService.add(book, userService.getLoggedInUser(), "Check In");
		if(removed) {
			if(addedLog!=null) {
				redir.addFlashAttribute("success", "You have successfully checked in this book");
				return m;
			}else {
				redir.addFlashAttribute("warning", "Book checked in successfully but couldn't be added to log");
				return m;
			}
			
		}
		redir.addFlashAttribute("error", "Could't Check in. Meet the libarian for further questions");
		return m;
		
	}
	
	@RequestMapping(value="/log")
	public ModelAndView viewLog() {
		List<Log> log = logService.getAllByUser(userService.getLoggedInUser());
		System.out.println(log.toString());
		ModelAndView m =  new ModelAndView("userLog");
		m.addObject("log", log);
		return m;
	}
}

