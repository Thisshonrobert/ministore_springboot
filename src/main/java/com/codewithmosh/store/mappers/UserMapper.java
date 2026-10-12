//member 3
package com.codewithmosh.store.mappers;

import com.codewithmosh.store.entities.User;
import com.codewithmosh.store.dtos.*;
import org.mapstruct.*;
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {

    UserDto toDto(User entity);
    @Mapping(target = "id", ignore = true)
    User toEntity(CreateCustomerRequest request);
    @Mapping(target = "id", ignore = true)
    void update(CreateCustomerRequest request, @MappingTarget User entity);
}
