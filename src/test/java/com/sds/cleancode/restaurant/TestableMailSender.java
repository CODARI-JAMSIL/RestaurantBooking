package com.sds.cleancode.restaurant;

public class TestableMailSender extends {

	/*
	 * #1. mail 전송한 countSendMailMethodIsCalled 변수 선언 (int 형)
	 *     test commit
	 */
	
	/*
	 * #2. MailSender의 sendMail이 구현한 내용이 없으므로 MailSender를  extends하여, sendMail을 Override 하고
	 *     Override된 sendMail 함수가 호출될 때마다 countSendMailMethodIsCalled 변수 증가
	 */   
	
	/*
	 * #3. countSendMailMethodIsCalled 변수를 리턴 하는 getCountSendMailMethodIsCalled 메서드 
	 */
}