package com.brains.libraryapp.controllers;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.brains.libraryapp.TokenInfo;
import com.brains.libraryapp.models.Book;
import com.brains.libraryapp.models.Log;
import com.brains.libraryapp.models.Role;
import com.brains.libraryapp.models.User;
import com.brains.libraryapp.services.BookService;
import com.brains.libraryapp.services.BorrowedService;
import com.brains.libraryapp.services.LogService;
import com.brains.libraryapp.services.RoleService;
import com.brains.libraryapp.services.UserService;


@Controller
public class AdminController {
	@Autowired
	private HttpSession session;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private BookService bookService;
	
	@Autowired
	private BorrowedService borrowedService;
	
	@Autowired
	private LogService logService;
	
	@InitBinder
	protected void binder(WebDataBinder bind) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		bind.registerCustomEditor(java.util.Date.class, "dob", new CustomDateEditor(format, false));

//		bind.registerCustomEditor(String.class, "bookName", new TitlePropertyEditor());
		
	}
		
	@RequestMapping(value="/login")
	public ModelAndView loginPage() {
		ModelAndView modelandview = new ModelAndView("login");
		return modelandview;
	}
	
	@RequestMapping(value="/login/{var}")
	public ModelAndView loginPage1(@PathVariable("var") String message) {
		ModelAndView modelandview = new ModelAndView("login");
		modelandview.addObject("message", message);
		return modelandview;
	}
	
	@RequestMapping(value="/register")
	public ModelAndView registrationPage(@ModelAttribute("user") User user) {
		return new ModelAndView("register");
	}
	
	@RequestMapping(value="/register", method=RequestMethod.POST)
	public ModelAndView registerUser(@Valid User user, BindingResult result) throws Exception {
		if(result.hasErrors()) {
			ModelAndView m = new ModelAndView("register");
			m.addObject("user", user);
			return m;
		}
		if(user!=null) {
			user.setUsername(user.getUsername().trim());
			user.setNonLocked(true);
			user.setEnabled(true);
			user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
			Set<Role> roles = new HashSet<Role>();
			roles.add(roleService.getUserRole());
			user.setRoles(roles);
			User added = userService.save(user);
			if(added!=null) {
				return new ModelAndView("redirect:/login/useradded");
			}
		}
		return new ModelAndView();
	}
	
	@RequestMapping(value="/store/{accessToken}/{refreshToken}")
	public ModelAndView getUsers(@PathVariable Map<String,String> pathVars) throws Exception {
		String accessToken = pathVars.get("accessToken");
		String refreshToken = pathVars.get("refreshToken");
		TokenInfo tokenn = new TokenInfo(accessToken, refreshToken); 
		session.setAttribute("toks", tokenn);
		TokenInfo info = (TokenInfo) session.getAttribute("toks");
		if(userService.isAdmin()) {
			return new ModelAndView("redirect:/dashboard?access_token="+info.getAccessToken());
		}
		return new ModelAndView("redirect:/user/dashboard?access_token="+info.getAccessToken());
	}
	
	@ModelAttribute
	public void addCommonObjects(Model model1) {
		if(session.getAttribute("toks")!=null) {
			TokenInfo info = (TokenInfo) session.getAttribute("toks");
			model1.addAttribute("sess", info);
		}
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping("/dashboard")
	public ModelAndView dash() {
		ModelAndView modelandview = new ModelAndView("dashboard");
		
		return modelandview;
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping("/addBook")
	public ModelAndView addBookPage(Book book){
		ModelAndView modelandview = new ModelAndView("addBookPage");
		return modelandview;
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value="/addBook", method=RequestMethod.POST)
	public ModelAndView addBook(@Valid Book book, BindingResult result){
		if(result.hasErrors()) {
			ModelAndView m = new ModelAndView("addBookPage", "error", "There are errors in the form");
			return m;
		}
		if(book!=null) {
			Book isbnBook = bookService.isIsbnExist(book.getIsbn());
			if(isbnBook!=null) {
				ModelAndView m = new ModelAndView("addBookPage", "error", "ISBN belongs to '"+isbnBook.getBookName()+"' already");
				return m;
			}
			book.setBookName(book.getBookName().trim());
			Book added = bookService.save(book);
			if(added!=null) {
				return new ModelAndView("addBookPage", "success",added.getTotalNumber()+" copies of '"+ added.getBookName()+"' has been successfully added to inventory");
			}
		}
		return new ModelAndView();
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value="/viewAllBooks")
	public ModelAndView viewBooks(){
		List<Book> myBooks = bookService.findAll();
		Map<Book, Integer> counts = new HashMap<Book, Integer>();
		for(Book b:myBooks) {
			int m = borrowedService.countByBook(b);
			counts.put(b, m);
		}
		ModelAndView modelandview = new ModelAndView("viewBooks");
		modelandview.addObject("allBooks", myBooks);
		modelandview.addObject("counts", counts);
		return modelandview;
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value="/editBook/{id}")
	public ModelAndView editBookPage(@PathVariable("id") int id) {
		Book book = bookService.getBookById((long) id);
		ModelAndView m = new ModelAndView("editBook");
		m.addObject("book", book);
		return m;
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value="/editBook/{id}", method=RequestMethod.POST)
	public ModelAndView editBook(@Valid Book book, BindingResult result, RedirectAttributes redir) {
		TokenInfo info = (TokenInfo) session.getAttribute("toks");
		if(result.hasErrors()) {
			ModelAndView m = new ModelAndView("editBook");
			m.addObject("book", book);
			m.addObject("error", "There are some in your form");
			return m;
		}
		if(book!=null) {
			Book isbnBook = bookService.isIsbnExist(book.getIsbn());
			if(isbnBook!=null) {
				if(book.getId()!=isbnBook.getId()) {
					ModelAndView m = new ModelAndView("editBook", "error", "ISBN belongs to '"+isbnBook.getBookName()+"' already");
					return m;
				}
			}
			book.setBookName(book.getBookName().trim());
			Book added = bookService.save(book);
			if(added!=null) {
				ModelAndView m = new ModelAndView("redirect:/viewAllBooks?access_token="+info.getAccessToken());
				redir.addFlashAttribute("success", added.getBookName()+" has been successfully modified");
				return m;
			}
		}
		return new ModelAndView();
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value="/deleteBook/{id}")
	public ModelAndView deleteBook(@PathVariable("id") int id, RedirectAttributes redir) {
		TokenInfo info = (TokenInfo) session.getAttribute("toks");
		Book book = bookService.getBookById((long)id);
		boolean b = borrowedService.isBookCheckedOut(book);
		if(b) {
			ModelAndView m = new ModelAndView("redirect:/viewAllBooks?access_token="+info.getAccessToken());
			redir.addFlashAttribute("error", book.getBookName()+" couldn't be deleted. Some copies has been checled out already");
			return m;
		}
		boolean deleted = bookService.delete(book);
		if(deleted) {
			ModelAndView m = new ModelAndView("redirect:/viewAllBooks?access_token="+info.getAccessToken());
			redir.addFlashAttribute("success", "Book has been successfully deleted");
			return m;
		}
		return new ModelAndView();
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value="/viewAllUsers")
	public ModelAndView viewUsers() {
		List<User> allUsers = userService.findAll();
		Map<User, Integer> borrowedCounts = new HashMap<User, Integer>();
		for(User u : allUsers) {
			int m = borrowedService.countByUser(u);
			borrowedCounts.put(u, m);
		}
		ModelAndView modelandview = new ModelAndView("viewUsers");
		modelandview.addObject("allUsers", allUsers);
		modelandview.addObject("counts", borrowedCounts);
		return modelandview;
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value="/blockUser/{id}")
	public ModelAndView block(@PathVariable("id") int id, RedirectAttributes redir) {
		TokenInfo info = (TokenInfo) session.getAttribute("toks");
		ModelAndView m = new ModelAndView("redirect:/viewAllUsers?access_token="+info.getAccessToken());
		User user = userService.findOneUser((long) id);
		if(id == 1) {
			redir.addFlashAttribute("error", user.getUsername() +" account can't be locked");
			return m;
		}
		if(!user.isNonLocked()) {
			redir.addFlashAttribute("warning", user.getUsername() +" has already been locked");
			return m;
		}
		user.setNonLocked(false);
		User added = userService.save(user);
		if(added!=null) {
			redir.addFlashAttribute("success", added.getUsername() +" account has been successfully locked");
			return m;
		}
		redir.addFlashAttribute("error", "Something went wrong");
		return m;
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value="/unBlockUser/{id}")
	public ModelAndView unBlock(@PathVariable("id") int id, RedirectAttributes redir) {
		TokenInfo info = (TokenInfo) session.getAttribute("toks");
		ModelAndView m = new ModelAndView("redirect:/viewAllUsers?access_token="+info.getAccessToken());
		User user = userService.findOneUser((long) id);
		if(user.isNonLocked()) {
			redir.addFlashAttribute("warning", user.getUsername() +" isn't locked");
			return m;
		}
		user.setNonLocked(true);
		User added = userService.save(user);
		if(added!=null) {
			redir.addFlashAttribute("success", added.getUsername() +" account has been successfully unlocked");
			return m;
		}
		redir.addFlashAttribute("error", "Something went wrong");
		return m;
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value="/viewLog")
	public ModelAndView log() {
		List<Book> books = bookService.findAll();
		List<Log> logs = logService.findAll();
		ModelAndView m = new ModelAndView("log");
		m.addObject("logs", logs);
		m.addObject("books", books);
		return m;
	}
	
	@RequestMapping(value="/logTimestamp", method=RequestMethod.POST)
	public ModelAndView logTimestamp(@RequestParam("from") Date from, @RequestParam("to") Date to) {
		List<Book> books = bookService.findAll();
		List<Log> logs = logService.getAllWithinTime(from, to);
		ModelAndView m = new ModelAndView("log");
		m.addObject("logs", logs);
		m.addObject("query", "TimeStamp");
		m.addObject("books", books);
		m.addObject("success", "Logs between "+from+" to "+ to);
		return m;
	}
	
	@RequestMapping(value="/logAction", method=RequestMethod.POST)
	public ModelAndView logAction(@RequestParam("action") String action) {
		List<Book> books = bookService.findAll();
		List<Log> logs = logService.getAllByAction(action);
		ModelAndView m = new ModelAndView("log");
		m.addObject("logs", logs);
		m.addObject("query", "TimeStamp");
		m.addObject("books", books);
		m.addObject("success", "Logs for all "+ action+"s");
		return m;
	}
	
	@RequestMapping(value="/logBook", method=RequestMethod.POST)
	public ModelAndView logBook(@RequestParam("book") long id) {
		List<Book> books = bookService.findAll();
		Book book = bookService.getBookById(id);
		List<Log> logs = logService.getAllByBook(book);
		ModelAndView m = new ModelAndView("log");
		m.addObject("logs", logs);
		m.addObject("books", books);
		m.addObject("query", "TimeStamp");
		m.addObject("success", "Logs for "+book.getBookName());
		return m;
	}
}
