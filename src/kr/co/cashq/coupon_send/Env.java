package kr.co.cashq.coupon_send;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

/**
 * 환경변수 관련 객체
 * @author pgs
 *
 */
public class Env {

	public String CORP_CODE = "";//업체코드 4자리
	public static final String NULL_TEL_NUMBER = "1234567890";
	public static final int MINUTE = 60;//1분 = 60초
	
	public static final String CALL_LOG_WORK_1000 = "1000";//접속 요청
	public static final String CALL_LOG_WORK_2000 = "2000";//접속 요청 응답
	public static final String CALL_LOG_WORK_1031 = "1031";//CallLog 전송
	public static final String CALL_LOG_WORK_2031 = "2031";//CallLog 전송 응답
	public static final String CALL_LOG_WORK_7777 = "7777";//HEART_BEAT 요청
	public static final String CALL_LOG_WORK_7778 = "7778";//HEART_BEAT 요청 응답
	public static final String SPACE = " ";//스페이스 한개
	public static final String CALL_LOG_RET_0000 = "0000";//성공처리_정상
	public static final String CALL_LOG_RET_0001 = "0001";//재전송요청_제휴업체 측에서 필요에 따라 해당 Call Log의 재전송 요청시 사용
	public static final String CALL_LOG_RET_0002 = "0002";//로그온실패 중복 로그인 이거나 로그인을 하지 않은 경우이거나임.

	public boolean USE_FILTER = false;//콜로그에 착신번호가 없는 로그가 들어오는 경우 입력하지 않을경우 true로 설정함.
	
	public static String confirmSafen = "";
	public static String confirmSafen_in = "";
	
	public String CALL_LOG_SERVER_IP = "";
	public int CALL_LOG_SERVER_PORT = 0;
	
	public String CON_DRIVER;
	public String CON_DOMAIN;
	public String CON_PORT;
	public String CON_USER;
	public String CON_DBNM;
	public String CON_PWD;
	
	public String sms_use;
    
	public String sms_phones;
    
	public String sms_send_phone;

	public String sms_success_week_days;
	
	public String sms_success_hour;
	
	public String sms_use_push_log;
	
	private static Env Instance = null;
	private Env() {
		Instance = this;
	}
	
	/**
	 * 환경변수 인스턴스를 static한 방법으로 가져온다.
	 * @return
	 */
	public static Env getInstance() {
		if(Instance == null)
		{
			Instance = new Env();
			
			Properties properties = loadEnvFile();
		    
		    Instance.CORP_CODE = Instance.readEnv(properties,"corp_code","");
		    Instance.CALL_LOG_SERVER_IP = Instance.readEnv(properties,"call_log_server_ip","");
		    Instance.CALL_LOG_SERVER_PORT = Integer.parseInt(Instance.readEnv(properties,"call_log_server_port",""));
		 
		    Instance.CON_DRIVER = Instance.readEnv(properties, "dbcon_driver","");
		    Instance.CON_DOMAIN = Instance.readEnv(properties,"dbcon_ip","");
		    Instance.CON_PORT = Instance.readEnv(properties,"dbcon_port","");
		    Instance.CON_DBNM = Instance.readEnv(properties,"dbcon_dbname","");
		    Instance.CON_USER = Instance.readEnv(properties,"dbcon_user","");
		    Instance.CON_PWD = Instance.readEnv(properties,"dbcon_pwd","");
		    Instance.USE_FILTER = "1".equals(Instance.readEnv(properties,"use_filter",""));
		    
		    updateSmsProperties(properties);
		    
		    properties = null;
		    
		}
		else {
			if("".equals(Instance.CORP_CODE) || Instance.CORP_CODE == null) 
			{
				throw new RuntimeException("env.xml file path error please param path");
			}
		}
		return Instance;
	}

	/**
	 * sms愿��젴�맂 �솚寃쎈��닔瑜� 媛��졇�삩�떎.
	 * @param prop
	 */
	private static void updateSmsProperties(Properties prop) {
		
		Properties properties = prop;		
	    Instance.sms_use = Instance.readEnv(properties,"sms_use","");
	    Instance.sms_phones = Instance.readEnv(properties,"sms_phones","");
	    Instance.sms_send_phone = Instance.readEnv(properties,"sms_send_phone","");
	    Instance.sms_success_week_days = Instance.readEnv(properties, "sms_success_week_days","");
	    Instance.sms_success_hour = Instance.readEnv(properties,"sms_success_hour","");
	    Instance.sms_use_push_log = Instance.readEnv(properties,"sms_use_push_log","");
		
	}

	/**
	 * sms관련된 환경변수를 가져온다.
	 * @param prop
	 */
	private static Properties loadEnvFile() {
		Properties properties = null;	

		try {
			properties = new Properties();
			
			properties.loadFromXML(new FileInputStream(COUPON_SEND.strPrePath+ "env.xml"));
		} catch (InvalidPropertiesFormatException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			properties = null;
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			properties = null;
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			properties = null;
		}
		
		if(properties == null) {
			throw new NullPointerException();
		}
		
		return properties;
	}

	/**
	 * 환경변수를 읽는다.
	 * @param properties
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String readEnv(Properties properties,String key, String defaultValue)
	{
		String val = "";
		val = (String)properties.get(key);
		
		if(val == null || "".equals(val)) 
		{
			val = defaultValue;
		}
		
		return val;
	}

}
