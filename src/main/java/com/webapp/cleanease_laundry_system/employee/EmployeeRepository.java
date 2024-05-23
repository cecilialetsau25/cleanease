package com.webapp.cleanease_laundry_system.employee;

import org.springframework.data.repository.CrudRepository;

public interface EmployeeRepository extends CrudRepository<Employee, Integer> {
    Employee findByEmployeeNumber(String employeeNumber);
}
