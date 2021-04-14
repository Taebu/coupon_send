package kr.co.cashq.coupon_send;
/**
 * PreparedStatement의 디자인패턴을 위한 클래스임.
 * closePstmt 의 검색결과 건수는
 * openPstmt 검색결과 건수와 동일해야 함.
 */

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 데이터 핸들링 관련 디자인 패턴과 관련된다.
 * <br>try{openPstmt}
 * <br>catch{}
 * <br>finally{closePstmt}
 * @author pgs
 * 
 */
public class MyDataObject {

	private PreparedStatement pstmt = null;

	private ResultSet rs = null;

	String pstmt_str = null;

	/**
	 * 프리페어드스테이트먼트를 내부적으로 할당한다.
	 * @param str
	 * @throws SQLException
	 */
	public void openPstmt(String str) throws SQLException {
		pstmt_str = str;
		pstmt = DBConn.getConnection().prepareStatement(str);
	}

	/**
	 * 프리페이드스테이트먼트를 리턴한다.
	 * @return
	 */
	public PreparedStatement pstmt() {
		return pstmt;
	}

	/**
	 * ResultSet 및 PreparedStatemet를 close한다.
	 */
	public void closePstmt() {
		pstmtClose();
	}

	/**
	 * 
	 */
	private void pstmtClose() {
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS022";
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS023";
		}
		
		try {
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS024";
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS025";
		}
	}

	/**
	 * 객체가 소멸될 때 호출된다. 실수로 해제되지 않았던 자원도 해제하여 메모리 문제를 해결한다.
	 */
	@Override
	protected void finalize() throws Throwable {
		if (pstmt != null) {
			tryClose();//close하지 못한곳에서 메모리 릭을 방지한다.
			Utils.getLogger().warning("close되지 않은 pstmt =>" + pstmt_str);
			DBConn.latest_warning = "ErrPOS026";
		}
	}

	/**
	 * 리절트셋을 할당한다.
	 * @param p_rs
	 * @throws SQLException
	 */
	public void setRs(ResultSet p_rs) throws SQLException {
		if (rs != null) {
			rs.close();
			rs = null;
		}
		rs = p_rs;
	}

	/**
	 * 할당되었던 리절트셋을 리턴한다.
	 * @return
	 */
	public ResultSet rs() {
		return rs;
	}

	/**
	 * finally부분이 아닌곳에서 호출하는 디자인 패턴부에서 자원해제를 위한 용도로 활용한다.
	 */
	public void tryClose() {
		pstmtClose();
	}

	/**
	 * 처리된 갯수가 예상과 틀릴경우 오류메시지 문자열을 리턴합니다.
	 * @param realCount 실제 처리 갯수
	 * @param expectedCnt 예상되는 처리 갯수
	 * @return
	 */
	public String getWarning(int realCount, int expectedCnt) {
		String retVal;
		retVal=pstmt_str + "처리결과가 ["+expectedCnt+"]가 아닙니다.["+ realCount+"]";
		return retVal;
	}

}
