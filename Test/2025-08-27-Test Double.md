## 1. 서비스레이어, 어떻게 테스트해야할까?
도메인 객체을 단위 테스트를 했을 땐, POJO 형태 였기에 어떤 의존성도 가지지 않아서 단위 테스트하기가 매우 수월했다.
하지만 서비스 객체을 단위 테스트할 때는 `@Autowired`로 여러 객체들과 엮여있기 때문에 "이 엮여있는 의존성들을 어떻게 처리해야하나?" 머리가 복잡했다.

## 2. Dummy? Stub? Fake? Mock? Spy?
서비스 객체가 의존하고 있는 실제 객체들을 테스트에서 의존하게 되면 단위 테스트가 아니다.
- **왜냐하면?**:
  - 단위 테스트는 독립적으로 실행되어야하고, 다른 외부 객체들에 의존하지 않아야한다.
  - 단위 테스트는 자주 반복적으로 실행되므로 빨라야한다.
 
그래서 서비스 객체처럼 외부 객체들에 의존을 하고 있는데, 단위 테스트를 해야한다면 실제 객체가 아닌 가짜 객체 즉, 대역(테스트 대역)이라는걸 이용해서 **의존성 격리**를 해야한다.  테스트 대역에는 "Dummy", "Stub", "Fake", "Mock", "Spy"이 있다.
- **Dummy**
  - 객체의 인스턴스화 된 건 필요하지만, 내부 기능은 필요하지 않을 때 사용
  - 함수 파라미터만 전달되는 용도로 사용
  - 어떤 동작도 하지 않음
```java
// 만약에 회원가입을 할 때 이메일을 보내서 정말 존재하는 이메일인지 확인을 하는 로직이 있다고 가정한다면
// 이를 테스트하기 위해서 실제로 이메일을 보낼 수는 없으니까 Service의 파라미터 전달 용로도 사용할 수 있다.
public class DummyVerificatedEmailSender implement VerificatedEmailSender {
    @Override
    public doSend(String targetEmail) {
        // 로직 X
    }
}

class AuthServiceTest {
    @Test
    void 회원가입_정보를_주면_확인_이메일을_보낸다() {
        // given
        SignUpRequestDto dto = new SignUpRequestDto("test@email.com", "password1234");
        
        // when
        AuthService authService = new AuthService(new DummyVerificatedEmailSender(), memberRepository);
        authService.signUp(dto);
        
        // then
        ...
    }
}
```
- **Stub**
  - Dummy 데이터가 실제로 동작하는 것처럼 만들어둔 객체
  - 테스트 중에 특정 메서드의 호출에 대한 응답만 필요해서 미리 반환할 값을 정의해놓고 사용
  - 정해진 값만 반환
```java
// PasswordEncoder를 대신하여 해당 메서드의 호출에 대한 응답만 필요해서 정해진 반환값만 반환
public class StubPasswordEncoder implements PasswordEncoder {
    @Override
    public String encode(CharSequence rawPassword) {
        return "encodedPassword";
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return true;
    }
}
```
- **Fake**
  - 실제 객체처럼 동작하는 객체
  - `Repository` 가 필요한데, 이때 실제 DB랑 연결된 `Repository`가 아닌 `HashMap` 같은 자료구조를 사용하는 `FakeRepository`를 이용
```java
// 실제 로직(In-memory 저장)을 가지는 가짜 객체
// 테스트 시나리오에 맞게 동작 제어를 위한 로직을 추가할 수도 있음
public class FakeMemberRepository implements MemberRepository{

    private final HashMap<Long, Member> store;
    private long sequence = 0L;

    public FakeMemberRepository() {
        this.store = new HashMap<>();
    }


    @Override
    public Member save(Member member) {
        if (member.getId() == null || member.getId() == 0) {
            sequence++;
            Member newMember = Member.from(sequence, member.getMemberInfo());
            store.put(sequence, newMember);
            return newMember;
        }

        store.put(member.getId(), member);
        return member;
    }

    @Override
    public Optional<Member> findById(Long userId) {
        return store.values().stream()
                .filter(m -> m.getId().equals(userId))
                .findFirst();
    }
}

class AuthServiceTest {
    @Test
    void 회원가입_정보를_주면_확인_이메일을_보낸다() {
        // given
        SignUpRequestDto dto = new SignUpRequestDto("test@email.com", "password1234");
        
        // when
        AuthService authService = new AuthService(new DummyVerificatedEmailSender(), new FakeMemberRepository());
        authService.signUp(dto);
        
        // then
        ...
    }
}

```
- **Mock**
  - Dummy 처럼 어떤 동작도 하지않지만 메서드의 호출을 기록
  - 특정 메서드 호출에 대한 여부와 호출 횟수를검증하기 위한 테스트 대역
