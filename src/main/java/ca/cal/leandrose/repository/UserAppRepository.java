package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.UserApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAppRepository extends JpaRepository<UserApp, Long> {

    @Query("""
        select u from UserApp u where trim(lower(u.credentials.email)) = lower(trim(:email))
    """)
    Optional<UserApp> findUserAppByEmail(@Param("email") String email);

}
