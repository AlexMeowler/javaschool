package org.retal.dao;


import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
import org.retal.service.HibernateSessionFactory;
import org.springframework.stereotype.Component;

@Component
public class UserInfoDAO implements DAO<UserInfo>
{

	@Override
	public void add(UserInfo userInfo)
	{
		log.info("Attempt to add info for user " + userInfo.getUser().toString() + ":" + userInfo.toString());
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.save(userInfo);
		session.flush();
		transaction.commit();
		session.close();
	}

	@Override
	public UserInfo read(int primaryKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserInfo find(String... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(UserInfo t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteById(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@Transactional
	public void update(UserInfo newUserInfo) 
	{
		log.info("Editing user info");
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		//Transaction transaction = session.beginTransaction();
		UserInfo oldUserInfo = session.find(UserInfo.class, newUserInfo.getId()); //add exception
		session.evict(oldUserInfo);
		oldUserInfo.setName(newUserInfo.getName());
		oldUserInfo.setSurname(newUserInfo.getName());
		//oldUserInfo.setHoursWorked(newUserInfo.getHoursWorked());
		oldUserInfo.setStatus(newUserInfo.getStatus());
		session.update(oldUserInfo);
		session.flush();
		//transaction.commit();
		session.close();
	}

	private static final Logger log = Logger.getLogger(UserInfoDAO.class);
}
