package se.ltu.M7017E.lab3.client;

import se.ltu.M7017E.lab3.audio.answerphone.AnswerPhone;

/** Main class for the client. */
public class AnwePhoneTestMain {

	public static void main(String[] args) {
		String localhost = "localhost";
		int porc = 5003;
		AnswerPhone a = new AnswerPhone();
		a.doAnswerPhone(localhost, porc);

	}
}
