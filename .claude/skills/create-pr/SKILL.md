---
name: create-pr
description: WEDU 백엔드 변경을 커밋·푸시하고 draft PR 을 생성한다. 브랜치/커밋 컨벤션과 PR 템플릿을 따르며, main 직접 push 를 막고 feature 브랜치로 유도한다. "PR 만들어줘", "PR 올려", "commit and PR" 등에 활성화.
---

# create-pr — WEDU PR 생성 스킬

WEDU 백엔드의 변경사항을 컨벤션에 맞게 커밋하고 **draft PR** 로 올린다.

## 전제 확인 (순서대로)

0. **작업 전 이슈 먼저 생성.** 작업 시작 전(코드·문서·설정 무엇을 바꾸든) GitHub 이슈를 만들고
   (작업 목적·범위 기술), 그 번호를 기억한다. 이슈 없이 PR 을 만들지 않는다. PR 본문 `이슈: Closes #<번호>` 로 연결해 머지 시 자동으로
   닫히게 한다.
1. **main 직접 작업 금지.** 현재 브랜치가 `main` 이면 중단하고, 변경 내용을 담을 `<type>/<요약>`
   브랜치를 만들어 옮긴 뒤 진행한다. `type` 은 커밋 타입과 동일하게 `feat|fix|docs|refactor|test|chore`
   중 하나(예: `feat/product-search`, `chore/coderabbit-config`). (main 은 브랜치 보호로 push 가 막혀 있다.)
2. **빌드 통과 확인.** `./gradlew clean build` 가 성공해야 PR 을 만든다. 실패하면 먼저 고친다.
3. **비밀값 점검.** `git status` / staged diff 에 secret·비밀번호·토큰·`application-secret.yml`·`.env`
   가 섞이지 않았는지 확인한다. 있으면 중단하고 알린다.

## 커밋

- 스테이징은 변경 파일을 명시적으로 추가하고, `git status` 로 포함 목록을 눈으로 확인한다. `git add -A`/`.` 지양.
- 커밋 메시지 형식: `feat|fix|docs|refactor|test|chore: 요약 (WEDU-XXX)`
  - 본문에는 무엇을·왜 바꿨는지만. 사람 이름/핸들·점수·불필요한 메타 금지.
- `Co-Authored-By` 등 트레일러를 임의로 붙이지 않는다.

## 푸시 & PR

1. `git push -u origin <branch>`
2. PR 은 **항상 draft** 로 생성한다:
   ```bash
   gh pr create --draft --base main --title "<type>: <요약> (WEDU-XXX)" --body-file <임시본문파일>
   ```
3. PR 본문은 `.github/PULL_REQUEST_TEMPLATE.md` 구조를 채운다. 해당 없는 섹션은 `- N/A`.
   - 작업 내용(`이슈: Closes #<번호>`·기능 ID·도메인), As-is/To-be, 체크리스트, 테스트 방법, 참고.
   - 체크리스트 항목(빌드 통과·계층 책임·테스트 추가·비밀값 없음·id 참조)을 실제 확인한 것만 체크.

## 마무리

- 생성된 PR URL 을 사용자에게 전달한다.
- push/PR 후 임의로 ready-for-review 전환하거나 병합하지 않는다. 리뷰·전환은 사람이 한다.
