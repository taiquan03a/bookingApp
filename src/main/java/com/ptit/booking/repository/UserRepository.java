package com.ptit.booking.repository;

import com.ptit.booking.model.Booking;
import com.ptit.booking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Optional<User> findByUid(String uid);
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.role != 'ROLE_USER'")
    List<User> findAllExcludingUserRole();

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.role = 'ROLE_USER'")
    List<User> findAllCustomer();

    @Query("select count (u) > 0 from User u JOIN u.roles r WHERE u.email = :email AND r.role != 'ROLE_USER'")
    boolean existsUserByEmail(@Param("email") String email);

    @Query("select count (u) > 0 from User u JOIN u.roles r WHERE u.email = :email AND r.role = 'ROLE_USER'")
    boolean existsCustomerByEmail(@Param("email") String email);

    @Query("select count(u) from User u join u.roles r where r.role = 'ROLE_USER'")
    int countCustomer();

    @Query("select count(u) from User u join u.roles r where r.role != 'ROLE_USER'")
    int countUser();

    @Query("""
        select b
        from Booking b
        where b.user = :user and b.status = 'CHECKOUT'
    """)
    List<Booking> findBookingByUser(@Param("user") User user);
}
