package kr.co.cashq.coupon_send;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database 연결 객체
 * @author pgs
 *
 */
public class DBConn {
	public static String latest_warning = "";
	private static String CON_DRIVER = "com.mysql.jdbc.Driver";
	private static String CON_DOMAIN = "localhost";
	private static String CON_PORT = "3306";
	private static String CON_DBNM = "";
	private static String CON_USER = "";
	private static String CON_PWD = "";

	private static Connection dbCon = null;
	public static java.sql.Statement stmt = null;

	public DBConn() {
		dbConCheck();
	}

	/**
	 * DB가 연결된 상태인지 체크하여 연결되지 않았으면 연결한다.
	 * 또한 연결이 끊어진 경우 다시 연결을 시도한다.
	 * 하지만 DB가 안올라온 경우 계속 연결을 시도하지만 연결이 안되므로 유의해야 한다.
	 * @throws SQLException
	 */
	public static void dbConCheck() {
		try {
			if (dbCon == null) {
				
		        CON_DRIVER = Env.getInstance().CON_DRIVER;
		        CON_DOMAIN = Env.getInstance().CON_DOMAIN;
		        CON_PORT = Env.getInstance().CON_PORT;
		        CON_DBNM = Env.getInstance().CON_DBNM;
		        CON_USER = Env.getInstance().CON_USER;
		        CON_PWD = Env.getInstance().CON_PWD;
			 	
				Class.forName(CON_DRIVER);
				dbCon = DriverManager.getConnection(CON_STR(), CON_USER,
						CON_PWD);
				stmt = dbCon.createStatement();
			} else {
				if (dbCon.isClosed()) {
					Class.forName(CON_DRIVER);
					dbCon = DriverManager.getConnection(CON_STR(), CON_USER,
							CON_PWD);
					stmt = dbCon.createStatement();
				}
			}
		} catch (SQLException sqex) {
			//Utils.logput(sqex.getMessage());
			Utils.getLogger().warning(sqex.getMessage());
			DBConn.latest_warning = "ErrPOS013";
			dbCon = null;
		} catch (ClassNotFoundException sqex) {
			//Utils.logput(sqex.getMessage());
			Utils.getLogger().warning(sqex.getMessage());
			DBConn.latest_warning = "ErrPOS014";
			dbCon = null;	
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS015";
			dbCon = null;
			throw new RuntimeException(DBConn.latest_warning);
		}
	}
	
	/**
	 * jdbc:mysql://localhost:3306/sktl
	 * 와 같은식으로 리턴한다.
	 * @return
	 */
	private static String CON_STR() {
		return "jdbc:mysql://" + CON_DOMAIN + ":" + CON_PORT + "/" + CON_DBNM+"?zeroDateTimeBehavior=convertToNull";
	}

	/**
	 * DB 컨넥션객체를 리턴한다.
	 * @return
	 * @throws Exception 
	 */
	public static Connection getConnection() {
		dbConCheck();
		return dbCon;
	}

	/**
	 * 프로그램 정상종료시 한 번만 close하길 권장함.
	 */
	public static void close() {
		try {
			if (dbCon != null) {
				if (dbCon.isClosed() == false) {
					dbCon.close();
				}
			}
		} catch (SQLException e) {
			//Utils.logput(e.getMessage());
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS016";
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS017";
		}
		dbCon = null;
	}

	/**
	 * safen_cmd_hist_YYYYMM테이블의 존재성을 판단한 후
	 * 존재하지 않으면 테이블을 생성한다.
	 * @param table_name
	 * @return history_table_name
	 */
	public static String isExistTableYYYYMM(String table_name) 
	{
		boolean isExistLogTable = false;
		String hist_table = table_name + "_" + Utils.getYYYYMM();
		isExistLogTable = isExistTable(hist_table);
		if (isExistLogTable == false)
		{			
			try {
				stmt.execute("create table cashq." + hist_table
						+ " as select * from cashq." + table_name+" limit 0");//테이블만 생성하고 데이터는 옮기지 않는다.
			} catch (SQLException e) {
				Utils.getLogger().warning(e.getMessage());
				DBConn.latest_warning = "ErrPOS018";
				e.printStackTrace();
			}
			catch (Exception e) {
				Utils.getLogger().warning(e.getMessage());
				Utils.getLogger().warning(Utils.stack(e));
				DBConn.latest_warning = "ErrPOS019";
			}
		}
		return hist_table;
	}

	/**
	 * 테이블이 존재하는지 조사한 후 조사하면 true를 리턴한다.
	 * @param tname
	 * @return
	 */
	public static boolean isExistTable(String tname) {
		StringBuffer sb = new StringBuffer();

		boolean exi = false;

		sb.append("select exists(SELECT 1 FROM information_schema.tables ");
		sb.append(" WHERE table_schema= '");
		sb.append(CON_DBNM);
		sb.append("' AND table_name = ?) a");

		MyDataObject dao = new MyDataObject();
		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, tname);
			
			
			dao.setRs(dao.pstmt().executeQuery());
			if (dao.rs().next()) {
				exi = dao.rs().getInt(1) == 1;
			}			
		} catch (SQLException e) {
			e.printStackTrace();
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS020";
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS021";
		}
		finally {
			dao.closePstmt();
		}

		return exi;
	}

}
