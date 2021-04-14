package kr.co.cashq.coupon_send;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * cashq.SMSQ_SEND 테이블의 데이터 처리 업무처리를 수행한다.
 * 
 * @author pgs
 * 
 */
public class Smsq_send {

	public Smsq_send() {

	}

	/**
	 * SKTL 성공시 SMS 메시지를 전송한다. DB입력성공시 true를 리턴한다.
	 * 
	 * @param sms_phones
	 *            : 전화번호들을 콤마로 구분하여 여러개 보낼 수 있다.
	 * 
	 */
	public static boolean sendSuccessMsg(String sms_phones) {
		boolean retVal = false;
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();

		sb.append("insert into cashq.SMSQ_SEND set "
				+ "msg_type='S', dest_no=?,call_back=?,msg_contents=?, sendreq_time=now()");

		String msg = "SKTL Server alive~[" + Env.getInstance().CORP_CODE + "]"
				+ DBConn.latest_warning;
		msg = Utils.substring(msg, 0, 80);

		try {
			dao.openPstmt(sb.toString());

			String[] phones = sms_phones.split(",");

			for (int i = 0; i < phones.length; i++) {
				int n = 1;
				dao.pstmt().setString(n++, phones[i]);
				dao.pstmt().setString(n++, Env.getInstance().sms_send_phone);
				dao.pstmt().setString(n++, msg);

				dao.pstmt().addBatch();
				dao.pstmt().clearParameters();
			}

			int cnt = 0;
			int[] arr_i = dao.pstmt().executeBatch();
			for (int i = 0; i < arr_i.length; i++) {
				cnt += arr_i[i];
			}

			if (cnt != phones.length) {
				Utils.getLogger().warning(dao.getWarning(cnt, phones.length));
				DBConn.latest_warning = "ErrPOS067";
			} else {
				//Site_push_log.sendMsg(sms_phones, msg);
			}
			DBConn.latest_warning = "";
			retVal = true;
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS068";
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS069";
		} finally {
			dao.closePstmt();
		}
		return retVal;
	}

	/**
	 * SKTL 연결 실패시 SMS 메시지를 전송한다. DB입력성공시 true를 리턴한다.
	 * 
	 * @param sms_phones
	 * @return
	 */
	public static boolean sendFailMsg(String sms_phones) {
		boolean retVal = false;
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();

		sb.append("insert into cashq.SMSQ_SEND set "
				+ "msg_type='S', dest_no=?,call_back=?,msg_contents=?, sendreq_time=now()");
		String msg = "SKTL Server down!![" + Env.getInstance().CORP_CODE + "]"
				+ DBConn.latest_warning;
		msg = Utils.substring(msg,0, 80);

		try {
			dao.openPstmt(sb.toString());

			String[] phones = sms_phones.split(",");
			for (int i = 0; i < phones.length; i++) {
				int n = 1;
				dao.pstmt().setString(n++, phones[i]);
				dao.pstmt().setString(n++, Env.getInstance().sms_send_phone);
				dao.pstmt().setString(n++, msg);

				dao.pstmt().addBatch();
				dao.pstmt().clearParameters();
			}

			int cnt = 0;
			int[] arr_i = dao.pstmt().executeBatch();
			for (int i = 0; i < arr_i.length; i++) {
				cnt += arr_i[i];
			}

			if (cnt != phones.length) {
				Utils.getLogger().warning(dao.getWarning(cnt, phones.length));
				DBConn.latest_warning = "ErrPOS070";
			} else {
				//Site_push_log.sendMsg(sms_phones, msg);
			}
			retVal = true;
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS071";
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS072";
		} finally {
			dao.closePstmt();
		}
		return retVal;
	}
}
