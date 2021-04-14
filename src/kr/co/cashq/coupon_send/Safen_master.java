package kr.co.cashq.coupon_send;

import java.sql.SQLException;

//import com.nostech.safen.SafeNo;

/**
 * safen_master 테이블 관련 객체
 * @author pgs
 *
 */
public class Safen_master {

	/**
	 * 마스터테이블에 착신번호를 등록하거나, 취소한다.
	 * 
	 * @param safen0504
	 * @param safen_in010
	 * @param mapping_option
	 *            1:등록, 2:취소
	 */
	public static void update_safen_master(String safen0504,
			String safen_in010, int mapping_option) {

		MyDataObject dao = new MyDataObject();
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("update safen_master set safen_in=?, status_cd=?, update_dt=now() where safen=?");

			dao.openPstmt(sb.toString());
			if (mapping_option == 1) {// 등록
				dao.pstmt().setString(1, safen_in010);
				dao.pstmt().setString(2, "u");// used
				dao.pstmt().setString(3, safen0504);
			} else if (mapping_option == 2) {// 취소
				dao.pstmt().setString(1, Env.NULL_TEL_NUMBER);
				dao.pstmt().setString(2, "e");// enabled
				dao.pstmt().setString(3, safen0504);
			}

			int cnt = dao.pstmt().executeUpdate();
			if(cnt!=1) {
				Utils.getLogger().warning(dao.getWarning(cnt,1));
				DBConn.latest_warning = "ErrPOS041";
			}

		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS042";
			e.printStackTrace();
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS043";
		}
		finally {
			dao.closePstmt();
		}
	}

	/**
	 * 안심번호의 그룹번호를 리턴한다.
	 * 
	 * @param safen0504
	 * @return
	 */
	public static String getGroupCode(String safen0504) {
		String strGrp = "";

		MyDataObject dao = new MyDataObject();
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select group_cd from safen_master where safen=?");
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, safen0504);
			dao.setRs(dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				strGrp = dao.rs().getString("group_cd");
			}

			dao.closePstmt();

		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS044";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS045";
		}
		return strGrp;
	}

	/**
	 * 안심번호에따른 업체상점전화번호를 리턴한다.
	 * 
	 * @param safen0504
	 * @return
	 */
	public static String getAccount_cd(String safen0504) {
		String strAccount_cd = "";

		MyDataObject dao = new MyDataObject();

		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select account_cd from safen_master where safen=?");
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, safen0504);

			dao.setRs(dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				strAccount_cd = dao.rs().getString("account_cd");
			}

			dao.closePstmt();

		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS046";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS047";
		}
		return strAccount_cd;
	}

	/**
	 * 안심번호에 따른 착신번호를 리턴한다.
	 * @param safen0504
	 * @param strHint
	 * @return
	 */
	public static String getSafen_in(String safen0504, String strHint) {
		String strSafen_in = "";

		MyDataObject dao = new MyDataObject();
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select safen_in from safen_master where safen=?");
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, safen0504);
			dao.setRs(dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				strSafen_in = dao.rs().getString("safen_in");
			}

			dao.closePstmt();

		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS048";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS049";
		}

		if ("".equals(strHint) && Env.NULL_TEL_NUMBER.equals(strSafen_in)) {
			// 아무것도 안함.
		} else {
			if (Env.NULL_TEL_NUMBER.equals(strSafen_in)) {
				String strSafen_in2;
				strSafen_in2 = update_safen_master_from_server(safen0504);

				if (strSafen_in2.startsWith("0")) {
					strSafen_in = strSafen_in2;
				} else {
					strSafen_in = strHint;
				}
			}
		}
		return strSafen_in;
	}

	/**
	 * 안심번호에 따른 서버정보를 가져와서 DB에 갱신한다.
	 * @param safen0504
	 * @return
	 */
	private static String update_safen_master_from_server(String safen0504) {
		//SafeNo safeNo = new SafeNo();
		String retCode2 = null;
		try {
			//retCode2 = safeNo.SafeNoAsk(Env.getInstance().CORP_CODE, safen0504);// 조회

			if (-1 < retCode2.indexOf(",")) {
				String safen_in;
				safen_in = (retCode2.split(","))[0];

				StringBuilder sb = new StringBuilder();
				sb.append("update safen_master set safen_in=?,status_cd=?,update_dt=now() where safen=?");

				MyDataObject dao = new MyDataObject();
				dao.openPstmt(sb.toString());

				dao.pstmt().setString(1, safen_in);
				dao.pstmt().setString(2, "u");// used
				dao.pstmt().setString(3, safen0504);

				int cnt = 0;
				cnt = dao.pstmt().executeUpdate();
				if(cnt!=1) {
					Utils.getLogger().warning(dao.getWarning(cnt,1));
					DBConn.latest_warning = "ErrPOS050";
				}

				dao.closePstmt();
			} else {				
				StringBuilder sb = new StringBuilder();
				sb.append("update safen_master set safen_in=?,status_cd=?,update_dt=now() where safen=?");

				MyDataObject dao = new MyDataObject();
				dao.openPstmt(sb.toString());
				dao.pstmt().setString(1, Env.NULL_TEL_NUMBER);
				dao.pstmt().setString(2, "e");// enabled
				dao.pstmt().setString(3, safen0504);

				int cnt = 0;
				cnt = dao.pstmt().executeUpdate();
				if(cnt!=1) {
					Utils.getLogger().warning(dao.getWarning(cnt,1));
					DBConn.latest_warning = "ErrPOS051";
				}
				dao.closePstmt();
			}
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS052";
			e.printStackTrace();
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS053";
		}
		return null;
	}

	/**
	 * 안심번호 마스터 테이블의 초기상태인 경우나 어제 진행도중 오류가 발생한 경우의 데이터가 존재하면 서버의 상태를 가져와 갱신한다.
	 * update safen_master set status_cd='a';//이런 식으로 초기에 인스톨이 필요한다.
	 */
	public static void doWark2() {
		MyDataObject dao2 = new MyDataObject();

		StringBuilder sb2 = new StringBuilder();

		sb2.append("select safen from safen_master where status_cd='a' or status_cd='i' and update_dt<date_sub(now(),interval 1 day) limit 1");

		try {
			dao2.openPstmt(sb2.toString());

			dao2.setRs(dao2.pstmt().executeQuery());
			if (dao2.rs().next()) {
				String safen0504 = dao2.rs().getString("safen");
				update_safen_master_from_server(safen0504);
			}
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS054";
			e.printStackTrace();
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS055";
		}
		finally {
			dao2.closePstmt();
		}
	}

	/**
	 * 콜로그가 들어온 시간을 갱신한다. 가령 safen_master의 값이 작은것을 추리기 위함이다.
	 * 
	 * @param safen0504
	 */
	public static void dealed(String safen0504) {
		// 최근 사용된 전화번호인지를 판단하는 정보로 활용하기 위함이다.
		StringBuilder sb = new StringBuilder();
		sb.append("update safen_master set dealed_dt=now() where safen=?");

		MyDataObject dao = new MyDataObject();
		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, safen0504);
			int cnt = 0;
			cnt = dao.pstmt().executeUpdate();
			if(cnt!=1) {
				Utils.getLogger().warning(dao.getWarning(cnt,1));
				DBConn.latest_warning = "ErrPOS056";
			}
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS057";
		} 
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS058";
		} finally {
			dao.closePstmt();
		}
	}

}
