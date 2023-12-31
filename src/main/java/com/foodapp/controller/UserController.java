package com.foodapp.controller;

import com.foodapp.exception.EmailAlreadyExistsException;
import com.foodapp.model.address.AddressDTO;
import com.foodapp.model.address.AddressService;
import com.foodapp.model.user.User;
import com.foodapp.model.user.UserRegistrationDTO;
import com.foodapp.model.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class UserController {
    private final UserService userService;
    private final AddressService addressService;

    public UserController(UserService userService, AddressService addressService) {
        this.userService = userService;
        this.addressService = addressService;
    }

    /**
     Handling registration requests
     */

    @GetMapping("/register")
    public String registerForm(Model model) {
        UserRegistrationDTO user = new UserRegistrationDTO();
        model.addAttribute("user", user);
        return "/home/register-form";
    }
    @PostMapping("/register")
    public String register(UserRegistrationDTO dto) {
        try {
            userService.register(dto);
            return "redirect:/confirmation-reg";
        } catch (EmailAlreadyExistsException e){
            return "redirect:/reg-error";
        }
    }
    @GetMapping("confirmation-reg")
    public String regConfirmation() {
        return "/home/registration-confirmation";
    }

    /**
     Handling loggin and home page requests
     */

    @GetMapping("/login")
    public String loginForm() {
        return "/home/login-form";
    }

    @GetMapping("/home-page")
    public String homeForm(Model model) {
        User user = userService.getLoggedInUser();
        model.addAttribute("user", user);
        model.addAttribute("username", user.getFirstName());
        model.addAttribute("welcomeMessage", "Witaj " + user.getFirstName() + "!");
        model.addAttribute("isAdmin", userService.isAdmin());
        return "home/home-page";
    }

    /**
     Handling requests available in the user's profile
     */

    @GetMapping("/profile")
    public String profileForm(Model model) {
        try {
            Double points = userService.getExtraPointsOfAuthenticatedUser();
            model.addAttribute("points", points);
            return "user-profile/profile";
        }catch (UsernameNotFoundException e){
            return "redirect:/user-error";
        }
    }
    @PostMapping("/change-address")
    public String changeAddress(@ModelAttribute("addressDTO") AddressDTO addressDTO, Principal principal){

        try {
            String name = principal.getName();
            addressService.addAddressForUser(name, addressDTO);
        }catch (UsernameNotFoundException e){
            return "redirect:/address-error";
        }
        return "redirect:/confirmation-address";
    }

    @PostMapping("/change-password")
    public String changePassword(Principal principal,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword){
        try {
            String name = principal.getName();
            userService.changeUserPassword(name, currentPassword,newPassword);
        } catch (IllegalArgumentException | UsernameNotFoundException e){
            return "redirect:/password-change-error";
        }
        return "redirect:/confirmation-password";
    }

    @GetMapping("confirmation-address")
    public String addressConfirmation(){
        return "user-profile/change-address-confirmation";
    }

    @GetMapping("/confirmation-password")
    public String passwordConfirmation(){
        return "user-profile/change-password-confirmation";
    }

    /**
     Handling errors
     */

    @GetMapping("user-error")
    public String userError(){
        return "error/user-error";
    }
    @GetMapping("reg-error")
    public String registerErrorForm(){
        return "/error/reg-error";
    }
}
