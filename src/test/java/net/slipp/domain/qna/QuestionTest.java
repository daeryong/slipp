package net.slipp.domain.qna;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import net.slipp.domain.tag.Tag;
import net.slipp.domain.user.SocialUser;
import net.slipp.repository.tag.TagRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

@RunWith(MockitoJUnitRunner.class)
public class QuestionTest {
	private Question dut;
	
	@Mock
	private TagRepository tagRepository;
	
	@Before
	public void setup() {
		dut = new Question();
	}
	
	@Test
	public void isWritedBy_sameUser() throws Exception {
		// given
		SocialUser user = new SocialUser(10);
		dut.writedBy(user);
		
		// when
		boolean actual = dut.isWritedBy(user);
		
		// then
		assertThat(actual, is(true));
	}
	
	@Test
	public void isWritedBy_differentUser() throws Exception {
		// given
		SocialUser user = new SocialUser(10);
		dut.writedBy(new SocialUser(11));
		
		// when
		boolean actual = dut.isWritedBy(user);
		
		// then
		assertThat(actual, is(false));
	}
	
	@Test
	public void newQuestion() throws Exception {
		// given
		SocialUser loginUser = new SocialUser();
		Tag java = new Tag("java");
		Question questionDto = QuestionFixture.createDto("title", "contents", "java javascript");
		when(tagRepository.findByName(java.getName())).thenReturn(java);
		
		// when
		Question newQuestion = Question.newQuestion(loginUser, questionDto, tagRepository);
		
		// then
		assertThat(newQuestion.getTitle(), is(questionDto.getTitle()));
		assertThat(newQuestion.getContents(), is(questionDto.getContents()));
		assertThat(newQuestion.hasTag(java), is(true));
		assertThat(newQuestion.getNewTags().size(), is(1));
	}
	
	@Test
	public void updateQuestion() throws Exception {
		// given
		SocialUser loginUser = new SocialUser();
		Tag java = new Tag("java");
		Question questionDto = QuestionFixture.createDto("title", "contents", "java javascript");
		when(tagRepository.findByName(java.getName())).thenReturn(java);
		Question newQuestion = Question.newQuestion(loginUser, questionDto, tagRepository);
		Question updatedQuestionDto = QuestionFixture.createDto("title2", "contents2", "java maven");
		
		// when
		newQuestion.update(loginUser, updatedQuestionDto, tagRepository);
		
		// then
		assertThat(newQuestion.getTitle(), is(updatedQuestionDto.getTitle()));
		assertThat(newQuestion.getContents(), is(updatedQuestionDto.getContents()));
		assertThat(newQuestion.hasTag(java), is(true));
		assertThat(newQuestion.getNewTags().size(), is(1));	
	}
	
	@Test
	public void tag() throws Exception {
		Tag tag = new Tag("newTag");
		Question question = new Question();
		question.tag(tag);
		
		Set<Tag> tags = Sets.newHashSet(tag);
		assertThat(question.getTags(), is(tags));
		assertThat(tag.getTaggedCount(), is(1));
	}
	
	@Test
	public void contents() throws Exception {
		String contents = "this is contents";
		dut.setContents(contents);
		assertThat(dut.getContents(), is(contents));
	}
	
	@Test
	public void connected() throws Exception {
		SocialUser loginUser = new SocialUser();
		loginUser.setProviderId("facebook");
		dut.writedBy(loginUser);
		String postId = "123456";
		SnsConnection actual = dut.connected(postId);
		
		SnsConnection expected = new SnsConnection(SnsType.facebook, postId);
		assertThat(actual, is(expected));
	}
}
