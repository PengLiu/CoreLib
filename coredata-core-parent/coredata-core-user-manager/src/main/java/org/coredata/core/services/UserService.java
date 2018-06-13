package org.coredata.core.services;

import org.coredata.core.entities.User;
import org.coredata.core.repositories.UserResp;
import org.coredata.core.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

	@Autowired
	private UserResp userResp;

	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException{
		User user = userResp.findByUserName(userName);
		if (user == null) {
			throw new UsernameNotFoundException(userName);
		}
		return new UserPrincipal(user);
	}

}