package com.brains.libraryapp.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.brains.libraryapp.models.Role;
import com.brains.libraryapp.repositories.RoleRepository;
import com.brains.libraryapp.services.RoleService;

@Service(value = "roleService")
public class RoleServiceImpl implements RoleService{
	
	@Autowired
	RoleRepository roleRepository;

	@Override
	public Role save(Role role) {
		Role added = roleRepository.save(role);
		return added;
	}

	@Override
	public Role getUserRole() {
		return roleRepository.findOneByRoleId((long)2);
	}

	@Override
	public Role getAdminRole() {
		return roleRepository.findOneByRoleId((long)1);
	}

}
