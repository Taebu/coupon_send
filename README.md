# coupon_send
coupon_send를 만든 목적

주문시 1시간이 지난 시점으로
아래 4가지의 조건을 만족해야 한다.
첫번째, code_3021 Y 값이고,
두번째, code_3022 0이상의 값이며,
세번째, Tradeid가 적립예정인 주문 포인트값과 동일하며,
네번째, 현장현금, 현장카드 결제가 아니어야 한다. (신규 추가 항목)

그러면 위에 조건에 만족하는 결제가 성공했다면 code_30202의 값이 "주문포인트"로 적립된다.

프로그램 작성 요령
1. cashq.ordtake 테이블에서 배달 중(pay_status==di) 이며 주문 후 한 시간(date_add(up_time,interval 1 hour)<now())이 지난 주문을 조회 한다.

2. code_3021 이 Y이고, code_3022가 0이상이며, Tradeid가 적립 예정 로그와 값이 동일하며, 현장 결제가 아닌 경우.

2-1. 참이면, 바로결제 포인트를 적립해준다.

3. 배달완료로 전환한다.

4. 끝

## Java 수정 후 구동 되게 만들기
- 해당 경로 이동
> $cd /?

- 구동 확인 숫자가 나오면 멈추고 빌드 해야 한다.
> $sh ./status.sh

- 멈추는 명령 
> $sh ./stop.sh

- java to class
> $sh ./mask.sh

- coupon_send 재구동
> $sh ./startw.sh

- coupon_send 재구동 확인
> $sh ./status.sh
