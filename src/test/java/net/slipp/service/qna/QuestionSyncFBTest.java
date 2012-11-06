package net.slipp.service.qna;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;
import com.restfb.types.Post;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-applicationContext.xml")
public class QuestionSyncFBTest {
	private static Logger logger = LoggerFactory.getLogger(QuestionSyncFBTest.class);
	
	@Value("#{applicationProperties['my.facebook.accessToken']}")
	private String myAccessToken;
	
	private FacebookClient dut;

	@Before
	public void setup() {
		dut = new DefaultFacebookClient(myAccessToken);
	}
	
	@Ignore
	@Test
	public void post() throws Exception {
		String message = "글쓰기 테스트입니다.";
		FacebookType response = dut.publish("me/feed", FacebookType.class, 
			Parameter.with("message", message));
		String id = response.getId();
		logger.debug("id : {}", id);
		
		Post post = dut.fetchObject(id, Post.class);
		assertThat(post.getMessage(), is(message));
	}
}
