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
public class RegexTest {
	
	
	public static void main(String[] args) {
		
		
		Map<String, String> messageMap=new HashMap<String, String>();
		messageMap.put("store.name","악마족발");
		messageMap.put("store.tel","050-8515-0493");
		messageMap.put("agencyMember.point_items","3개 5,000원적립\n5개 10,000원적립\n10개 20,000원적립");

		String bt_content="[#{매장명}]을 이용해주셔서 감사합니다.\n";
		bt_content+="적립번호 : [#{050번호}]\n";
		bt_content+="\n";
		bt_content+="고객님에게 \"2,000원\" 적립 해드렸습니다.\n";
		bt_content+="\"배달톡톡\" 어플로 접속하시면 확인가능합니다.\n";
		bt_content+="\n";
		bt_content+="[새로운 미션내용]\n";
		bt_content+="#{미션내용}\n";
		bt_content+="적립기간 : 주문일 부터 60일 후 소멸\n";
		bt_content+="\n";
		bt_content+="요즘 트랜드에 맞게 배달음식을 주문 시 마다 현금적립 어플을 새롭게 출시 하였습니다.\n";
		bt_content+="고객님만을 위한 배달어플을 지금 확인 하세요!\n";
		bt_content+="\n";
		bt_content+="적립금 관련 궁금한 점은 1599-9495 으로 문의해 주세요\n";
		bt_content+="\n";
		bt_content+="\"배달톡톡\" 앱 포인트 확인 링크\n";
		bt_content+="http://bdtalk.co.kr/m/p/ ";

		String bt_regex="#{매장명}=store.name&#{050번호}=store.tel&#{미션내용}=agencyMember.point_items";		
		
		/* 템플릿을 정해진 패턴대로 변경 합니다. 
		 * @param bt_content 템플릿 내용, 
		 * @param bt_regex 템플릿 패턴
		 * @param messageMap 템플릿 패턴을 바꿀 내용
		 * */
		//String messages=chg_regexrule(bt_content,bt_regex, messageMap);
		//System.out.println(messages);
		
		String point_set="3_5000&5_10000&10_20000";
		String point_text=getPointSet(point_set);
		System.out.println(point_text);
		
		
	}

	/**
	 * @param point_set ="3_5000&5_10000&10_20000"
	 * @return
	 * 3개 5,000원
	 * 5개 10,000원
	 * 10개 20,000원
	 */
	private static String getPointSet(String point_set) {
		// TODO Auto-generated method stub
		String[] regex_array=point_set.split("&");
		String[] keys;
		String returnValue="";

		/* bt_regex 의 크기 만큼 반복하여 변환한다. */
		for (int i = 0; i < regex_array.length; i++) {
			keys=regex_array[i].split("_");
			returnValue=returnValue+keys[0]+"개 "+String.format("%,d", Integer.parseInt(keys[1]))+"원\n";
		}
		return returnValue;
	}

	/**
	 * @param bt_content
	 * 메세지 전문과 변환될 텍스트가 지정 되어 있습니다. 정해진 룰의 패턴이 지정 되어 있습니다.
	 *  패턴의 예 
	 *  #{매장명}을 이용해 주셔서 #{050번호}
	 * 
	 * @param bt_regex
	 *  룰의 규칙을 넣습니다. bt_content에서 선언한 #{키값}의 모든 패턴은 아래와 같이 모두 선언 되어 있어야 합니다.        
	 *  
	 *  예) 
	 *  #{매장명}=store.name&#{050번호}=store.tel
	 *  
	 *  라면 두개의 규칙이 존재하고 #{매장명}을 store.name의 맵의 키로 지정합니다.  
	 * @param messageMap
	 *  위에서 지정한 store.name의 키가 함수 호출전에 아래와 같은 형태로 정의 되어 인수로 들어가야 합니다.
	 *  Map<String, String> messageMap=new HashMap<String, String>();
		messageMap.put("store.name","태부치킨");
	 * @return
	 */
	private static String chg_regexrule(String bt_content, String bt_regex, Map<String, String> messageMap) {
		// TODO Auto-generated method stub
		String[] regex_array=bt_regex.split("&");
		String[] keys;
		/* bt_regex 의 크기 만큼 반복하여 변환한다. */
		for (int i = 0; i < regex_array.length; i++) {
			keys=regex_array[i].split("=");
			bt_content=bt_content.replace(keys[0], messageMap.get(keys[1]));
		}
		
		return bt_content;
	}

	

	
}
