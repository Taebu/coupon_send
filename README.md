# order_send
order_send를  만든 목적
아이폰에서는 푸시를 날리면 앱이 알아 차려서 주문을 액티브 하는 모션을 취하지 않는다. 공교롭게도 앱이 실행중이고 실행중인 앱이 화면상에 활성화 되어 있을 때만 푸시를 알아 차린다.
따라서 주문이 들어 왔을 때 주문을 알아 차릴 수 있도록 주문을 승인 할 때 까지 알림 푸시를 5초 간격으로 전송한다. 만일 주문이 승인 되면 더 이상 주문 요청을 하지 않고 주문이 들어온 것을 계속 적으로 판단하는 프로세스를 구동한다.

프로그램 작성 요령
1. 주문이 들어 온다. 
/www/m/ajax_data/set_ordtake_bak.php
/www/ext/dyinfo/set_order2.php
에서 요청을 한다. 배달 중인 상태로 변경 된 건이

pay_status='1' (주문 승인)일 때, 
ordtake.pay_status='di' 로 변경한다.

2. 주문을 조회한다.
select * from ordtake where seq=?

3. 상점아이디를 조회한다.
select * from store where seq=?

4. 상점코드와 상태 메시지를 전달한다.
https://img.cashq.co.kr/api/token/set_token.php
store info
"주문이 도착 했습니다."
10초 마다 java에서 요청한다.

4-1. img.cashq.co.kr 에서 받은 요청에 데이터로 받은 정보는 데이터로서 주문에 대한 로그를 남긴다.
5. cashq.ordtake.up_time이 갱신된 값이 발생하지 않으면 5분간 모든 토큰에 10초 간격으로 계속 보낸다. 약 30회 발송.
- java에서 처리 dowork() -> Order_fcm_queue.doMainProcess();

6. 5분이 지나도 갱신이 되지 않을 시 미승인 자동 취소된다.
- java에서 처리 
Order_fcm_queue.update_delivery_complete();


7. 프로그램은 주문을 대기하는 상태로 돌아간다.(1. 반복, 프로그램 종료시 까지)

번외, 주문 이후 di상태에서 한시간이 지나도 상태가 미변경시 dc(delivery_complete)가 이벤트로서 실행된다.
```sql
> show create event `ev_ordtake`

******************** 1. row *********************
               Event: ev_ordtake
            sql_mode: 
           time_zone: SYSTEM
        Create Event: CREATE EVENT `ev_ordtake` ON SCHEDULE EVERY 1 MINUTE STARTS '2018-01-03 16:04:53' ON COMPLETION NOT PRESERVE ENABLE DO BEGIN
 update `cashq`.`ordtake` SET `pay_status`='dc',up_time=now() where date_add(insdate,interval 1 hour)<now() and pay_status='di';
  END
character_set_client: utf8
collation_connection: utf8_general_ci
  Database Collation: utf8_general_ci
1 rows in set
```

상세 상태
```sql
CREATE EVENT `ev_ordtake` 
ON SCHEDULE EVERY 1 MINUTE 
STARTS '2018-01-03 16:04:53' ON 
COMPLETION NOT 
PRESERVE ENABLE DO BEGIN
 update `cashq`.`ordtake` SET 
 `pay_status`='dc',
 up_time=now() 
 where date_add(insdate,interval 1 hour)<now()
  and pay_status='di';
```


## Java 수정 후 구동 되게 만들기
- 해당 경로 이동
> $cd /?

- 구동 확인 숫자가 나오면 멈추고 빌드 해야 한다.
> $sh ./status.sh

- 멈추는 명령 
> $sh ./stop.sh

- java to class
> $sh ./mask.sh

- order_send 재구동
> $sh ./startw.sh

- order_send 재구동 확인
> $sh ./status.sh
