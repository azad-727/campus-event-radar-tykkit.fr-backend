package com.tykkit.fr.main.controller;

import com.tykkit.fr.main.dto.ChangePasswordRequest;
import com.tykkit.fr.main.model.User;
import com.tykkit.fr.main.repository.UserRepository;
import com.tykkit.fr.main.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User signUpRequest){
        if(userRepo.existsByEmail(signUpRequest.getEmail())){
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }
        User user=new User();
        user.setFullName(signUpRequest.getFullName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setRollType("ROLE_STUDENT");
        user.setAcademicRollNo(signUpRequest.getAcademicRollNo());

        userRepo.save(user);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest){
        Optional<User> userOptional=userRepo.findByEmail(loginRequest.getEmail());
        if(userOptional.isPresent()){
            User user=userOptional.get();
            if(encoder.matches(loginRequest.getPassword(), user.getPassword())){
                String jwt=jwtUtils.generateJwtToken(user.getEmail());
                Map<String,Object> response=new HashMap<>();
                response.put("token",jwt);
                response.put("email",user.getEmail());
                response.put("fullName",user.getFullName());
                response.put("studentId",user.getAcademicRollNo());

                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(401).body("Error: Invalid credentials");
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail=authentication.getName();

        Optional<User> userOptional=userRepo.findByEmail(currentUserEmail);
        if(userOptional.isPresent()){
            User user=userOptional.get();
            if(encoder.matches(request.getOldPassword(),user.getPassword())){
                user.setPassword(encoder.encode(request.getNewPassword()));
                userRepo.save(user);
                return ResponseEntity.ok("Password Changed Successfully");
            }
            else{
                return ResponseEntity.status(401).body("Error: Old Password Mismatch");
            }
        }
        return ResponseEntity.status(401).body("Error: User not found");
    }
}