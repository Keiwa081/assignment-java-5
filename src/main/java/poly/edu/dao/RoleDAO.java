package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.model.Role;

import java.util.Optional;

@Repository
public interface RoleDAO extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);
}