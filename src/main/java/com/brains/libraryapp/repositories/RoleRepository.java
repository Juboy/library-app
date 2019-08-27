package com.brains.libraryapp.repositories;

import org.springframework.data.repository.CrudRepository;

import com.brains.libraryapp.models.Role;

public interface RoleRepository extends CrudRepository<Role, Long>{
	Role findOneByRoleId(Long roleId);
}
