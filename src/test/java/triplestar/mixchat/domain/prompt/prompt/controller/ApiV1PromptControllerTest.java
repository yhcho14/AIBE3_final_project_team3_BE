package triplestar.mixchat.domain.prompt.prompt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import triplestar.mixchat.domain.member.member.constant.MembershipGrade;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.entity.Password;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.prompt.prompt.constant.PromptType;
import triplestar.mixchat.domain.prompt.prompt.dto.PromptReq;
import triplestar.mixchat.domain.prompt.prompt.entity.Prompt;
import triplestar.mixchat.domain.prompt.prompt.repository.PromptRepository;
import triplestar.mixchat.global.security.CustomUserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import triplestar.mixchat.domain.member.member.constant.Country;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiV1PromptControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MemberRepository memberRepository;
    @Autowired PromptRepository promptRepository;
    @Autowired PasswordEncoder passwordEncoder;

    Member premiumMember;
    Member basicMember;

    // TestMemberFactory 사용 시도했지만, 오류 미해결로 인해 임시로 사용
    // Todo: @WithMockUser와 TestMemberFactory 병행 사용 시 SecurityContext 충돌 문제 해결
    @BeforeEach
    void setUp() {
        promptRepository.deleteAll();
        memberRepository.deleteAll();
        premiumMember = memberRepository.save(Member.builder()
                .email("premium@example.com")
                .password(Password.encrypt("test1234", passwordEncoder))
                .name("테스트닉네임")
                .nickname("PREMIUM")
                .country(Country.SOUTH_KOREA)
                .englishLevel(triplestar.mixchat.domain.member.member.constant.EnglishLevel.INTERMEDIATE)
                .interest("테스트소개")
                .description("테스트관심사")
                .build());
        premiumMember.changeMembershipGrade(MembershipGrade.PREMIUM);
        premiumMember = memberRepository.save(premiumMember); // 등급 변경 후 저장

        basicMember = memberRepository.save(Member.builder()
                .email("basic@example.com")
                .password(Password.encrypt("test1234", passwordEncoder))
                .name("테스트닉네임")
                .nickname("BASIC")
                .country(Country.SOUTH_KOREA)
                .englishLevel(triplestar.mixchat.domain.member.member.constant.EnglishLevel.INTERMEDIATE)
                .interest("테스트소개")
                .description("테스트관심사")
                .build());
        basicMember.changeMembershipGrade(MembershipGrade.BASIC);
        basicMember = memberRepository.save(basicMember); // 등급 변경 후 저장
    }

    void setAuth(Member member) {
        CustomUserDetails userDetails = new CustomUserDetails(member.getId(), member.getRole());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("프리미엄 회원이 프롬프트 생성에 성공")
    void createPrompt_success() throws Exception {
        setAuth(premiumMember);
        PromptReq req = new PromptReq("테스트 프롬프트", "프롬프트 내용입니다.", PromptType.CUSTOM.name());
        ResultActions result = mockMvc.perform(post("/api/v1/prompt/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.title").value("테스트 프롬프트"));
    }

    @Test
    @DisplayName("베이직 회원은 프롬프트 생성에 실패")
    void createPrompt_fail_basicUser() throws Exception {
        setAuth(basicMember);
        PromptReq req = new PromptReq("테스트 프롬프트", "프롬프트 내용입니다.", PromptType.CUSTOM.name());
        ResultActions result = mockMvc.perform(post("/api/v1/prompt/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));
        result.andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("프리미엄 회원이 본인 프롬프트를 수정에 성공")
    void updatePrompt_success() throws Exception {
        setAuth(premiumMember);
        Prompt prompt = promptRepository.save(Prompt.create(premiumMember, "수정 프롬프트", "내용", PromptType.CUSTOM.name()));
        prompt = promptRepository.save(prompt);
        PromptReq req = new PromptReq("수정된 프롬프트", "수정된 내용입니다.", PromptType.CUSTOM.name());
        ResultActions result = mockMvc.perform(put("/api/v1/prompt/update/" + prompt.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.title").value("수정된 프롬프트"));
    }

    @Test
    @DisplayName("프리미엄 회원이 타인 프롬프트 수정에 실패")
    void updatePrompt_fail_notOwner() throws Exception {
        setAuth(premiumMember);
        Prompt prompt = promptRepository.save(Prompt.create(premiumMember, "타인 프롬프트", "내용", PromptType.CUSTOM.name()));
        prompt = promptRepository.save(prompt);
        PromptReq req = new PromptReq("수정 시도", "내용", PromptType.CUSTOM.name());
        ResultActions result = mockMvc.perform(put("/api/v1/prompt/update/" + prompt.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));
        result.andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("프리미엄 회원이 본인 프롬프트 삭제에 성공")
    void deletePrompt_success() throws Exception {
        setAuth(premiumMember);
        Prompt prompt = promptRepository.save(Prompt.create(premiumMember, "삭제 프롬프트", "내용", PromptType.CUSTOM.name()));
        prompt = promptRepository.save(prompt);
        ResultActions result = mockMvc.perform(delete("/api/v1/prompt/delete/" + prompt.getId()));
        result.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("프리미엄 회원이 타인 프롬프트 삭제에 실패")
    void deletePrompt_fail_notOwner() throws Exception {
        setAuth(premiumMember);
        Prompt prompt = promptRepository.save(Prompt.create(premiumMember, "타인 프롬프트", "내용", PromptType.CUSTOM.name()));
        prompt = promptRepository.save(prompt);
        ResultActions result = mockMvc.perform(delete("/api/v1/prompt/delete/" + prompt.getId()));
        result.andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("베이직 회원은 PRE_SCRIPTED 프롬프트 목록만 조회")
    void listPrompt_basicUser() throws Exception {
        setAuth(basicMember);
        promptRepository.save(Prompt.create(basicMember, "스크립트1", "내용1", PromptType.PRE_SCRIPTED.name()));
        promptRepository.save(Prompt.create(basicMember, "스크립트2", "내용2", PromptType.PRE_SCRIPTED.name()));
        promptRepository.save(Prompt.create(basicMember, "커스텀", "내용3", PromptType.CUSTOM.name()));
        ResultActions result = mockMvc.perform(get("/api/v1/prompt"));
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$[0].title").value("스크립트1"));
    }

    @Test
    @DisplayName("프리미엄 회원은 본인 커스텀 프롬프트까지 목록 조회")
    void listPrompt_premiumUser() throws Exception {
        setAuth(premiumMember);
        Prompt pre1 = promptRepository.save(Prompt.create(premiumMember, "스크립트1", "내용1", PromptType.PRE_SCRIPTED.name()));
        Prompt pre2 = promptRepository.save(Prompt.create(premiumMember, "스크립트2", "내용2", PromptType.PRE_SCRIPTED.name()));
        Prompt custom = promptRepository.save(Prompt.create(premiumMember, "커스텀", "내용3", PromptType.CUSTOM.name()));
        custom = promptRepository.save(custom); // 멤버 할당 후 저장
        ResultActions result = mockMvc.perform(get("/api/v1/prompt"));
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$[2].title").value("커스텀"));
    }

    @Test
    @DisplayName("프리미엄 회원이 본인 커스텀 프롬프트 상세조회에 성공")
    void detailPrompt_success() throws Exception {
        setAuth(premiumMember);
        Prompt prompt = promptRepository.save(Prompt.create(premiumMember, "상세 프롬프트", "상세 내용", PromptType.CUSTOM.name()));
        prompt = promptRepository.save(prompt);
        ResultActions result = mockMvc.perform(get("/api/v1/prompt/" + prompt.getId()));
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.title").value("상세 프롬프트"));
    }

    @Test
    @DisplayName("프리미엄 회원이 타인 커스텀 프롬프트 상세조회에 실패")
    void detailPrompt_fail_notOwner() throws Exception {
        setAuth(premiumMember);
        Prompt prompt = promptRepository.save(Prompt.create(premiumMember, "타인 프롬프트", "내용", PromptType.CUSTOM.name()));
        prompt = promptRepository.save(prompt);
        ResultActions result = mockMvc.perform(get("/api/v1/prompt/" + prompt.getId()));
        result.andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("베이직 회원은 커스텀 프롬프트 상세조회에 실패")
    void detailPrompt_fail_basicUser() throws Exception {
        setAuth(basicMember);
        Prompt prompt = promptRepository.save(Prompt.create(basicMember, "상세 프롬프트", "상세 내용", PromptType.CUSTOM.name()));
        prompt = promptRepository.save(prompt);
        ResultActions result = mockMvc.perform(get("/api/v1/prompt/" + prompt.getId()));
        result.andExpect(status().is4xxClientError());
    }
}
