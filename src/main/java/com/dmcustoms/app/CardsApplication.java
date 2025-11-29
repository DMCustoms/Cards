package com.dmcustoms.app;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dmcustoms.app.data.entities.Card;
import com.dmcustoms.app.data.entities.User;
import com.dmcustoms.app.data.repositories.CardRepository;
import com.dmcustoms.app.data.repositories.UserRepository;
import com.dmcustoms.app.data.types.Authorities;
import com.dmcustoms.app.data.types.CardStatus;

@SpringBootApplication
public class CardsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardsApplication.class, args);
	}

	@Bean
	@Profile("dev")
	CommandLineRunner loadInitialTestData(UserRepository userRepository, CardRepository cardRepository,
			PasswordEncoder passwordEncoder) {
		return args -> {
			if (userRepository.count() == 0 && cardRepository.count() == 0) {
				Card card1 = new Card("2202202044507626", Instant.now().plus(Duration.ofDays(1825)), CardStatus.ACTIVE,
						17000., -1., -1.);
				Card card2 = new Card("2202202044507627", Instant.now().plus(Duration.ofDays(1825)), CardStatus.ACTIVE,
						1500., -1., -1.);
				Card card3 = new Card("2202202044507628", Instant.now().plus(Duration.ofDays(1825)), CardStatus.ACTIVE,
						90235., -1., -1.);

				cardRepository.save(card1);
				cardRepository.save(card2);
				cardRepository.save(card3);

				User user1 = new User("Ivanov", "Petr", "Sergeevich", "i.ivanov@test.com",
						passwordEncoder.encode("password"), true, true, true, true, Arrays.asList(Authorities.USER));
				User user2 = new User("Petrov", "Sergey", "Ivanovich", "s.petrov@test.com",
						passwordEncoder.encode("password"), true, true, true, true, Arrays.asList(Authorities.USER));
				User admin1 = new User("Sergeev", "Victor", "Konstantinovich", "v.sergeev@test.com",
						passwordEncoder.encode("password"), true, true, true, true, Arrays.asList(Authorities.ADMIN));
				
				user1.addCard(card1);
				user2.addCard(card2);
				user2.addCard(card3);
				
				userRepository.save(user1);
				userRepository.save(user2);
				userRepository.save(admin1);
			}
		};
	}

}
