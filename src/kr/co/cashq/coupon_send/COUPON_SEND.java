package kr.co.cashq.coupon_send;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * 안심번호 매핑 업무와 콜로그 업무 객체
 * 
 * @author pgs
 * 
 */
public class COUPON_SEND {
	
	public static String strPrePath = "";//경로가 입력된경우는 해당 경로를 로그나 환경변수에서 읽어오게 한다.
	
	private static final long N600000 = 600000;//10분 = 60만초
	// private static final boolean TEST_MODE = false;
	public static int heart_beat = 0;
	private static int call_log_skip_count = 60;
	private static Calendar pivotFutureTime;
	private static long loggedTime = 0;
	public static boolean hasStarted1,hasStarted2,hasStarted3,hasStarted4;
	public static String machine_number1_order_number = "",
			machine_number2_order_number= "",
			machine_number3_order_number= "",
			machine_number4_order_number= "";
	

	public static ArrayList<String> check_order_number = new ArrayList<String>();
	
	/* 생성자 constructor 기본 초기값 구성 */
	COUPON_SEND() {
		hasStarted1 = false;
		hasStarted2 = false;
		hasStarted3 = false;
		hasStarted4 = false;
		machine_number1_order_number ="";
		machine_number2_order_number ="";
		machine_number3_order_number ="";
		machine_number4_order_number ="";
		
	}
	
