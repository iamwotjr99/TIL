# JUnit, AssertJ 정리
#### 테스트 코드 없이 개발하던 시절, 기존 코드에 새로운 기능을 추가할 때마다 기존 로직이 깨지진 않을까 늘 불안했다.
#### 그래서 다음 프로젝트부터는 반드시 테스트 코드를 작성하며 개발하겠다고 마음먹었다.
#### 최근 프로젝트에서 테스트를 도입하며 JUnit과 AssertJ를 함께 사용하게 되었고, IntelliJ에서 두 라이브러리의 자동완성 기능이 함께 제공되는 것을 보고
#### 두 도구의 역할과 차이를 명확히 이해하고 써야겠다는 생각이 들어 이렇게 정리해본다.


## JUnit? AssertJ?
- JUnit: Java 진영의 대표적인 단위 테스트 프레임워크
- AssertJ : JUnit과 함께 사용하는 외부 assertion 라이브러리로, 더 나은 가독성과 다양한 검증 메서드를 제공
- assertion 라이브러리: 테스트 코드에서 기대하는 값과 실제 값을 비교해서, 테스트가 성공/실패 했는지 판단해주는 기능을 제공하는 도구

## 기본적인 사용 방식
```java
// JUnit5
import static org.junit.jupiter.api.Assertions.*;

@Test
void junitTest() {
    assertEquals(5, sum(2, 3));
    assertTrue(isValid());
}

// AssertJ 
import static org.assertj.core.api.Assertions.*;

@Test
void assertjTest() {
    assertThat(sum(2, 3)).isEqualTo(5);
    assertThat(isValid()).isTrue();
}
```

## 차이점
### 1. 가독성
```java
@Test
@DisplayName("방장은 승인 대기 중인 참여자를 승인할 수 있다.")
void givenParticipantApply_whenApproveParticipant_thenReturnApprovedParticipant() {
    // given
    Long studyGroupId = studyGroup.getId();
    Participant participant = Participant.apply(USER1_ID, studyGroupId);

    // when
    Participant approved = studyGroup.approveParticipant(HOST_ID, participant);

    // then
    // JUnit
    assertEquals(participant.userId(), approved.userId());
    assertEquals(participant.studyGroupId(), approved.studyGroupId());
    assertEquals(1, studyGroup.getParticipantSet().size());
    assertTrue(studyGroup.getParticipantSet().contains(approved));
    assertEquals(ParticipantStatus.APPROVED, approved.status());

    // AssertJ
    assertThat(approved.userId()).isEqualTo(participant.userId());
    assertThat(approved.studyGroupId()).isEqualTo(participant.studyGroupId());
    assertThat(studyGroup.getParticipantSet())
            .hasSize(1)
            .contains(approved);
    assertThat(approved.status()).isEqualTo(ParticipantStatus.APPROVED);
}
```
- JUnit은 문법이 간결하지만 조건이 많아질수록 읽기 어렵다.
- AssertJ는 메서드 체이닝 방식을 제공하기 때문에 가독성이 좋다.
### 2. 오류 메시지
```java
// JUnit
assertEquals(3, studyGroup.getParticipantSet().size());
/**
   JUnit 실패시  
   Expected :3
   Actual   :1
   <Click to see difference>
 **/

// AssertJ
assertThat(studyGroup.getParticipantSet())
        .hasSize(3)
/**
 AssertJ 실패시  
 Expected size: 3 but was: 1 in:
 [Participant[userId=2, studyGroupId=1, status=APPROVED]]
 **/
```
- JUnit은 기본 메시지는 제공하지만, 컬렉션의 상세 구조나 깊은 비교까지는 한계가 있다
- AssertJ는 상세하게 오류 메세지를 보여줘서 디버깅 시에 도움이 많이 된다.
### 3. 컬렉션 테스트
```java
// JUnit
assertEquals(1, studyGroup.getParticipantSet().size());
assertTrue(studyGroup.getParticipantSet().contains(approved));

// AssertJ
assertThat(studyGroup.getParticipantSet())
        .hasSize(1)
        .contains(approved);
```
- JUnit은 직접 메서드로 조합을 해야한다.
- AssertJ는 컬렉션 전용 메서드들을 제공해서 간결하고 가독성이 좋게 구성할 수 있다.
### 4. 확장성
```java
@Test
@DisplayName("방장이 승인 대기중이 아닌 참여자를 승인하려고 하면 예외를 던진다.")
void givenNotPendingParticipant_whenApproveParticipant_thenThrowException() {
    // given
    Participant[] invalids = {
            new Participant(3L, studyGroup.getId(), ParticipantStatus.APPROVED),
            new Participant(4L, studyGroup.getId(), ParticipantStatus.REJECTED),
            new Participant(5L, studyGroup.getId(), ParticipantStatus.CANCELED),
            new Participant(6L, studyGroup.getId(), ParticipantStatus.LEAVE),
            new Participant(7L, studyGroup.getId(), ParticipantStatus.KICKED)
    };

    // when
    // then
    for (Participant p : invalids) {
        // JUnit
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> studyGroup.approveParticipant(HOST_ID, p));
        assertEquals("대기중인 유저가 아닙니다.", exception.getMessage());

        // AssertJ
        assertThatThrownBy(() -> studyGroup.approveParticipant(HOST_ID, p))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("대기중인 유저가 아닙니다.");
    }
}
```
- JUnit은 Optional, 예외 메시지, 컬렉션 등 복합 구조를 검증할 때 코드가 복잡해지거나 가독성이 떨어질 수 있다.
- AssertJ는 다양한 타입을 지원하고 메서드 체이닝 방식으로 다양한 조건의 검증이 가능하다.

## 정리
#### 정리를 해보자면, 간단하게 검증하고 싶은건 JUnit을 사용해도 좋지만
#### 예외 처리, 컬렉션 검증, 메시지 비교 등 다양한 상황에서 AssertJ의 강점을 명확히 느낄 수 있었다.
#### 현재 프로젝트에서 테스트 코드를 적용해보니 단위 테스트 단계에서는 나의 코드에 대한 신뢰감과 안정감이 생겼다.
#### 앞으로는 JUnit과 AssertJ를 함께 활용해,
#### 기능을 추가하거나 수정할 때도 안심할 수 있는 테스트 환경을 만들어가려고 한다.

