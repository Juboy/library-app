package com.brains.libraryapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserController {

	@RequestMapping(value="/login")
	public String loginPage() {
		return "login";
	}
}