	// 프로세스의 시작점
	public static void main(String[] args) {
		
		if(0 < args.length)
		{
			strPrePath = args[0];
			strPrePath += File.separatorChar;
		}
		
		Utils.getLogger().info("COUPON_SEND program started!");
		try {
			terminate_this();// killme.txt파일이 존재하여도 프로그램구동시 정상구동되도록
							// killme.txt파일을 지우고 시작한다.
			doMainProcess();
			
			
			DBConn.close();
			CdrTrigger w = CdrTrigger.getInstance();
			w.disconnectCallLogServer();
			Utils.getLogger().info("COUPON_SEND program ended!");
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning += "ErrPOS062";
		} catch (Error e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning += "ErrPOS063";
		}
		
		if(!"".equals(DBConn.latest_warning))
		{
			BufferedWriter writer = null;
			File logFile = new File(strPrePath + "_error_log.txt");

            try {
				writer = new BufferedWriter(new FileWriter(logFile));
				
				writer.write(Utils.getYMD() + " " + DBConn.latest_warning+"\n");
				writer.flush();
			    
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}



	/**
	 * 메인프로세스
	 */
	private static void doMainProcess() {
		Thread this_thread = Thread.currentThread();
		try {
			synchronized (this_thread) {
				while (doWork()) {
					if (0 < heart_beat++) {
						if (DBConn.getConnection() != null) {
							Calendar nowTime = Calendar.getInstance();
							if (pivotFutureTime == null) {
								pivotFutureTime = nowTime;// 미래시간으로 셋팅됨
							}

							long delta_time = pivotFutureTime.getTimeInMillis() - nowTime.getTimeInMillis();

							/* 1분간격 혹은 5초*60이면 5분 간격으로 콜로그를 갱신한다. */
							if (60 <= call_log_skip_count++ || delta_time <= 0)
							{
								call_log_skip_count = 0;
								pivotFutureTime = nowTime;
								pivotFutureTime.add(Calendar.SECOND, 5);//5초 후로 셋팅한다.(기존 5초)
								doProcessCallLog();
							}

							if (heart_beat < Env.MINUTE) {
								this_thread.wait(1000);// 1초 대기
							} else if (heart_beat < Env.MINUTE * 2) {
								this_thread.wait(2000);// 2초 대기
							} else if (heart_beat < Env.MINUTE * 4) {
								this_thread.wait(3000);// 3초 대기
							} else if (heart_beat < Env.MINUTE * 8) {
								this_thread.wait(4000);// 4초 대기
							} else {// 그 외는 5초 대기
								this_thread.wait(5000);// 5초 대기
							}

							if (log10Minute()) {
								Utils.getLogger().info("COUPON_SEND alive");
							}

							/* doSMSProcess */
							/*
							if ("1".equals(Env.getInstance().sms_use)) {
								doSMSProcess();
							}
							*/

						} else {
							this_thread.wait(10000);// 10초 대기 db가 올라오기를 기다린다.
							Utils.getLogger().warning("DB서비스가 올라왔는지 확인하세요!");
							DBConn.latest_warning = "ErrPOS064";
						}
					}
				}
			}
		} catch (InterruptedException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS065";
			e.printStackTrace();
		}
	}

	/**
	 * SMS관련된 작업을 처리한다.
	 */
	private static void doSMSProcess() {
		if (CdrTrigger.nLogonAndCon == 1)// ;//0:최초(날짜가 바뀌면 0으로 변경함), 1: 연결
											// 성공 그리고 로그인 성공, 2:연결실패 혹은 로그인 실패
		{// 연결성공
			String strYMDH = Utils.getYMDH();
			if (!strYMDH.equals(CdrTrigger.strYMD_success)) {
				String[] arrWeekDays = Env.getInstance().sms_success_week_days
						.split(",");

				for (int i = 0; i < arrWeekDays.length; i++) {
					if(Utils.getWeekDay() == Integer.parseInt(arrWeekDays[i])) 
					{
						if(Env.getInstance().sms_success_hour.equals(Utils.getHH())) 
						{	
							Smsq_send.sendSuccessMsg(Env.getInstance().sms_phones);
							CdrTrigger.strYMD_success = Utils.getYMDH();
						}
					}
				}
			}
		} else {// 연결실패
			String strYMD = Utils.getYMD();
			
			if(!strYMD.equals(CdrTrigger.strYMD_fail))
			{
				Smsq_send.sendFailMsg(Env.getInstance().sms_phones);
				CdrTrigger.strYMD_fail = strYMD;
			}
			
		}
		// CdrTrigger.strYMD_success = "";//SMS전송한 날짜 매주 금요일 오전 10시에 한 번만
		// 보냄<nLogonAndCon변수와 관계가 있다.>
		// CdrTrigger.strYMD_fail="";
	}

	/***
	 * 10분만다 로그를 찍도록 하는 조건으로 10분이 흘렀는지를 체크한다.
	 * 
	 * @return
	 */
	private static boolean log10Minute() {
		boolean retVal = false;
		long ctime = System.currentTimeMillis();
		if (loggedTime < ctime) {
			loggedTime = ctime + N600000;
			retVal = true;
		}
		return retVal;
	}

	/**
	 * 콜로그 프로세스 수행
	 */
	private static void doProcessCallLog() {
		try {

			CdrTrigger w = CdrTrigger.getInstance();
			String strRetVal = "";

			// if (TEST_MODE == false) {
			//w.doLogon();
			
			//waitMiliSec(5000);
			
//			strRetVal = w.doMsgMain();
			w.doDBWork(strRetVal);

		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS066";
		}
	}

	/** n 밀리sec 동안 대기한다.
	 * 
	 */
	private static void waitMiliSec(int n5000) {
		Thread this_thread = Thread.currentThread();
		synchronized (this_thread) {
			try {
				this_thread.wait(n5000);// 5초간 대기한다.
			} catch (InterruptedException e) {
				Utils.getLogger().warning(e.getMessage());
				DBConn.latest_warning = "ErrPOS012";
				e.printStackTrace();
			}// 5초 대기함.
		}
	}

	/**
	 * 안전한 종료를 수행할지 판단한 후 무한루프를 진입해야 한다면 true를 리턴한다.
	 * 
	 * @return
	 */
	private static boolean doWork() {
		if (terminate_this()) {
			return false;
		} else {
			// /여기서 DB를 읽어서 작업한다.
			Coupon_fcm_queue.doMainProcess();
			return true;
		}
	}



	/**
	 * 종료를 수행할지 결정한다. killme.txt파일이 존재하면 안전한 종료를 수행한다. 처음 시작시에 호출되기도 하지만 죽이는 기능이
	 * 아니라 다음에 안전한 종료를 위한 안정적인 수행을 위한 방안이다.
	 * 
	 * @return
	 */
	private static boolean terminate_this() {
		boolean is_file_exist = false;
		// 종료를 명령하는 파일이 있으면 안전한 종료를 수행한다.
		File file = new File(COUPON_SEND.strPrePath + "killme.txt");
		is_file_exist = file.exists();
		if (is_file_exist) {
			is_file_exist = file.delete();
		}
		return is_file_exist;
	}

}
