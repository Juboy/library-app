package com.brains.libraryapp.services;

import com.brains.libraryapp.models.Role;

public interface RoleService {
	Role save(Role role);
	Role getUserRole();
	Role getAdminRole();
}
