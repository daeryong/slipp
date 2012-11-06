package net.slipp.service.user;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import net.slipp.domain.user.ExistedUserException;
import net.slipp.domain.user.SocialUser;
import net.slipp.repository.user.SocialUserRepository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class SocialUserServiceTest {
	@Mock
	private UsersConnectionRepository usersConnectionRepository;
	
	@Mock
	private ConnectionRepository connectionRepository;
	
	@Mock
	private SocialUserRepository socialUserRepository;
	
	@Mock
	private Connection<?> connection;
	
	@InjectMocks
	private SocialUserService dut = new SocialUserService();
	
	@Test
	public void createNewSocialUser_availableUserId() throws Exception {
		String userId = "userId";
		when(usersConnectionRepository.createConnectionRepository(userId)).thenReturn(connectionRepository);
		List<SocialUser> socialUsers = Lists.newArrayList();
		when(socialUserRepository.findsByUserId(userId)).thenReturn(socialUsers);
		
		dut.createNewSocialUser(userId, connection);
		
		verify(connectionRepository).addConnection(connection);
	}
	
	@Test(expected=ExistedUserException.class)
	public void createNewSocialUser_notAvailableUserId() throws Exception {
		String userId = "userId";
		when(usersConnectionRepository.createConnectionRepository(userId)).thenReturn(connectionRepository);
		List<SocialUser> socialUsers = Lists.newArrayList(new SocialUser());
		when(socialUserRepository.findsByUserId(userId)).thenReturn(socialUsers);
		
		dut.createNewSocialUser(userId, connection);
	}
}
