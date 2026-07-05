package com.example.aims.config;

import com.example.aims.enums.OrderStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderStatusConstraintInitializer implements ApplicationRunner {

    private static final String CONSTRAINT_NAME = "orders_status_check";

    JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        List<String> statusConstraints = jdbcTemplate.queryForList("""
                select c.conname
                from pg_constraint c
                join pg_class t on t.oid = c.conrelid
                where t.relname = 'orders'
                  and c.contype = 'c'
                  and pg_get_constraintdef(c.oid) like '%status%'
                """, String.class);

        statusConstraints.forEach(name -> jdbcTemplate.execute(
                "alter table orders drop constraint if exists " + quoteIdentifier(name)
        ));

        jdbcTemplate.execute("""
                alter table orders
                add constraint %s check (status in (%s))
                """.formatted(quoteIdentifier(CONSTRAINT_NAME), quotedStatuses()));
        log.info("Order status check constraint refreshed with values: {}", Arrays.toString(OrderStatus.values()));
    }

    private String quotedStatuses() {
        return Arrays.stream(OrderStatus.values())
                .map(status -> "'" + status.name() + "'")
                .collect(Collectors.joining(", "));
    }

    private String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}
