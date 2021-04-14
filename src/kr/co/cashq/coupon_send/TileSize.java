/**
 * 
 */
package kr.co.cashq.coupon_send;

/**
 * @author Taebu
 *
 */
public class TileSize {
 static int TILESIZE=8;
 int wholeTitlesCount;
 int partTitlesCount;
 int width;
 int height;
 int widthTilesCount;
 int heightTilesCount;
 int widthRemainder;
 int heightRemainder;
 
 public static void main(String[] args){
	 TileSize ts=new TileSize();
	 ts.width=100;
	 ts.height=120;
	ts.widthTilesCount=ts.width/TILESIZE; 
	ts.heightTilesCount=ts.height/TILESIZE;
	ts.wholeTitlesCount=ts.widthTilesCount*ts.heightTilesCount;
	ts.widthRemainder=ts.width-(ts.widthTilesCount*TILESIZE);
	ts.heightRemainder=ts.height-(ts.heightTilesCount*TILESIZE);
	
	if(ts.widthTilesCount>0 && ts.heightRemainder>0)
	{
		ts.partTitlesCount=(ts.width/TILESIZE)+(ts.height/TILESIZE)+1;
		
	}else{
		if(ts.widthRemainder>0&&ts.heightRemainder==0){
			ts.partTitlesCount=ts.height/TILESIZE;
		}else{
			if(ts.widthRemainder==0&&ts.heightRemainder>0){
				ts.partTitlesCount=ts.width/TILESIZE;
			}else{
				ts.partTitlesCount=0;
			}
			
		}
	}
	System.out.println(ts.wholeTitlesCount);
	System.out.println(ts.partTitlesCount);
 }
}
