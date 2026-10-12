//member 3
package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.*;
import com.codewithmosh.store.services.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.List;

    @RestController @RequestMapping("/users") @RequiredArgsConstructor
    public class UserController {
        private final CustomerService service;
        @GetMapping public List<UserDto> list() { return service.list(); }
        @GetMapping("/{id}") public UserDto get(@PathVariable Long id) { return service.get(id); }
        @PostMapping public ResponseEntity<UserDto> create(@Valid @RequestBody CreateCustomerRequest request) {
            var dto = service.create(request);
            return ResponseEntity.created(URI.create("/users/" + dto.getId())).body(dto);
        }
        @PutMapping("/{id}") public UserDto update(@PathVariable Long id, @Valid @RequestBody CreateCustomerRequest request) { return service.update(id, request); }
        @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) {
            service.delete(id);
            return ResponseEntity.noContent().build();
        }
    }
