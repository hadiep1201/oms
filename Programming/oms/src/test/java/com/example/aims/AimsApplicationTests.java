package com.example.aims;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AimsApplicationTests {

	@Test
	void applicationClassCanBeLoaded() {
		assertDoesNotThrow(() -> Class.forName(AimsApplication.class.getName()));
	}

}
