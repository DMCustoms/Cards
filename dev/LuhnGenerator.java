import java.util.Random;

public class LuhnGenerator {

	public static String generateValidLuhn() {
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

	public static void main(String[] args) {
		System.out.println(generateValidLuhn());
	}
}