```java
// 회원가입시에 확인 이메일을 보내는 메서드가 몇번 호출 했는지 검증할 수 있음
public class MockVerificatedEmailSender implement VerificatedEmailSender {
    public int doSendCallCnt = 0;
    @Override
    public doSend(String targetEmail) {
        // 실제 로직은 없지만 호출은 카운팅
        this.doSendCallCnt++;
    }
}

class AuthServiceTest {
    @Test
    void 회원가입_정보를_주면_확인_이메일을_보낸다() {
        // given
        SignUpRequestDto dto = new SignUpRequestDto("test@email.com", "password1234");
        
        // when
        MockVerificatedEmailSender mockEmailSender = new MockVerificatedEmailSender();
        AuthService authService = new AuthService(new MockVerificatedEmailSender(), memberRepository);
        authService.signUp(dto);
        
        // then
        // 실제로 한번만 호출이 됐는지 검증(행위기반테스트)
        assertThat(mockEmailSender.doSendCallCnt).isEqualTo(1);
    }
}
```
- **Spy**
  - 실제 객체와 동일하게 동작하면서, 메서드의 호출을 기록함 **Like 스파이~**
  - 실제 객체의 로직을 그대로 실행해야만 하는 경우에 유용함
```java
// 실제 객체와 동일하게 동작해야하므로 서비스에서 의존하는 추상객체의 구현체를 상속받아서 구현
// 실제로 어떤 메서드가 호출 됐는지 검증이 가능함
public class SpyMemberRepository extends MemberRepositoryImpl {

    public int saveCallCnt = 0;
    public int findByIdCallCnt = 0;

    public SpyMemberRepository(JpaMemberRepository jpaMemberRepository) {
        super(jpaMemberRepository);
    }

    @Override
    public Member save(Member member) {
        this.saveCallCnt++;
        return super.save(member);
    }

    @Override
    public Optional<Member> findById(Long userId) {
        this.findByIdCallCnt++;
        return super.findById(userId);
    }
}
```
## 3. 상태 기반 검증, 행위 기반 검증
여기서, `Stub`, `Fake` 처럼 결과물을 반환하는 테스트 대역이있고, `Mock`, `Spy` 처럼 메서드의 호출을 확인할 수 있는 테스트 대역이 있다.
- **상태 기반 검증**:
  - 테스트의 검증 동작에 **상태**를 사용하는 것
  - 테스트를 실행 한 후에 테스트 대상의 상태가 어떻게 변하는지에 따라 테스트 실행 결과를 판단
- **행위 기반 검증**:
  - 테스트의 검증 동작에 **메서드의 호출 여부**를 사용하는 것
  - 테스트 대상 뿐만 아니라, 테스트 대상과 협력하는 객체, 시스템의 메서드 호출 여부에 따라 테스트 실행 결과를 판단
```java
assertThat(loginResponse.token()).isEqualTo("access-token"); // 상태 기반 검증, 로그인 후 응답에 토큰이 진짜 맞는 토큰인지

verify(authenticationManager, times(1))
        .authenticate(any(UsernamePasswordAuthenticationToken.class)); // 행위 기반 검증, 로그인할 때 해당 클래스를 1번 호출했는지
verify(tokenProvider, times(1)).generateToken(mockAuthenticationObj); // 행위 기반 검증, 로그인할 때 토큰 생성 메서드를 1번 호출했는지
```

## 4. 고마워요! **"Mockito""**
이렇게 실제 객체가 아닌 **테스트 대역**을 만들면 **의존성을 격리**하여 서비스 객체처럼 다른 외부 객체들이나 시스템에 의존하고 있는 객체들을 단위 테스트를 할 수 있다. 하지만, 매번 테스트할 때마다 이렇게 많은 테스트 대역을 수동으로 만들면 배보다 배꼽이 더 커지는 상황이 생긴다.
- 모든 의존성에 대해 `Stub...`, `Fake..` 등등 클래스를 다 구현해야한다.
- 유연하게 작성하는데 비용이 많이든다.
- 행위 검증을 할 때 호출 횟수를 위한 `callCount` 변수를 추가하는 등 번거로운 작업을 해야한다.
이러한 불편함을 **Mockito**를 이용하면 테스트 대역들을 아주 쉽게 만들 수 있고, 행위 검증도 매우 쉽다.

## 5. 정리
Mockito 프레임워크를 이용하면 다양한 테스트 대역을 쉽게 만들 수 있고 테스트 검증을 할 수 있으므로 Mockito를 사용을 하면 좋다! 하지만, 모든 기술이 그렇듯이 기반 지식 없이 쌓은 기술에 대한 지식은 해당 기술이 변경되었을 때 지식이 무용지물이 된다고 생각한다. 그래서 한번 구현코드를 정리해봤다.
