package com.brains.libraryapp.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.brains.libraryapp.models.CustomUserDetails;
import com.brains.libraryapp.models.Role;
import com.brains.libraryapp.models.User;
import com.brains.libraryapp.repositories.UserRepository;
import com.brains.libraryapp.services.RoleService;
import com.brains.libraryapp.services.UserService;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

@Service(value = "userService")
public class UserServiceImpl implements UserDetailsService, UserService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private HttpSession session;
	
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(userId);
		if(user == null){
			throw new UsernameNotFoundException("Invalid username or password.");
		}else {
			session.setAttribute("userId", user.getId());
		}
		return new CustomUserDetails(user);
	}

	public List<User> findAll() {
		List<User> list = new ArrayList<>();
		userRepository.findAll().iterator().forEachRemaining(list::add);
		return list;
	}

	@Override
	public void delete(Long id) {
		userRepository.deleteById(id);
	}

	@Override
    public User save(User user) {
        return userRepository.save(user);
    }
	
	@Override
	public User getLoggedInUser() {
		return userRepository.findOneById((long) session.getAttribute("userId"));
	}
	
	@Override
	public Long getUserId() {
		return this.getLoggedInUser().getId();
	}

	@Override
	public boolean isUser() {
		Role userRole = roleService.getUserRole();
		User user = userRepository.findOneById(this.getUserId());
		return user.getRoles().contains(userRole);
	}

	@Override
	public boolean isAdmin() {
		Role userRole = roleService.getAdminRole();
		User user = userRepository.findOneById(this.getUserId());
		return user.getRoles().contains(userRole);
	}

	@Override
	public User findOneUser(Long id) {
		return userRepository.findOneById(id);
	}

}
