package kr.co.cashq.coupon_send;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;

/**
 * 콜로그관련된 업무로직을 처리한다. safen_cdr 테이블과 관련된다.
 * 
 * @author pgs
 * 
 */
public class CdrTrigger {
	public static int nLogonAndCon = 0;//0:최초(날짜가 바뀌면 0으로 변경함), 1: 연결 성공 그리고 로그인 성공, 2:연결실패 혹은 로그인 실패	
	public static String strYMD_success = "";//SMS전송한 날짜 매주 금요일 오전 10시에 한 번만 보냄<nLogonAndCon변수와 관계가 있다.>
	
	public static String strYMD_fail = "";//SMS전송실패한 날짜 매일 한 번 <nLogonAndCon 변수와 관계가 있다>
	
	private static final int PACKET_LENGTH_8192 = 8192;
	private static final int N149 = 149;
	private static final int N24 = 24;
	private Socket socket;
	private BufferedInputStream bis;
	private BufferedOutputStream bos;
	private static CdrTrigger Instance;
	private boolean isConnected = false;
	private boolean isLogon;

	/** Creates a new instance of Worker */
	private CdrTrigger() {
		socket = null;
		bis = null;
		bos = null;
	}

	/**
	 * 메세지를 보낸다.
	 * 
	 * @param message
	 */
	private void sendMessage(String message) {
		try {
			if (isConnected == false) {
				//connect2CallLogServer(Env.getInstance().CALL_LOG_SERVER_IP,	Env.getInstance().CALL_LOG_SERVER_PORT);
			}
			if (isConnected) {
				bos.write(message.getBytes());
				bos.flush();
				Utils.getLogger().info("send:[" + message + "]");
			}
		} catch (IOException ioe) {
			Utils.getLogger().warning(ioe.getMessage());
			DBConn.latest_warning = "ErrPOS001";
		}
	}

	/**
	 * 메세지를 수신한다.
	 * 
	 * @return
	 */
	public String receiveMessage() {
		StringBuffer strbuf = new StringBuffer();
		try {
			if (isConnected) {
				byte[] buf = new byte[PACKET_LENGTH_8192];

				int read = 0;
				if ((read = bis.read(buf)) > 0) {// 여기서 응답없는 현상이 생길 수 있다. 특히 두번째
													// 진입시
													// 그럴 수 있다.
					String str = new String(buf);
					Utils.getLogger().info("rcv:[" + str.trim() + "]");
					strbuf.append(new String(buf, 0, read));
				}
			}
		} catch (IOException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS002";
		}

		return strbuf.toString();
	}

	/**
	 * 콜로그서버와의 연결을 종료한다.
	 */
	public void disconnectCallLogServer() {
		if (isConnected) {
			try {
				bis.close();
				bos.close();
				socket.close();
				isConnected = false;
				isLogon = false;
				Utils.getLogger().info(":disconnect:");
			} catch (IOException e) {
				Utils.getLogger().warning(e.getMessage());
				DBConn.latest_warning = "ErrPOS003";
			}
		}
	}

	/**
	 * DB작업을 수행한다.
	 * 
	 * @param paramVal
	 */
	public void doDBWork(String paramVal) {
		packetProcess(paramVal);
	}

	/**
	 * @param strVal
	 * @throws NumberFormatException
	 */
	public void packetProcess(String strVal) throws NumberFormatException {
		String strHeader2;

	}

	private String parseCallLog(String str) {
		String v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11;

		int s = 0;
		v1 = str.substring(s, s = s + 20).trim();// 고유값20자리
		v2 = str.substring(s, s = s + 20).trim();// 발신번호
		v3 = str.substring(s, s = s + 20).trim();// 접속번호
		v4 = str.substring(s, s = s + 20).trim();// 착신번호
		v5 = str.substring(s, s = s + 14).trim();// 연결시작시간
		v6 = str.substring(s, s = s + 14).trim();// 연결종료시간
		v7 = str.substring(s, s = s + 14).trim();// 서비스시작시간
		v8 = str.substring(s, s = s + 14).trim();// 서비스종료시간
		v9 = str.substring(s, s = s + 6).trim();// 통화 시간
		v10 = str.substring(s, s = s + 6).trim();// 서비스 시간
		v11 = str.substring(s, s = s + 1).trim();// 통화결과
		/*
		 * 1:통화 성공, 2:착신 통화중, 3:착신 무응답, 4:착신측 회선부족, 5:착신번호 결번 혹은 유효하지않은 번호,
		 * 6:발/착신자 통화 연결 오류, B:착신 시도 중 발신측 호 종료, F:착신번호 없음
		 */
		String retVal = "";
		if (dbCallLogProcess(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11)) {
			retVal = Utils.paddingLeft(20, v1) + "0000";// 응답
		} else {
			retVal = Utils.paddingLeft(20, v1) + "0001";// 나중에 재전송해줄것을 당부하여 서버로
														// 다시보냄 DB처리를 하지 못하였음.
		}
		return retVal;
	}

