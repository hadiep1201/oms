package com.example.aims.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data // tu tao getter, setter
@NoArgsConstructor
@AllArgsConstructor // tu tao 2 constructor co du gtri va ko du gtri
@Builder // dung de tao object
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Sender {
    String name, email;
}
