package com.example.aims.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@JsonInclude(JsonInclude.Include.NON_NULL) // khai bao cho json la khi đổi object sang json thi field nao null se dc bo di
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
/*
 * Coupling/Cohesion:
 * - Low coupling, high cohesion.
 * Reason why:
 * - This generic wrapper only standardizes API response shape across controllers.
 * - It is reusable, contains no use-case logic, and depends only on the result type parameter.
 */
public class ApiResponse<T>{
    @Builder.Default
    private int code = 1000;

    private String message;
    private T result;
}
