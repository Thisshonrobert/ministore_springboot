//member 3
package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.CreateCustomerRequest;
import com.codewithmosh.store.dtos.UserDto;
import com.codewithmosh.store.entities.User;
import com.codewithmosh.store.exceptions.ConflictException;
import com.codewithmosh.store.exceptions.ResourceNotFoundException;
import com.codewithmosh.store.mappers.UserMapper;
import com.codewithmosh.store.repositories.CartRepository;
import com.codewithmosh.store.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class CustomerService {
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final UserMapper userMapper;

    public CustomerService(UserRepository userRepository,
                           CartRepository cartRepository,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public List<UserDto> list() {
        List<User> users = userRepository.findAll();
        List<UserDto> result = new ArrayList<>();

        for (User user : users) {
            result.add(userMapper.toDto(user));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public UserDto get(Long id) {
        User user = findCustomer(id);
        return userMapper.toDto(user);
    }

    public UserDto create(CreateCustomerRequest request) {
        normalize(request);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already belongs to a customer.");
        }

        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    public UserDto update(Long id, CreateCustomerRequest request) {
        User user = findCustomer(id);
        normalize(request);

        if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new ConflictException("Email already belongs to a customer.");
        }

        userMapper.update(request, user);
        return userMapper.toDto(user);
    }

    public void delete(Long id) {
        User user = findCustomer(id);

        if (cartRepository.existsByUserId(id)) {
            throw new ConflictException("Delete this customer's cart first.");
        }

        userRepository.delete(user);
    }

    private void normalize(CreateCustomerRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        request.setEmail(email);
    }

    private User findCustomer(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found."));
    }
}
