package com.webapp.cleanease_laundry_system.user;

import com.webapp.cleanease_laundry_system.order.Order;
import com.webapp.cleanease_laundry_system.order.OrderRepository;
import com.webapp.cleanease_laundry_system.shipping.details.ShippingDetails;
import com.webapp.cleanease_laundry_system.shipping.details.ShippingDetailsRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Date;

@Controller
public class UserController {

    @Autowired private UserRepository userRepository;

    @Autowired private EmailService emailService;

    @Autowired private ShippingDetailsRepository shippingDetailsRepository;

    @Autowired private OrderRepository orderRepository;

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login(){
        return "login_page";
    }

    @GetMapping("/register")
    public String register(){
        return "register_page";
    }

    @PostMapping("/register/user")
    public String registerUser(
            @RequestParam("name") String name,
            @RequestParam("surname") String surname,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            RedirectAttributes redirectAttributes) {

        // Check if the email is already registered
        if (userRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("message", "Email already exists");
            return "redirect:/register";
        }

        // Create a new user and set its properties
        User user = new User();
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setPassword(password);

        // Save the user to the database
        userRepository.save(user);

        // Send confirmation email
        String subject = "Welcome to CleanEase";
        String body = "Welcome to CleanEase\n\n" +
                "Your ultimate solution for easy and efficient laundry service.\n\n" +
                "This is our web application.";
        try {
            emailService.sendEmail(email, subject, body);
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Registration successful, but failed to send confirmation email.");
            return "redirect:/login";
        }

        redirectAttributes.addFlashAttribute("message", "Registration Successful. Receive A Confirmation Email.");
        // Redirect to a success page or login page
        return "redirect:/login";
    }

    @PostMapping("/login/user")
    public String loginUser(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            RedirectAttributes redirectAttributes, HttpServletRequest request,
            Model model) {

        HttpSession session = request.getSession(true);

        // Check if the email is registered
        User user = userRepository.findByEmail(email);
        if (user == null) {
            redirectAttributes.addFlashAttribute("message", "Email does not exist");
            return "redirect:/login";
        }

        // Validate password
        if (!user.getPassword().equals(password)) {
            redirectAttributes.addFlashAttribute("message", "Incorrect password");
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        session.setAttribute("user", user);
        // If credentials are valid, go to the order creation page
        return "order_create";
    }

    @GetMapping("/order_create")
    public String orderCreate(HttpServletRequest request, Model model,
                              RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if(user != null){
            model.addAttribute("user", user);
            return "order_create";
        }else{
            redirectAttributes.addFlashAttribute("message", "You must log in first.");
            return "redirect:/login";
        }
    }

    @PostMapping("/order/save")
    public String saveOrder(
            @RequestParam("orderDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date orderDate,
            @RequestParam("numBaskets") int numBaskets,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("shippingAddress") String shippingAddress,
            @RequestParam("city") String city,
            @RequestParam("state") String state,
            @RequestParam("zipcode") String zipcode,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            Model model) {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        // Create and populate ShippingDetails
        ShippingDetails shippingDetails = new ShippingDetails();
        shippingDetails.setAddress(shippingAddress);
        shippingDetails.setCity(city);
        shippingDetails.setState(state);
        shippingDetails.setZipcode(zipcode);
        shippingDetails.setPhoneNumber(phoneNumber);

        // Create and populate Order
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(orderDate);
        order.setOrderStatus("Pending"); // Set default status
        order.setOrderAmount(numBaskets * 120.0); // Assuming each basket costs R120
        order.setNumBaskets(numBaskets);
        order.setShippingDetails(shippingDetails);

        // Save ShippingDetails and Order to the database
        shippingDetailsRepository.save(shippingDetails);
        orderRepository.save(order);

        // Send order confirmation email
        String subject = "Order Confirmation - CleanEase";
        String body = "Dear " + user.getName() + ",\n\n" +
                "Your order has been successfully placed.\n" +
                "Order details:\n" +
                "Order ID: " + order.getOrderId() + "\n" +
                "Number of Baskets: " + numBaskets + "\n" +
                "Total Amount: R" + order.getOrderAmount() + "\n\n" +
                "User Order ID to track your order" + "\n\n" +
                "Thank you for choosing CleanEase!";
        try {
            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Order saved but failed to send confirmation email.");
            return "redirect:/index";
        }

        model.addAttribute("user", user);
        redirectAttributes.addFlashAttribute("message", "Order placed successfully!");
        return "redirect:/order_create";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, RedirectAttributes redirectAttributes){
        HttpSession session = request.getSession();
        session.invalidate();

        redirectAttributes.addFlashAttribute("message", "Logout Successful!");
        return "redirect:/login";
    }

    @GetMapping("/track/order")
    public String trackOrder(){
        return "track_order";
    }

    @PostMapping("/track/order")
    public String trackOrder(@RequestParam("orderId") Integer orderId, Model model, RedirectAttributes redirectAttributes) {
        // Find the order by ID
        Order order = orderRepository.findById(orderId).orElse(null);

        // Check if the order exists
        if (order == null) {
            redirectAttributes.addFlashAttribute("message", "Order ID not found");
            return "redirect:/track/order";
        }

        // Add the order details to the model
        model.addAttribute("order", order);
        return "order_result";
    }

    @GetMapping("/delete")
    public String deleteAccount(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user != null) {
            // Remove associated orders and shipping details first if needed
            orderRepository.deleteAllByUser(user);

            // Delete the user account
            userRepository.deleteByEmail(user.getEmail());

            // Invalidate the session
            session.invalidate();

            redirectAttributes.addFlashAttribute("message", "Account deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("message", "You must log in first.");
        }

        return "redirect:/login";
    }



}
