package org.retal.dao;


import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
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
	public void update(UserInfo t, String... args) {
		// TODO Auto-generated method stub
		
	}

	private static final Logger log = Logger.getLogger(UserInfoDAO.class);
}
