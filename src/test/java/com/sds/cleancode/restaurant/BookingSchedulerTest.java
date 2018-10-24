package com.sds.cleancode.restaurant;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;

import com.paopao.customer.Customer;

public class BookingSchedulerTest {
	private static final int UNDER_CAPACITY = 1;
	private static final int MAX_CAPACITY = 3;
	private static final DateTimeFormatter DATE_TIME_FORMATTER= DateTimeFormat.forPattern("YYYY/MM/dd HH:mm");
	private static final DateTime NOT_ON_THE_HOUR= DATE_TIME_FORMATTER.parseDateTime("2018/09/13 17:05");
	private static final DateTime ON_THE_HOUR= DATE_TIME_FORMATTER.parseDateTime("2018/09/13 17:00");
	private static final Customer CUSTOMER_WITHOUT_MAIL= new Customer("Yu", "010 1234 5678");
	private static final Customer CUSTOMER_WITH_MAIL= new Customer("Yu", "010 1234 5678", "test@test.com");
	private BookingScheduler booking= new BookingScheduler(MAX_CAPACITY);

	private List<Schedule> schedules= new ArrayList<>();
	private TestableMailSender testableMailSender= new TestableMailSender();
	private TestableSmsSender testableSmsSender= new TestableSmsSender();
	private MailSender mailSender= new MailSender();
	private SmsSender smsSender= new SmsSender();
	
	@Before
	public void setUp() {
		booking.setSchedules(schedules);
		booking.setSmsSender(testableSmsSender);
		booking.setMailSender(testableMailSender);
	}
	
	@Test(expected= RuntimeException.class)
	public void 정시에_예약하지_않을시_예외처리() {
		Schedule schedule= new Schedule(NOT_ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER_WITHOUT_MAIL);
		booking.addSchedule(schedule);
		fail();
	}
	
	@Test
	public void 정시에_예약할_경우_예약성공() {
		// Arrange
		Schedule schedule= new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER_WITHOUT_MAIL);
		
		// Act
		booking.addSchedule(schedule);
		
		// Assert
		assertThat(booking.hasSchedule(schedule), is(true));
	}
	
	@Test
	public void 시간대별_인원제한() {
		Schedule fullSchedule= new Schedule(ON_THE_HOUR, MAX_CAPACITY, CUSTOMER_WITHOUT_MAIL);
		booking.addSchedule(fullSchedule);
		
		try {
			Schedule newSchedule= new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER_WITHOUT_MAIL);
			booking.addSchedule(newSchedule);
			fail();
		} catch (RuntimeException e) {
			assertThat(e.getMessage(), is("Number of people is over restaurant capacity per hour"));
		}
	}
	
	@Test
	public void 시간대가_다르면_예약가능() {
		Schedule schedule= new Schedule(ON_THE_HOUR, MAX_CAPACITY, CUSTOMER_WITHOUT_MAIL);
		booking.addSchedule(schedule);
		
		DateTime anotherTime= ON_THE_HOUR.plusHours(UNDER_CAPACITY);
		Schedule newSchedule= new Schedule(anotherTime, MAX_CAPACITY, CUSTOMER_WITHOUT_MAIL);
		booking.addSchedule(newSchedule);
		
		assertThat(booking.hasSchedule(newSchedule), is(true));
	}
	
	@Test
	public void 예약완료시_sms_발송() {
		Schedule schedule= new Schedule(ON_THE_HOUR, MAX_CAPACITY, CUSTOMER_WITHOUT_MAIL);
		
		booking.addSchedule(schedule);
		
		assertThat(testableSmsSender.isSendMethodIsCalled(), is(true));
	}
	
	@Test
	public void email이_없는_경우_mail_미발송() {
		Schedule schedule= new Schedule(ON_THE_HOUR, MAX_CAPACITY, CUSTOMER_WITHOUT_MAIL);
		
		booking.addSchedule(schedule);
		
		assertThat(testableMailSender.getCountSendMailMethodIsCalled(), is(0));
	}
	
	@Test
	public void email이_있는_경우_mail_발송() {
		Schedule schedule= new Schedule(ON_THE_HOUR, MAX_CAPACITY, CUSTOMER_WITH_MAIL);
		booking.addSchedule(schedule);
		assertThat(testableMailSender.getCountSendMailMethodIsCalled(), is(1));
	}
	
	@Test
	public void 일요일인_경우_예외처리() {
		String sunday= "2018/09/09 17:00";
		try {
			booking= new TestableBookingScheduler(MAX_CAPACITY, sunday);
			
			booking.setSchedules(schedules);
			booking.setSmsSender(testableSmsSender);
			booking.setMailSender(testableMailSender);
			
			Schedule schedule= new Schedule(ON_THE_HOUR, MAX_CAPACITY, CUSTOMER_WITH_MAIL);
			booking.addSchedule(schedule);
			fail();
		} catch(RuntimeException e) {
			assertThat(e.getMessage(), is("Booking system is not available on sunday"));
		}
	}
	
	@Test
	public void 일요일_아닌경우_예약성공() {
		String monday= "2018/09/10 17:00";
		booking= new TestableBookingScheduler(MAX_CAPACITY, monday);
		
		booking.setSchedules(schedules);
		booking.setSmsSender(testableSmsSender);
		booking.setMailSender(testableMailSender);
		
		Schedule schedule= new Schedule(ON_THE_HOUR, MAX_CAPACITY, CUSTOMER_WITH_MAIL);
		booking.addSchedule(schedule);
		assertThat(booking.hasSchedule(schedule), is(true));
	}
	
	@Test
	public void Testable_클래스_쓰지않고() {
		booking= new BookingScheduler(MAX_CAPACITY);
		
		booking.setSchedules(schedules);
		booking.setSmsSender(smsSender);
		booking.setMailSender(mailSender);
		
		Schedule schedule= new Schedule(ON_THE_HOUR, MAX_CAPACITY, CUSTOMER_WITH_MAIL);
		booking.addSchedule(schedule);
		assertThat(booking.hasSchedule(schedule), is(true));
	}
}