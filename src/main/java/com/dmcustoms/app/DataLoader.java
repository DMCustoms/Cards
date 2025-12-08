package com.dmcustoms.app;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dmcustoms.app.data.entities.Card;
import com.dmcustoms.app.data.entities.User;
import com.dmcustoms.app.data.repositories.CardRepository;
import com.dmcustoms.app.data.repositories.UserRepository;
import com.dmcustoms.app.data.types.Authorities;
import com.dmcustoms.app.data.types.CardStatus;

@Configuration
public class DataLoader {

	@Bean
	CommandLineRunner loadInitialTestData(UserRepository userRepository, CardRepository cardRepository,
			PasswordEncoder passwordEncoder) {
		return args -> {
			if (userRepository.count() == 0 && cardRepository.count() == 0) {
				Card[] cards = new Card[30];
				cards[0] = new Card("2202202044507626", Instant.now().plus(Duration.ofDays(1825)), CardStatus.ACTIVE,
						12350.94, 100000000.00, 100000000.00, false);
				cardRepository.save(cards[0]);
				for (int i = 1; i < cards.length; i++) {
					String cardNumber = generateValidLuhn();
					double balance = Math.round((Math.random() * 100000) * 100.0) / 100.0;
					cards[i] = new Card(cardNumber, Instant.now().plus(Duration.ofDays(1825)), CardStatus.ACTIVE,
							balance, 100000000.00, 100000000.00, false);
					cardRepository.save(cards[i]);
				}

				Card cardWithoutOwner = new Card("4333780415293668", Instant.now().plus(Duration.ofDays(1825)),
						CardStatus.ACTIVE, 12350.94, 100000000.00, 100000000.00, false);

				cardRepository.save(cardWithoutOwner);

				User[] users = new User[] { new User("Ivanov", "Petr", "Sergeevich", "i.ivanov@test.com",
						passwordEncoder.encode("password"), true, true, true, true, Arrays.asList(Authorities.USER)),
						new User("Petrov", "Sergey", "Ivanovich", "s.petrov@test.com",
								passwordEncoder.encode("password"), true, true, true, true,
								Arrays.asList(Authorities.USER)),
						new User("Voskoboinikov", "Alexandr", "Alexeevich", "a.voskoboinikov@test.com",
								passwordEncoder.encode("password"), true, true, true, true,
								Arrays.asList(Authorities.USER)),
						new User("Shevchenko", "Oleg", "Dmitrievich", "o.shevchenko@test.com",
								passwordEncoder.encode("password"), true, true, true, true,
								Arrays.asList(Authorities.USER)),
						new User("Trofimov", "Victor", "Vladimirovich", "v.trofimov@test.com",
								passwordEncoder.encode("password"), true, true, true, true,
								Arrays.asList(Authorities.USER)),
						new User("Petrova", "Elizaveta", "Maksimovna", "e.petrova@test.com",
								passwordEncoder.encode("password"), true, true, true, true,
								Arrays.asList(Authorities.USER)),
						new User("Shevchuk", "Dmitriy", "Vladimirovich", "d.shevchuk@test.com",
								passwordEncoder.encode("password"), true, true, true, true,
								Arrays.asList(Authorities.USER)),
						new User("Kolesnikova", "Mariya", "Dmitrievna", "m.kolesnikova@test.com",
								passwordEncoder.encode("password"), true, true, true, true,
								Arrays.asList(Authorities.USER)),
						new User("Kudinova", "Anastasiya", "Alexandrovna", "a.kudinova@test.com",
								passwordEncoder.encode("password"), true, true, true, true,
								Arrays.asList(Authorities.USER)),
						new User("Menshov", "Victor", "Vladimirovich", "v.menshov@test.com",
								passwordEncoder.encode("password"), true, true, true, true,
								Arrays.asList(Authorities.USER)) };

				Random random = new Random();

				for (Card card : cards) {
					users[random.nextInt(9)].addCard(card);
				}

				for (User user : users) {
					userRepository.save(user);
				}

				User admin = new User("Sergeev", "Victor", "Konstantinovich", "v.sergeev@test.com",
						passwordEncoder.encode("password"), true, true, true, true, Arrays.asList(Authorities.ADMIN));

				userRepository.save(admin);

			}
		};
	}

	private String generateValidLuhn() {
		Random random = new Random();
		int[] digits = new int[15];

		for (int i = 0; i < 15; i++) {
			digits[i] = random.nextInt(10);
		}

		int sum = 0;
		boolean alternate = true;
		for (int i = 14; i >= 0; i--) {
			int digit = digits[i];
			if (alternate) {
				digit *= 2;
				if (digit > 9)
					digit -= 9;
			}
			sum += digit;
			alternate = !alternate;
		}
		int checkDigit = (10 - (sum % 10)) % 10;

		StringBuilder number = new StringBuilder();
		for (int digit : digits) {
			number.append(digit);
		}
		number.append(checkDigit);
		return number.toString();
	}

}
