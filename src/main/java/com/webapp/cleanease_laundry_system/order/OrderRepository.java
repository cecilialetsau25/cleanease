package com.webapp.cleanease_laundry_system.order;

import com.webapp.cleanease_laundry_system.user.User;
import jakarta.transaction.Transactional;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderRepository extends CrudRepository<Order, Integer> {
    List<Order> findByOrderStatus(String pending);

    @Transactional
    void deleteAllByUser(User user);
}
