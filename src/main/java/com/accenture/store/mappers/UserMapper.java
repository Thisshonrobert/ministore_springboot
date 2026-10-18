//member 3
package com.codewithmosh.store.mappers;

import com.codewithmosh.store.dtos.CreateCustomerRequest;
import com.codewithmosh.store.dtos.UserDto;
import com.codewithmosh.store.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }

    public User toEntity(CreateCustomerRequest request) {
        if (request == null) {
            return null;
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        return user;
    }

    public void update(CreateCustomerRequest request, User user) {
        if (request == null) {
            return;
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
    }
}
