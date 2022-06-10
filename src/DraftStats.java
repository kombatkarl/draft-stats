import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

/*
 * Created on Sep 27, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

/**
 * AUTHOR : karl coleman
 * DATE   : Sep 27, 2004
 * VERSION:
 * 
 * Class Description Here
 */
public class DraftStats {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException {
		String name;
		String teamid;
		String leagueid;
		int stint;
		int ab;
		int doubles;
		int triples;
		int homeruns;
		int walks;
		int ibb;
		int strikeouts;
		int sb;
		int year;
		double avg;
		double obp;
		double slg;
		BigDecimal ops;
		BigDecimal ots;
		
		double ip;
		BigDecimal h9;
		BigDecimal b9;
		BigDecimal br9;
		BigDecimal k9;
		int saves;
		int hits;
		double era;
		
		if(args.length != 2) {
			System.out.println("Invalid # of arguments. Must supply year " +
					"and file prefix.");
			System.exit(1);
		}
		
		String season = args[0];
		String filePrefix = args[1];
		
		System.out.println("season: " + season);
		
		Connection conn;
		Statement stmt = null;
		Statement stmt2 = null;
		ResultSet rs;
		ResultSet rs2;
		DecimalFormat avgFormatter = new DecimalFormat("0.000");
		DecimalFormat pFormatter = new DecimalFormat("#0.0");
		File batterFile = new File("DraftStats/batters.txt");
		File bstatFile = new File("DraftStats/" + filePrefix + "-batterstats.txt");
		File pitcherFile = new File("DraftStats/pitchers.txt");
		File pstatFile = new File("DraftStats/" + filePrefix + "-pitcherstats.txt");
		String line;
		String [] nameParts;
		String id;
		BufferedReader bReader = null;
		BufferedWriter bWriter = null;
		BufferedReader pReader = null;
		BufferedWriter pWriter = null;
		int lastnameLength;
		String sql = null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
	        conn = DriverManager.getConnection("jdbc:mysql://localhost/lahmansbaseballdb", "root", "6qrjwy380rie5h");
	        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

			stmt2 = conn.createStatement();
			bWriter = new BufferedWriter(new FileWriter(bstatFile));
			bReader = new BufferedReader(new FileReader(batterFile));
			while((line = bReader.readLine()) != null) {
				nameParts = line.split(",");
				lastnameLength = nameParts[1].length() < 5 ? nameParts[1].length() : 5;
				id = (nameParts[1].replaceAll("'","").replaceAll(" ", "").substring(0,lastnameLength) + nameParts[0].substring(0,2)).toLowerCase();
				if(nameParts.length == 3) {
					id += nameParts[2];
				}
				sql = "SELECT DISTINCT PLAYERID from BATTING WHERE PLAYERID LIKE '"+id+"%' " +
						"AND YEARID >= " + season;
				rs = stmt.executeQuery(sql);
				if(!rs.next()) {
					System.out.println("Player not found in DB as given:" + line.replaceAll(",", " ") + " : " + id);
				} else {
					id = rs.getString("PLAYERID");
					if(rs.next()) {
						System.out.println("Ambiguous Player Name:" + line.replaceAll(",", " "));
					} else {
						System.out.println(id);
						sql = "SELECT concat(nameFirst,' ',nameLast) as NAME " +
								"FROM Batting, People " +
								"Where People.playerID = Batting.playerID and Batting.playerID = '"+id+"' " +
								"AND yearID >= " + season + " limit 1";
						rs = stmt.executeQuery(sql);
						rs.next();
						name = rs.getString("NAME");
						sql = "SELECT nameFirst, nameLast, stint, yearID, teamID, lgID, AB, 2B, 3B, " +
								"HR, SB, BB, SO, IBB, ROUND(H/AB, 3) as AVG, " +
								//"ROUND(([H]+[BB]+[HBP])/([AB]+[BB]+[HBP]+[SF]), 3) AS OBP, " +
								"ROUND((H+BB+IFNULL(HBP, 0))/(AB+BB+IFNULL(HBP, 0)), 3) AS OBP, " +
								"ROUND(((H-2B-3B-HR)+(2B*2)+(3B*3)+(HR*4))/AB, 3) AS SLG " +
								"FROM Batting, People " +
								"Where People.playerID = Batting.playerID and Batting.playerID = '"+id+"' " +
								"AND yearID >= " + season +
								" ORDER BY Batting.yearID, Batting.stint ";
						rs = stmt.executeQuery(sql);
						bWriter.write(name+"\r\n");
						bWriter.write("Year Stnt tm  lg  ab   2b  3b  hr  bb  ibb   k   sb   avg      obp     slg     ops     ots    positions\r\n");
						while(rs.next()) {
							year = rs.getInt("yearID");
							stint = rs.getInt("stint");
							teamid = rs.getString("teamID");
							leagueid = rs.getString("lgID");
							ab = rs.getInt("AB");
							doubles = rs.getInt("2B");
							triples = rs.getInt("3B");
							homeruns = rs.getInt("HR");
							walks = rs.getInt("BB");
							ibb = rs.getInt("IBB");
							strikeouts = rs.getInt("SO");
							sb = rs.getInt("SB");
							if(ab > 0) {
								avg = rs.getDouble("AVG");
								obp = rs.getDouble("OBP");
								slg = rs.getDouble(("SLG"));
								ops = new BigDecimal(rs.getDouble("OBP")).
										add(new BigDecimal(rs.getDouble("SLG")));
								ots = new BigDecimal(rs.getDouble("OBP")).
										multiply(new BigDecimal(rs.getDouble("SLG")));
							} else {
								avg = new Double(0.0);
								obp = new Double(0.0);
								slg = new Double(0.0);
								ops = new BigDecimal(0.0);
								ots = new BigDecimal(0.0);
							}
							String row = year+"  "+stint+"  "+ teamid+"  "+leagueid+"  "+addSpaces(""+ab,5)+addSpaces(""+doubles,4)+addSpaces(""+triples,4)+addSpaces(""+homeruns,4)+addSpaces(""+walks,5)+addSpaces(""+ibb,4)+addSpaces(""+strikeouts,5)+addSpaces(""+sb,4)+avgFormatter.format(avg)+"    "+avgFormatter.format(obp)+"   "+avgFormatter.format(slg)+"   "+avgFormatter.format(ops)+"   "+avgFormatter.format(ots)+"   ";
							sql = "select * from Fielding where Fielding.playerID = '" + id + "' " +
								"and Fielding.yearID = " + year;
							rs2 = stmt2.executeQuery(sql);
							while(rs2.next()) {
								row += rs2.getString("POS") + "-" + rs2.getInt("G") + " ";
							}
							bWriter.write(row + "\r\n");
						}
						bWriter.write("-----------------------------------------------------------------------------------------------------\r\n");
					}
				}
			}
			
