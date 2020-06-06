package smartspace.infra;

import java.io.FileWriter;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.CSVWriter;

import smartspace.AppProperties;
import smartspace.com.AwsAndRecommendation;
import smartspace.dao.EnhancedUserDao;
import smartspace.data.UserEntity;
import smartspace.data.UserRole;

@Service
public class UserServiceImpl implements UserService {
	private EnhancedUserDao<String> userDao;
	private AppProperties appProperties;
	private AwsAndRecommendation collection = new AwsAndRecommendation();
	private final String key_name = "recommend_1.csv";


	@Autowired
	public UserServiceImpl(EnhancedUserDao<String> userDao, AppProperties appPropertie) {
		this.userDao = userDao;
		this.appProperties = appPropertie;
	}

	@Override
	public UserEntity newUser(UserEntity entity) {
		try {
		if (valiadate(entity)) {
			collection.AddFacesToCollection(entity, 1);
		    CSVWriter writer = new CSVWriter(new FileWriter(key_name, true));
			//Updating Recommendation system
		    String[] user = (entity.getUserEmail() + "").split(",");
		     writer.writeNext(user);
		     writer.close();
		     collection.uploadCSV(key_name);
			return this.userDao.create(entity);
		} else {
			throw new RuntimeException("invalid user");
		}
	}	catch (Exception e) {
		throw new RuntimeException(e);
	}
		
	}

	private boolean valiadate(UserEntity entity) {
		return entity.getUserName() != null && !entity.getUserName().trim().isEmpty() && entity.getUserEmail() != null;
	}

	@Override
	public List<UserEntity> getUserUsingPagination(String adminSmartspace, String adminEmail, int size, int page) {
		if (this.userDao.readById(adminEmail +"#"+ adminSmartspace).isPresent()) {
			if (this.userDao.getUserRole(adminSmartspace, adminEmail) != UserRole.ADMIN) {
				throw new RuntimeException("You are not allowed to get users");
			}
		} else
			throw new NoSuchElementException("Your user doesn't exist");
		return this.userDao.readAll("key", size, page);
	}
	
	@Override
	public List<UserEntity> getUsersList(String userSmartspace, String userEmail) {
		if (this.userDao.readById(userEmail +"#"+ userSmartspace).isPresent()) {
			if (this.userDao.getUserRole(userSmartspace, userEmail) != UserRole.MANAGER) {
				throw new RuntimeException("You are not allowed to get users");
			}
		} else
			throw new NoSuchElementException("Your user doesn't exist");
		return this.userDao.readAll();
	}
	
	@Override
	@Transactional
	public Collection<UserEntity> store(String adminSmartspace, String adminEmail,
			Collection<UserEntity> userEntitiesToImport){
		if(this.userDao.getUserRole(adminSmartspace, adminEmail) != UserRole.ADMIN) {
			throw new RuntimeException("You are not allowed to create users");
		} else {
			if(userEntitiesToImport.stream().anyMatch(entity-> entity.getUserSmartspace().equals(appProperties.getName()))){
				throw new RuntimeException("Not allowed to import data from the local smartspace");
			}
			
			return userEntitiesToImport.stream()
					.map(entity-> userDao.insert(entity))
					.collect(Collectors.toList());
		}
	}
	
	@Override
	public void update(String userKey, UserEntity user) {
		if(user.getKey()!=null) {
			user.setKey(userKey);
			this.userDao
				.update(user);	
		}
		else {
			throw new RuntimeException("Can't re-write key value, must be Null");
		}
	}
	
	@Override
	public void updateWithoutPoints(String userKey, UserEntity user) {
		if(user.getKey()!=null) {
			user.setKey(userKey);
			this.userDao
				.updateWithoutPoints(user);
			
		}
		else {
			throw new RuntimeException("Can't re-write key value, must be Null");
		}
	}
	
	@Override
	public UserEntity login(String email, String smartspace) throws NullPointerException {
		UserEntity user = getUserById(email, smartspace);
		if (user.getKey().isEmpty())
			throw new NullPointerException("Can not login to unverfied user account");
		return user;
	}
	
	@Override
	@Transactional
	public UserEntity getUserById(String email, String smartspace) throws NullPointerException {
		return userDao.readById(email +"#" +smartspace).orElseThrow(() -> new NullPointerException(
				"User doesn't exists with email: " + email + ", and smartspace: " + smartspace));
	}

	@Override
	public void deleteByKey(String key) throws NullPointerException{
		this.userDao.deleteById(key);
		
	}
}
