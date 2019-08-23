package com.brains.libraryapp.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.brains.libraryapp.TokenInfo;
import com.brains.libraryapp.models.User;
import com.brains.libraryapp.services.UserService;


@Controller
public class UserController {
	@Autowired
	private HttpSession session;
	
	@Autowired
	private UserService userService;
		
	@RequestMapping(value="/login")
	public String loginPage() {
		return "login";
	}
	
	@RequestMapping(value="/register")
	public String registrationPage() {
		return "register";
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
			System.out.println(info.getAccessToken());
			System.out.println(info.getRefreshToken());
			model1.addAttribute("sess", info);
		}
	}
	
	@RequestMapping("/dashboard")
	public ModelAndView dash(Principal principal) {
		List<User> allUsers=userService.findAll();
		ModelAndView modelandview = new ModelAndView("dashboard");
		modelandview.addObject("user", principal.getName());
		modelandview.addObject("allUsers", allUsers);
		return modelandview;
	}
	
	@RequestMapping("/board")
	public ModelAndView board(Principal principal) {
		ModelAndView modelandview = new ModelAndView("dashboard");
		modelandview.addObject("user", principal.getName()+" \n "+ principal.toString());
		return modelandview;
	}
	
	
}