			pWriter = new BufferedWriter(new FileWriter(pstatFile));
			pReader = new BufferedReader(new FileReader(pitcherFile));
			while((line = pReader.readLine()) != null) {
				nameParts = line.split(",");
				lastnameLength = nameParts[1].length() < 5 ? nameParts[1].length() : 5;
				id = (nameParts[1].replaceAll("'","").replaceAll(" ", "").substring(0,lastnameLength) + nameParts[0].substring(0,2)).toLowerCase();
				sql = "SELECT DISTINCT PLAYERID from PITCHING WHERE PLAYERID LIKE '"+id.replaceAll("'", "''")+"%' " +
						"AND YEARID >= " + season;
				rs = stmt.executeQuery(sql);
				if(!rs.next()) {
					System.out.println("Player not found in DB as given:" + line.replaceAll(",", " ") + " : " + id);
				} else {
					id = rs.getString("PLAYERID");
					if(rs.next()) {
						System.out.println("Ambiguous Player Name:" + line.replaceAll(",", " "));
					} else {
						System.out.println(id);
						sql = "SELECT concat(nameFirst,' ',nameLast) as NAME " +
								"FROM Pitching, People " +
								"Where People.playerID = Pitching.playerID and Pitching.playerID = '"+id+"' " +
								"AND yearID >= " + season + " limit 1";
						rs = stmt.executeQuery(sql);
						rs.next();
						name = rs.getString("NAME");
						sql = "SELECT nameFirst, nameLast, stint, yearID, teamID, lgID, Round(IPOuts/3,1) as IP, " +
								"H, BB, SO, HR, ERA, SV " +
								"FROM Pitching, People " +
								"Where People.playerID = Pitching.playerID and Pitching.playerID = '"+id+"' " +
								"AND yearID >= " + season +
								" ORDER BY Pitching.yearID, Pitching.stint ";
						rs = stmt.executeQuery(sql);
						pWriter.write(name+"\r\n");
						pWriter.write("Year Stnt tm  lg  IP       H   BB    SO  HR  SV  H/9   BB/9  BR/9  K/9   ERA\r\n");
						while(rs.next()) {
							try {
								year = rs.getInt("yearID");
								stint = rs.getInt("stint");
								teamid = rs.getString("teamID");
								leagueid = rs.getString("lgID");
								ip = rs.getDouble("IP");
								hits = rs.getInt("H");
								walks = rs.getInt("BB");
								strikeouts = rs.getInt("SO");
								homeruns = rs.getInt("HR");
								saves = rs.getInt("SV");
								if(ip > 0) {
								h9 = new BigDecimal(hits*9/ip);
								b9 = new BigDecimal(walks*9/ip);
								k9 = new BigDecimal(strikeouts*9/ip);
								br9 = h9.add(b9);
								} else {
									h9 = new BigDecimal(0.0);
									b9 = new BigDecimal(0.0);
									k9 = new BigDecimal(0.0);
									br9 = new BigDecimal(0.0);
								}
								era = rs.getDouble("ERA");
								pWriter.write(year+"  "+stint+"  "+ teamid+"  "+leagueid+"  "+addSpaces(""+ip,8)+addSpaces(""+hits,5)+addSpaces(""+walks,5)+addSpaces(""+strikeouts,5)+addSpaces(""+homeruns,4)+addSpaces(""+saves,4)+addSpaces(pFormatter.format(h9),6)+addSpaces(pFormatter.format(b9),6)+addSpaces(pFormatter.format(br9),6)+addSpaces(pFormatter.format(k9),6)+era+"\r\n");
							} catch(SQLException e) {
								continue;
							}
						}
						pWriter.write("-------------------------------------------------------------------------------------------\r\n");
					}
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			System.out.println(sql);
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bReader.close();
				bWriter.close();			
				if(pReader != null) pReader.close();
				if(pWriter != null) pWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}
	
	private static String addSpaces(String string, int desiredLength) {
		int len = string.length();
		if(len > desiredLength) return string.substring(desiredLength);
		for(int i=0;i<desiredLength-len;i++) {
			string += " ";
		}
		return string;
	}
}
