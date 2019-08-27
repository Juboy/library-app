package com.brains.libraryapp.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.springframework.web.servlet.ModelAndView;

import com.brains.libraryapp.TokenInfo;
import com.brains.libraryapp.models.Book;
import com.brains.libraryapp.models.Role;
import com.brains.libraryapp.models.User;
import com.brains.libraryapp.services.BookService;
import com.brains.libraryapp.services.RoleService;
import com.brains.libraryapp.services.UserService;


@Controller
public class UserController {
	@Autowired
	private HttpSession session;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private BookService bookService;
	
	
	@InitBinder
	protected void binder(WebDataBinder bind) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		bind.registerCustomEditor(Date.class, "dob", new CustomDateEditor(format, false));
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
		return new ModelAndView("redirect:/dashboard?access_token="+info.getAccessToken());
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
//		modelandview.addObject("user", principal.getName());
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
	
	@RequestMapping(value="/viewAllBooks")
	public ModelAndView viewBooks(@Valid Book book, BindingResult result){
		Iterable<Book> iBooks = bookService.findAll();
		List<Book> myBooks = new ArrayList<Book>();
		for(Book eachbook : iBooks) {
			myBooks.add(eachbook);
		}
		ModelAndView modelandview = new ModelAndView("viewBooks");
		modelandview.addObject("allBooks", myBooks);
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
	public ModelAndView editBook(@Valid Book book, BindingResult result) {
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
				return new ModelAndView("redirect:/viewAllBooks?access_token="+info.getAccessToken(), "success", added.getBookName()+"' has been successfully modified");
			}
		}
		return new ModelAndView();
	}
	
}
