package com.brains.libraryapp.controllers;

import java.security.Principal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brains.libraryapp.models.Book;
import com.brains.libraryapp.models.Borrowed;
import com.brains.libraryapp.models.Log;
import com.brains.libraryapp.models.Role;
import com.brains.libraryapp.models.User;
import com.brains.libraryapp.services.BookService;
import com.brains.libraryapp.services.BorrowedService;
import com.brains.libraryapp.services.LogService;
import com.brains.libraryapp.services.RoleService;
import com.brains.libraryapp.services.UserService;

@RequestMapping("/api")
@RestController
public class UserRestController {
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private BookService bookService;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private BorrowedService borrowedService;
	
	@Autowired
	private TokenStore tokenStore;
	
	@InitBinder
	protected void binder(WebDataBinder bind) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		bind.registerCustomEditor(java.util.Date.class, "dob", new CustomDateEditor(format, false));
		bind.registerCustomEditor(java.util.Date.class, "from", new CustomDateEditor(format, false));
		bind.registerCustomEditor(java.util.Date.class, "to", new CustomDateEditor(format, false));

//		bind.registerCustomEditor(String.class, "bookName", new TitlePropertyEditor());
		
	}
	
	@RequestMapping(value="/all-books")
	public ResponseEntity<List<Book>> viewBooks(){
		List<Book> myBooks = bookService.findAll();
		return new ResponseEntity<List<Book>>(myBooks, HttpStatus.OK);
	}
	
	@RequestMapping(value="/isCheckedout/{id}")
	public ResponseEntity<String> isBookCheckedOut(@PathVariable("id") int id, Principal principal){
		Book book = bookService.getBookById((long)id);
		if(book == null) {
			return new ResponseEntity<String>("Book does not exist", HttpStatus.NOT_FOUND);
		}
		if(borrowedService.hasUserBorrowedBook(userService.getUserByPrincipal(principal), book)) {
			return new ResponseEntity<String>("true", HttpStatus.OK);
		}
		return new ResponseEntity<String>("false", HttpStatus.OK);
	}
	
	@RequestMapping(value="/checkout/{id}")
	public ResponseEntity<String> checkBookOut(@PathVariable("id") int id, Principal principal){
		Book book = bookService.getBookById((long)id);
		if(book == null) {
			return new ResponseEntity<String>("Book does not exist", HttpStatus.NOT_FOUND);
		}
		if(book.getTotalNumber()<=1) {
			return new ResponseEntity<String>("All copies of '"+book.getBookName()+"' have been checked out at the moment", HttpStatus.FORBIDDEN);
		}
		if(borrowedService.hasUserBorrowedBook(userService.getUserByPrincipal(principal), book)) {
			return new ResponseEntity<String>("You have already checked out this book. Kindly check ur checked out books", HttpStatus.ALREADY_REPORTED);
		}
		Borrowed added = borrowedService.borrowBook(userService.getUserByPrincipal(principal), book);
		Log addedLog = logService.add(book, userService.getUserByPrincipal(principal), "Check Out");
		if(added!=null) {
			if(addedLog!=null) {
				return new ResponseEntity<String>("You have successfully checked out this book", HttpStatus.CREATED);
			}else {
				return new ResponseEntity<String>("Book checked out successfully but couldn't be added to log", HttpStatus.ACCEPTED);
			}
		}
		return new ResponseEntity<String>("Could't Check out. Meet the libarian for further questions", HttpStatus.BAD_REQUEST);	
	}
	
	@RequestMapping(value="/checkin/{id}")
	public ResponseEntity<String> checkBookin(@PathVariable("id") int id,Principal principal){
		Book book = bookService.getBookById((long)id);
		if(book == null) {
			return new ResponseEntity<String>("Book does not exist", HttpStatus.NOT_FOUND);
		}
		if(!borrowedService.hasUserBorrowedBook(userService.getUserByPrincipal(principal), book)) {
			return new ResponseEntity<String>("This book is not in the list of your checked out book", HttpStatus.OK);
		}
		boolean removed = borrowedService.deleteBorrowed(userService.getUserByPrincipal(principal), book);
		Log addedLog = logService.add(book, userService.getUserByPrincipal(principal), "Check In");
		if(removed) {
			if(addedLog!=null) {
				return new ResponseEntity<String>("You have successfully checked in this book", HttpStatus.CREATED);
			}else {
				return new ResponseEntity<String>("Book checked in successfully but couldn't be added to log", HttpStatus.ACCEPTED);
			}
			
		}
		return new ResponseEntity<String>("Could't Check in. Meet the libarian for further questions", HttpStatus.BAD_REQUEST);
	}
	
	@RequestMapping(value="/register",  method = RequestMethod.POST)
	public ResponseEntity<String> register(@Valid User user, BindingResult result) {
		if(user==null) {
			return new ResponseEntity<>("Invalid Details, All fields are required", HttpStatus.BAD_REQUEST);
		}
		if(result.hasErrors()) {
			return new ResponseEntity<>("Error in "+result.getFieldError().getField(), HttpStatus.BAD_REQUEST);
		}
		user.setUsername(user.getUsername().trim());
		user.setNonLocked(true);
		user.setEnabled(true);
		user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
		if(userService.userExists(user.getUsername())) {
			return new ResponseEntity<>("Username already taken", HttpStatus.CONFLICT);
		}
		if(user.getUsername().contains(" ")) {
			return new ResponseEntity<>("Username should not contain spaces", HttpStatus.BAD_REQUEST);
		}
		Set<Role> roles = new HashSet<Role>();
		roles.add(roleService.getUserRole());
		user.setRoles(roles);
		User added = userService.save(user);
		if(added!=null) {
			return new ResponseEntity<>("Successfull", HttpStatus.CREATED);
		}
		return new ResponseEntity<>("Failed", HttpStatus.BAD_REQUEST);
	}
	
	@RequestMapping(value="/borrowed")
	public ResponseEntity<List<Book>> viewBorrowedBooks(Principal principal) {
		List<Book> myBooks = borrowedService.findBookByUser(userService.getUserByPrincipal(principal));
		return new ResponseEntity<List<Book>>(myBooks, HttpStatus.OK);
	}
	
	@RequestMapping(value="/log")
	public ResponseEntity<List<Log>> viewLog(Principal principal) {
		List<Log> log = logService.getAllByUser(userService.getUserByPrincipal(principal));
		return new ResponseEntity<List<Log>>(log, HttpStatus.OK);
	}
	
	@RequestMapping(value="/logTimestamp", method=RequestMethod.POST)
	public ResponseEntity<List<Log>> logTimestamp(@RequestParam("from") Date from, @RequestParam("to") Date to, Principal principal) {
		User user = userService.getUserByPrincipal(principal);
		System.out.println(user.toString());
		System.out.println(from);
		System.out.println(to);
		List<Log> logs = logService.getAllByUserWithinTime(user, from, to);
		
		return new ResponseEntity<List<Log>>(logs,HttpStatus.OK); 
	}
	
	@RequestMapping(value="/log-out")
	public ResponseEntity<String> logout(@RequestParam("access_token") String token) {
		try {
			OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(token);
			if (oAuth2AccessToken != null) {
				OAuth2RefreshToken oAuth2RefreshToken = oAuth2AccessToken.getRefreshToken();
				tokenStore.removeAccessToken(oAuth2AccessToken);
				if (oAuth2RefreshToken != null) {
					tokenStore.removeRefreshToken(oAuth2RefreshToken);
					return new ResponseEntity<String>("Success", HttpStatus.OK);
				}
			}
			return new ResponseEntity<String>("Invalid accessToken", HttpStatus.OK);

		} catch (Exception e) {
			System.out.println("Error while logging out because: " + e.getMessage());
		}
		return new ResponseEntity<String>("Logout failed", HttpStatus.OK);
	}
}