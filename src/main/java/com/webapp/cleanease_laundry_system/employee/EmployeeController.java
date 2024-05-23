package com.webapp.cleanease_laundry_system.employee;

import com.webapp.cleanease_laundry_system.order.Order;
import com.webapp.cleanease_laundry_system.order.OrderRepository;
import com.webapp.cleanease_laundry_system.user.EmailService;
import com.webapp.cleanease_laundry_system.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EmailService emailService;

    @GetMapping("/staff/login")
    public String employeeLogin(){
        return "employee_login";
    }

    @GetMapping("/login/employee")
    public String goToEmployeeDashboard(HttpServletRequest request, RedirectAttributes redirectAttributes){
        HttpSession session = request.getSession();
        Employee employee = (Employee) session.getAttribute("employee");

        if(employee != null){
            return "employee_handle_orders";
        }else{
            redirectAttributes.addFlashAttribute("message", "Login first!!!");
            // Redirect to a success page or login page
            return "redirect:/login";
        }
    }

    @PostMapping("/login/employee")
    public String loginEmployee(
            @RequestParam("employee_number") String employeeNumber,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request, Model model) {

        HttpSession session = request.getSession(true);
        Employee employee = employeeRepository.findByEmployeeNumber(employeeNumber);

        if (employee == null) {
            redirectAttributes.addFlashAttribute("message", "Employee number not found");
            return "redirect:/staff/login";
        }

        List<Order> pendingOrders = orderRepository.findByOrderStatus("Pending");
        model.addAttribute("employee", employee);
        model.addAttribute("orders", pendingOrders);
        session.setAttribute("employee", employee);
        return "employee_handle_orders";
    }

    @PostMapping("/order/handle")
    public String handleOrder(@RequestParam("orderId") Integer orderId, RedirectAttributes redirectAttributes) {
        Order order = orderRepository.findById(orderId).orElse(null);

        if (order != null) {
            order.setOrderStatus("Successful");
            orderRepository.save(order);

            // Send email to the user
            User user = order.getUser();
            String subject = "Order Update - CleanEase";
            String body = "Dear " + user.getName() + ",\n\n" +
                    "Your order has been successfully handled by our staff.\n" +
                    "Order details:\n" +
                    "Order ID: " + order.getOrderId() + "\n" +
                    "Number of Baskets: " + order.getNumBaskets() + "\n" +
                    "Total Amount: R" + order.getOrderAmount() + "\n\n" +
                    "Thank you for choosing CleanEase!";
            try {
                emailService.sendEmail(user.getEmail(), subject, body);
            } catch (Exception e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("message", "Order handled successfully, but failed to send confirmation email.");
            }
            redirectAttributes.addFlashAttribute("message", "Order handled successfully!");
        } else {
            redirectAttributes.addFlashAttribute("message", "Order not found");
        }

        return "redirect:/employee_handle_orders";
    }


    @GetMapping("/employee_handle_orders")
    public String handleResult(HttpServletRequest request, RedirectAttributes redirectAttributes, Model model){
        HttpSession session = request.getSession();
        Employee employee = (Employee) session.getAttribute("employee");
        if(employee != null){
            List<Order> pendingOrders = orderRepository.findByOrderStatus("Pending");
            model.addAttribute("employee", employee);
            model.addAttribute("orders", pendingOrders);
            return "employee_handle_orders";
        } else {
            redirectAttributes.addFlashAttribute("message", "Login first!");
            return "redirect:/staff/login";
        }
    }
}
