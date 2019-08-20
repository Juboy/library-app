package com.brains.libraryapp.controllers;

//import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserRestController {

	@RequestMapping(value="/index")
	public String getUser() {
		return "Brains Technologies";
	}
}
