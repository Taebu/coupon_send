	/**
 * 
 */
package kr.co.cashq.coupon_send;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Taebu
 *
 */
 
 import java.util.Timer;
 import java.util.TimerTask;

 /**
  * @author Taebu
  * timer 4 개를 선언하여 4개의 주문을 동시에 처리 할수 있도록, Timer 와 TimerTask 를 선언하고 구동 중여부를 판단 할 수 있는 변수를 선언 한다.
  * 
  * 그리고 구동 중이면 구동중이기에 구동 중인 Timer에게는 일을 시키지 않는다.
  * 
  *  TimerMachine
  */
 public class TimerMachine {

 	public static int count1,count2,count3,count4;
 	public static int order_timer;
 	public static int try_order_count;
 	public static Timer m_timer1,m_timer2,m_timer3,m_timer4;
 	public static TimerTask m_task1,m_task2,m_task3,m_task4;
 	public static boolean hasStarted1,hasStarted2,hasStarted3,hasStarted4;


 	/* 생성자 초기화 변수 */
 	TimerMachine(){
 		hasStarted1 = false;
 		hasStarted2 = false;
 		hasStarted3 = false;
 		hasStarted4 = false;
 		order_timer = 30000;
 		try_order_count = 10;
 	}
 	

 	
 	/* timer machine 1 호기 */
 	static void timer_machine1(final String seq,final String tradeid, final String mobilid,final String prdtprice){
 		
 		m_timer1 = new Timer();
 		m_task1 = new TimerTask(){
 			
 			/* 주문 초기 진입시 무조건 초기 값은 0 값입니다. 승인시 "1"로 선언하고, 거절시 "2"로 선언 됩니다. */
 			String order_result = "0";
 			
 			boolean is_order1 = false;
 			boolean is_auto_cancel1 = false;
 			
 			boolean is_order_done1 = false;
 			
 			
 			public void run() {
 				hasStarted1 = true;
 			    
 				order_result = Coupon_fcm_queue.check_order_result(seq);
 				
 				/* 주문 승인*/
 				if(order_result.equals("1"))
 				{
 					/* 타이머 1 을 중지 합니다. */
 					m_timer1.cancel();
 					
 					/* 머신을 중지 합니다. */
 					hasStarted1 = false;
 					
 					/* 주문이 승인되었다고 선언 */
 					is_order1 = true;

 					System.out.println(seq + ", "+tradeid+ "승인 되었습니다. 첫번째 머신을 종료 합니다. ");
 					count1 = 0;
 					COUPON_SEND.hasStarted1 = false;
 					COUPON_SEND.check_order_number.remove(seq);

 					/* 알림톡을 보냅니다. */
 					Coupon_fcm_queue.send_arlimtalk(order_result, seq); 					
 				}
 				
 				is_order_done1 = order_result.equals("2")||order_result.equals("3")||order_result.equals("4")||order_result.equals("5");
 				
 				/* 주문 끝 */
 				if(is_order_done1){
 					/* 타이머 1 을 중지 합니다. */
 					m_timer1.cancel();
 					
 					/* 머신을 중지 합니다. */
 					hasStarted1 = false;
 					
 					/* 주문이 거절 되었다고 선언 */
 					is_order1 = false;
 					System.out.println(seq + ", "+tradeid+ "주문이 끝나게 되었습니다. 첫번째 머신을 종료 합니다. ");
 					count1 = 0;
 					COUPON_SEND.hasStarted1 = false;
 					COUPON_SEND.check_order_number.remove(seq);

 					/* 알림톡을 보냅니다. */
 					Coupon_fcm_queue.send_arlimtalk(order_result, seq);
 				}
 				
 				if(count1<try_order_count&&hasStarted1){
 					count1++;
 					System.out.println(seq + ", "+tradeid+ "첫번째 머신이 주문을 시도 합니다. " + count1 + " 번째 아직 주문이 아직 승인되지 않았습니다 !!! ");
 					Coupon_fcm_queue.set_notification(seq,Integer.toString(count1));
 					

 				}
				
				if (count1 >= try_order_count&&hasStarted1) {
					
					System.out.println(tradeid+"주문을 안 받다니 도저히 참을 수 없네요! 주문을 자동 취소(ad : auto_deny) 합니다.");
					
					/* 타이머 1 을 중지 합니다. */
 					m_timer1.cancel();
 					
					/* 자동 취소 되었습니다.  라고 선언*/
					is_auto_cancel1 = true;
					
					set_auto_cancel(seq,mobilid,prdtprice);
			 		/* 2018-12월 버전으로 자동 취소 url로 요청 하는 것
			 		 * 
			 		 * http://cashq.co.kr/adm/ext/kgmobilians/card/cancel/cn_cancel_result.php
			 		 * */
					count1 = 0;
					Coupon_fcm_queue.set_kgorder_autocancel_from_url(tradeid,mobilid,prdtprice);
					
			 		/* 모바일 자동 취소 url로 요청 하는 것
			 		 * 
			 		 * http://cashq.co.kr/adm/ext/kgmobilians/mobile/cancel/cancel_result.php
			 		 * */
			 		Coupon_fcm_queue.set_kgmobile_order_autocancel_from_url(tradeid,mobilid,prdtprice);
			 		
			 		
			 		//Coupon_fcm_queue.set_danal_card_order_autocancel_from_url(tradeid);
			 		
			 		/* 다날 카드 자동 취소 2020-04-10 */
			 		Coupon_fcm_queue.set_danal_card_autocancel_from_url(tradeid);
			 		
			 		/* 다날 모바일 자동 취소 2020-04-10 */
			 		Coupon_fcm_queue.set_danal_mobile_order_autocancel_from_url(tradeid);
			 		
			 		
					COUPON_SEND.hasStarted1 = false;
					COUPON_SEND.check_order_number.remove(seq);
					
					
				} 				
 				
 			}
 		};
 		m_timer1.schedule(m_task1, 0,order_timer);
 	}
 	
 	
 	/* timer machine 2 호기 */
 	static void timer_machine2(final String seq,final String tradeid, final String mobilid,final String prdtprice){
 		
 		m_timer2 = new Timer();
 		m_task2 = new TimerTask(){
 			
 			/* 주문 초기 진입시 무조건 초기 값은 0 값입니다. 승인시 "2"로 선언하고, 거절시 "2"로 선언 됩니다. */
 			String order_result = "0";
 			
 			boolean is_order2 = false;
 			boolean is_auto_cancel2 = false;
 			boolean is_order_done2 = false;
 			public void run() {
 				hasStarted2 = true;
 			    
 				order_result = Coupon_fcm_queue.check_order_result(seq);
 				
 				/* 주문 승인*/
 				if(order_result.equals("1")){
 					/* 타이머 2 을 중지 합니다. */
 					m_timer2.cancel();
 					
 					/* 머신을 중지 합니다. */
 					hasStarted2 = false;
 					
 					/* 주문이 승인되었다고 선언 */
 					is_order2 = true;
 					
 					System.out.println(seq + ", "+tradeid+ "승인 되었습니다. 두번째 머신을 종료 합니다. ");
 					count2 = 0;
 					COUPON_SEND.hasStarted2 = false;
 					COUPON_SEND.check_order_number.remove(seq);
 				}

 				is_order_done2 = order_result.equals("2")||order_result.equals("3")||order_result.equals("4")||order_result.equals("5");
 				/* 주문 거절 */
 				if(is_order_done2){
 					/* 타이머 2 을 중지 합니다. */
 					m_timer2.cancel();
 					
 					/* 머신을 중지 합니다. */
 					hasStarted2 = false;
 					
 					/* 주문이 거절 되었다고 선언 */
 					is_order2 = false;
 					System.out.println(seq + ", "+tradeid+ "거절 되었습니다. 두번째 머신을 종료 합니다. ");
 					count2 = 0;
 					COUPON_SEND.hasStarted2 = false;
 					COUPON_SEND.check_order_number.remove(seq);
 				}
 				
 				if(count2<try_order_count&&hasStarted2){
 					count2++;
 					System.out.println(seq + ", "+tradeid+ "두번째 머신이 주문을 시도 합니다. " + count2 + " 번째 아직 주문이 아직 승인되지 않았습니다 !!! ");
 					Coupon_fcm_queue.set_notification(seq,Integer.toString(count2));
 					
 				}
				
				if (count2 >= try_order_count&&hasStarted2) {
					
					System.out.println(tradeid+"주문을 안 받다니 도저히 참을 수 없네요! 주문을 자동 취소(ad : auto_deny) 합니다.");
					
					/* 타이머 2 을 중지 합니다. */
 					m_timer2.cancel();
 					hasStarted2 = false;
					/* 자동 취소 되었습니다.  라고 선언*/
					is_auto_cancel2 = true;
					
					set_auto_cancel(seq,mobilid,prdtprice);
			 		/* 2018-12월 버전으로 자동 취소 url로 요청 하는 것
			 		 * 
			 		 * http://cashq.co.kr/adm/ext/kgmobilians/card/cancel/cn_cancel_result.php
			 		 * */
			 		Coupon_fcm_queue.set_kgorder_autocancel_from_url(tradeid,mobilid,prdtprice);
			 		
			 		/* 모바일 자동 취소 url로 요청 하는 것
			 		 * 
			 		 * http://cashq.co.kr/adm/ext/kgmobilians/mobile/cancel/cancel_result.php
			 		 * */
			 		Coupon_fcm_queue.set_kgmobile_order_autocancel_from_url(tradeid,mobilid,prdtprice);

			 		/* 다날 카드 자동 취소 2020-04-10 */
			 		Coupon_fcm_queue.set_danal_card_autocancel_from_url(tradeid);
			 		
			 		/* 다날 모바일 자동 취소 2020-04-10 */
			 		Coupon_fcm_queue.set_danal_mobile_order_autocancel_from_url(tradeid);
			 		
					count2 = 0;
					COUPON_SEND.hasStarted2 = false;
					COUPON_SEND.check_order_number.remove(seq);
					
					
				} 				
 				
 			}
 		};
 		m_timer2.schedule(m_task2, 0,order_timer);
 	}
 	
 	

 	

 	/* timer machine 3 호기 */
 	static void timer_machine3(final String seq,final String tradeid, final String mobilid,final String prdtprice){
 		
 		m_timer3 = new Timer();
 		m_task3 = new TimerTask(){
 			
 			/* 주문 초기 진입시 무조건 초기 값은 0 값입니다. 승인시 "3"로 선언하고, 거절시 "3"로 선언 됩니다. */
 			String order_result = "0";
 			
 			boolean is_order3 = false;
 			boolean is_auto_cancel3 = false;
 			
 			boolean is_order_done3 = false;
 			
 			
 			public void run() {
 				hasStarted3 = true;
 			    
 				order_result = Coupon_fcm_queue.check_order_result(seq);
 				
 				/* 주문 승인*/
 				if(order_result.equals("1")){
 					/* 타이머 3 을 중지 합니다. */
 					m_timer3.cancel();
 					
 					/* 머신을 중지 합니다. */
 					hasStarted3 = false;
 					
 					/* 주문이 승인되었다고 선언 */
 					is_order3 = true;
 					System.out.println(seq + ", "+tradeid+ "승인 되었습니다. 세번째 머신을 종료 합니다. ");
 					count3 = 1;
 					COUPON_SEND.hasStarted3 = false;
 					COUPON_SEND.check_order_number.remove(seq);
 				}
 				

 				is_order_done3 = order_result.equals("2")||order_result.equals("3")||order_result.equals("4")||order_result.equals("5");
 				
 				/* 주문 거절 */
 				if(is_order_done3){
 					/* 타이머 3 을 중지 합니다. */
 					m_timer3.cancel();
 					
 					/* 머신을 중지 합니다. */
 					hasStarted3 = false;
 					
 					/* 주문이 거절 되었다고 선언 */
 					is_order3 = false;
 					System.out.println(seq + ", "+tradeid+ "거절 되었습니다. 세번째 머신을 종료 합니다. ");
 					count3 = 0;
 					COUPON_SEND.hasStarted3 = false;
 					COUPON_SEND.check_order_number.remove(seq);
 				}
 				
 				if(count3<try_order_count&&hasStarted3){
 					count3++;
 					System.out.println(seq + ", "+tradeid+ "세번째 머신이 주문을 시도 합니다. " + count3 + " 번째 아직 주문이 아직 승인되지 않았습니다 !!! ");
 					Coupon_fcm_queue.set_notification(seq,Integer.toString(count3));
 					
 				}
				
				if (count3 >= try_order_count&&hasStarted3) {
					
					System.out.println(tradeid+"주문을 안 받다니 도저히 참을 수 없네요! 주문을 자동 취소(ad : auto_deny) 합니다.");
					
					/* 타이머 3 을 중지 합니다. */
 					m_timer3.cancel();
 					
 					hasStarted3 = false;
 					
					/* 자동 취소 되었습니다.  라고 선언*/
					is_auto_cancel3 = true;
					
					set_auto_cancel(seq,mobilid,prdtprice);
			 		/* 2018-12월 버전으로 자동 취소 url로 요청 하는 것
			 		 * 
			 		 * http://cashq.co.kr/adm/ext/kgmobilians/card/cancel/cn_cancel_result.php
			 		 * */
			 		Coupon_fcm_queue.set_kgorder_autocancel_from_url(tradeid,mobilid,prdtprice);
			 		
			 		/* 모바일 자동 취소 url로 요청 하는 것
			 		 * 
			 		 * http://cashq.co.kr/adm/ext/kgmobilians/mobile/cancel/cancel_result.php
			 		 * */
			 		Coupon_fcm_queue.set_kgmobile_order_autocancel_from_url(tradeid,mobilid,prdtprice);
			 		
			 		/* 다날 카드 자동 취소 2020-04-10 */
			 		Coupon_fcm_queue.set_danal_card_autocancel_from_url(tradeid);
			 		
			 		/* 다날 모바일 자동 취소 2020-04-10 */
			 		Coupon_fcm_queue.set_danal_mobile_order_autocancel_from_url(tradeid);
			 		
					count3 = 0;
					COUPON_SEND.hasStarted3 = false;
					COUPON_SEND.check_order_number.remove(seq);
					
					

				} 				
 				
 			}
 		};
 		
 		/* 타이머 머신 세번째, 0초 부터 시작 10초 간격으로 실행.*/
 		m_timer3.schedule(m_task3, 0,order_timer);
 	}
 	


 	

 	/* timer machine 4 호기 */
 	static void timer_machine4(final String seq,final String tradeid, final String mobilid,final String prdtprice){
 		
 		m_timer4 = new Timer();
 		m_task4 = new TimerTask(){
 			
 			/* 주문 초기 진입시 무조건 초기 값은 0 값입니다. 승인시 "4"로 선언하고, 거절시 "4"로 선언 됩니다. */
 			String order_result = "0";
 			
 			boolean is_order4 = false;
 			boolean is_auto_cancel4 = false;
 			boolean is_order_done4 = false;
 			
 			public void run() {
 				hasStarted4 = true;
 			    
 				order_result = Coupon_fcm_queue.check_order_result(seq);
 				
 				/* 주문 승인*/
 				if(order_result.equals("1")){
 					/* 타이머 4 을 중지 합니다. */
 					m_timer4.cancel();
 					
 					/* 머신을 중지 합니다. */
 					hasStarted4 = false;
 					
 					/* 주문이 승인되었다고 선언 */
 					is_order4 = true;
 					
 					
 					System.out.println(seq + ", "+tradeid+ "승인 되었습니다. 네번째 머신을 종료 합니다. ");
 					count4 = 0;
 					COUPON_SEND.hasStarted4 = false;
 					COUPON_SEND.check_order_number.remove(seq);
 				}

 				is_order_done4 = order_result.equals("2")||order_result.equals("3")||order_result.equals("4")||order_result.equals("5");
 				
 				/* 주문 거절 */
 				if(is_order_done4){
 					/* 타이머 4 을 중지 합니다. */
 					m_timer4.cancel();
 					
 					/* 머신을 중지 합니다. */
 					hasStarted4 = false;
 					
 					/* 주문이 거절 되었다고 선언 */
 					is_order4 = false;
 					
 					 
 					set_order(seq,"2","");
 					System.out.println(seq + ", "+tradeid+ "거절 되었습니다. 네번째 머신을 종료 합니다. ");
 					count4 = 0;
 					COUPON_SEND.hasStarted4 = false;
 					COUPON_SEND.check_order_number.remove(seq);
 				}
 				
 				if(count4<try_order_count&&hasStarted4){
 					count4++;
 					System.out.println(seq + ", "+tradeid+ "네번째 머신이 주문을 시도 합니다. " + count4 + " 번째 아직 주문이 아직 승인되지 않았습니다 !!! ");
 					Coupon_fcm_queue.set_notification(seq,Integer.toString(count4));
 					
 				}
				
				if (count4 >= try_order_count&&hasStarted4) {
					
					System.out.println(tradeid+"주문을 안 받다니 도저히 참을 수 없네요! 주문을 자동 취소(ad : auto_deny) 합니다.");
					
					/* 타이머 4 을 중지 합니다. */
 					m_timer4.cancel();
 					
					/* 자동 취소 되었습니다.  라고 선언*/
					is_auto_cancel4 = true;
					
					/* 자동 최소에 대한 메서드 실행 */
					set_auto_cancel(seq,mobilid,prdtprice);

			 		/* 2018-12월 버전으로 자동 취소 url로 요청 하는 것
			 		 * 
			 		 * http://cashq.co.kr/adm/ext/kgmobilians/card/cancel/cn_cancel_result.php
			 		 * */
			 		Coupon_fcm_queue.set_kgorder_autocancel_from_url(tradeid,mobilid,prdtprice);
			 		
			 		/* 모바일 자동 취소 url로 요청 하는 것
			 		 * 
			 		 * http://cashq.co.kr/adm/ext/kgmobilians/mobile/cancel/cancel_result.php
			 		 * */
			 		Coupon_fcm_queue.set_kgmobile_order_autocancel_from_url(tradeid,mobilid,prdtprice);
					
			 		/* 다날 카드 자동 취소 2020-04-10 */
			 		Coupon_fcm_queue.set_danal_card_autocancel_from_url(tradeid);
			 		
			 		/* 다날 모바일 자동 취소 2020-04-10 */
			 		Coupon_fcm_queue.set_danal_mobile_order_autocancel_from_url(tradeid);
			 		
			 		count4 = 0;
					COUPON_SEND.hasStarted4 = false;
					COUPON_SEND.check_order_number.remove(seq);
					
					

				} 				
 				
 			}
 		};
 		
 		/* 네번째 타이머 머신을 0초 부터 시작 10초 간의 간격으로 시도 합니다. */
 		m_timer4.schedule(m_task4, 0,order_timer);
 	}
 	
 	/* 주문 승인·거절에 대한 메서드 */
 	static void set_order(String exam_num1,String exam_num2,String seq){
 		
 		/*주문 승인 */
 		if(exam_num1.equals("1"))
 		{
 			System.out.println("주문 "+exam_num1+" 메서드 실행");
 			Utils.getLogger().info("주문 "+seq+" 메서드 실행");
 		
 			
 		/*주문 거절 */
 		}else if(exam_num1.equals("2")){
 			System.out.println("주문 거절 "+exam_num1+" 메서드 실행");
 			Utils.getLogger().info("주문 "+seq+" 메서드 실행");
 			
 				
 		}
  	}
 	
 	
 	/* 자동 최소에 대한 메서드 */
 	static void set_auto_cancel(String seq, String mobilid,String prdtprice){
 		System.out.println("자동 취소 메서드 실행");
 		Utils.getLogger().info("주문 번호 `"+seq+"` 자동 취소 메서드 실행");
 		
 		/* ordtake.pay_status='ad' 로 변경  디비 갱신 */
 		Coupon_fcm_queue.update_delivery_cancel(seq);
 		
 		/* 자동 취소 알림톡을 보냅니다. */
		Coupon_fcm_queue.send_arlimtalk("4", seq);
 		/* 알림도 요청 할 것 */
 		
 	}
 	

 }


