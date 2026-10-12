//member 3
package com.codewithmosh.store.services;

import com.codewithmosh.store.entities.*;
import com.codewithmosh.store.dtos.*;
import com.codewithmosh.store.repositories.*;
import com.codewithmosh.store.mappers.*;
import com.codewithmosh.store.exceptions.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional
public class CustomerService {
    private final UserRepository users;
    private final CartRepository carts;
    private final UserMapper mapper;
    @Transactional(readOnly = true)
    public List<UserDto> list() { return users.findAll().stream().map(mapper::toDto).toList(); }
    @Transactional(readOnly = true)
    public UserDto get(Long id) { return mapper.toDto(find(id)); }
    public UserDto create(CreateCustomerRequest request) {
        normalize(request);
        if (users.existsByEmail(request.getEmail())) throw new ConflictException("Email already belongs to a customer.");
        return mapper.toDto(users.save(mapper.toEntity(request)));
    }
    public UserDto update(Long id, CreateCustomerRequest request) {
        var user = find(id);
        normalize(request);
        if (users.existsByEmailAndIdNot(request.getEmail(), id)) throw new ConflictException("Email already belongs to a customer.");
        mapper.update(request, user);
        return mapper.toDto(user);
    }
    public void delete(Long id) {
        var user = find(id);
        if (carts.existsByUserId(id)) throw new ConflictException("Delete this customer's cart first.");
        users.delete(user);
    }
    private void normalize(CreateCustomerRequest request) {
        request.setEmail(request.getEmail().trim().toLowerCase(java.util.Locale.ROOT));
    }
    private User find(Long id) {
        return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer not found."));
    }
}
