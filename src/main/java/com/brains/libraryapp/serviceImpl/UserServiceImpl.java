package com.brains.libraryapp.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.brains.libraryapp.models.CustomUserDetails;
import com.brains.libraryapp.models.User;
import com.brains.libraryapp.repositories.UserRepository;
import com.brains.libraryapp.services.UserService;

import java.util.ArrayList;
import java.util.List;

@Service(value = "userService")
public class UserServiceImpl implements UserDetailsService, UserService {
	
	@Autowired
	private UserRepository userDao;

	private Long userId;
	
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		User user = userDao.findByUsername(userId);
		if(user == null){
			throw new UsernameNotFoundException("Invalid username or password.");
		}else {
			this.userId = user.getId();
		}
		return new CustomUserDetails(user);
	}

	public List<User> findAll() {
		List<User> list = new ArrayList<>();
		userDao.findAll().iterator().forEachRemaining(list::add);
		return list;
	}

	@Override
	public void delete(Long id) {
		userDao.deleteById(id);
	}

	@Override
    public User save(User user) {
        return userDao.save(user);
    }

	public Long getUserId() {
		return userId;
	}


}
