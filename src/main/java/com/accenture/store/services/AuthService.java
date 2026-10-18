//member 3
package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.LoginRequest;
import com.codewithmosh.store.dtos.RegisterRequest;
import com.codewithmosh.store.dtos.UserDto;
import com.codewithmosh.store.entities.Cart;
import com.codewithmosh.store.entities.User;
import com.codewithmosh.store.exceptions.ConflictException;
import com.codewithmosh.store.exceptions.UnauthorizedException;
import com.codewithmosh.store.mappers.UserMapper;
import com.codewithmosh.store.repositories.CartRepository;
import com.codewithmosh.store.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional
public class AuthService {
    public static final String SESSION_CUSTOMER_ID = "customerId";

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final UserMapper userMapper;
    private final PasswordHasher passwordHasher;

    public AuthService(UserRepository userRepository,
                       CartRepository cartRepository,
                       UserMapper userMapper,
                       PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.userMapper = userMapper;
        this.passwordHasher = passwordHasher;
    }

    public UserDto register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already belongs to a customer.");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordHasher.hash(request.getPassword()));
        User savedUser = userRepository.save(user);

        Cart cart = new Cart();
        cart.setUser(savedUser);
        cartRepository.save(cart);

        return userMapper.toDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDto authenticate(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null
                || user.getPasswordHash() == null
                || !passwordHasher.matches(request.getPassword(), user.getPasswordHash())) {
            // One message for unknown email, missing password and wrong password.
            throw new UnauthorizedException("Invalid email or password.");
        }

        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto getCustomer(Long customerId) {
        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new UnauthorizedException("Your session is no longer valid. Please sign in again."));
        return userMapper.toDto(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
