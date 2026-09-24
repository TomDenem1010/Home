package trd.home.auth.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import trd.home.auth.dao.User;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