	/**
	 * 서버에 다시보내주기를 요청하는 경우에는 false를 리턴함.
	 * 
	 * @param v1
	 * @param v2
	 * @param v3
	 * @param v4
	 * @param v5
	 * @param v6
	 * @param v7
	 * @param v8
	 * @param v9
	 * @param v10
	 * @param v11
	 * @return
	 */
	private boolean dbCallLogProcess(String v1, String v2, String v3,
			String v4, String v5, String v6, String v7, String v8, String v9,
			String v10, String v11) {

		boolean result = false;

		if (Env.getInstance().USE_FILTER == true && "".equals(v4)
				&& "F".equals(v11)) {
			// 아무것도 안함. 로그에 쌓지 않고 단지 마스터에 최근사용된날짜로 저장한다.
			result = true;
		} else {
			StringBuilder sb = new StringBuilder();
			/*
			sb.append("insert into safen_cdr(conn_sdt, conn_edt, conn_sec, service_sdt, ");
			sb.append(" service_edt, service_sec, safen, safen_in, safen_out, billsec,");
			sb.append(" unique_id, account_cd, calllog_rec_file, rec_file_cd, status_cd, create_dt) values("
					+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())");
			*/
			MyDataObject dao = new MyDataObject();// PreparedStatement pstmt;

			try {
				dao.openPstmt(sb.toString());

				int n = 1;

				String dt1 = null, dt2 = null, dt3 = null, dt4 = null;

				dt1 = Utils.toDate(v5);
				dt2 = Utils.toDate(v6);
				dt3 = Utils.toDate(v7);
				dt4 = Utils.toDate(v8);

				dao.pstmt().setString(n++, dt1);
				dao.pstmt().setString(n++, dt2);

				dao.pstmt().setInt(n++, Integer.parseInt(v9));
				dao.pstmt().setString(n++, dt3);
				dao.pstmt().setString(n++, dt4);
				dao.pstmt().setInt(n++, Integer.parseInt(v10));
				dao.pstmt().setString(n++, v3);// 0504번호
				dao.pstmt().setString(n++, v4);// 착신번호
				dao.pstmt().setString(n++, v2);// 발신번호
				dao.pstmt().setInt(n++,
						Integer.parseInt(v9) - Integer.parseInt(v10));
				dao.pstmt().setString(n++, v1);
				dao.pstmt().setString(n++, Safen_master.getAccount_cd(v3));// account_cd
				dao.pstmt().setString(n++, "");// calllog_rec_file
				dao.pstmt().setString(n++, "1");// rec_file_cd
				dao.pstmt().setString(n++, v11);

				int cnt = 0;
				cnt = dao.pstmt().executeUpdate();

				if (cnt == 1) {
					result = true;
				}
			} catch (SQLException e) {
				if (-1 < e.getMessage().indexOf("uplicate")) {// 중복인 경우 성공으로
																// 간주한다./Duplicate
																// entry ...를
																// 리턴하기
																// 때문에
					// 단, 업데이트를 수행한다.? 그냥 무시한다.
					result = true;
				} else {
					Utils.getLogger().warning(e.getMessage());
					DBConn.latest_warning = "ErrPOS009";
					e.printStackTrace();
					result = false;
				}
			} catch (Exception e) {
				Utils.getLogger().warning(e.getMessage());
				Utils.getLogger().warning(Utils.stack(e));
				DBConn.latest_warning = "ErrPOS010";
			} finally {
				dao.closePstmt();
			}

		}

		Safen_master.dealed(v3);

		return result;
	}

	public static CdrTrigger getInstance() {
		if (Instance == null) {
			Instance = new CdrTrigger();
		}
		return Instance;
	}

}
