package trd.home.auth.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import trd.home.auth.constant.UserRole;
import trd.home.auth.dao.User;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("SELECT CASE WHEN COUNT(appUser) > 0 THEN TRUE ELSE FALSE END "
            + "FROM User appUser JOIN appUser.roles role WHERE role = :role")
    boolean existsByRole(@Param("role") UserRole role);
}
