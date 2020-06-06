package smartspace.dao.rdb;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import smartspace.data.UserEntity;
import smartspace.com.*;
import smartspace.data.UserRole;
import smartspace.AppProperties;
import smartspace.dao.EnhancedUserDao;

@Repository
public class RdbUserDao implements EnhancedUserDao<String> {
	private UserCrud userCrud;
	private AppProperties appProperties;
	private @Autowired MongoTemplate mongo;
	private AwsAndRecommendation collection = new AwsAndRecommendation();
	final String data = "trx_data.csv";
	final String users = "recommend_1.csv";



	@Autowired
	public RdbUserDao(UserCrud userCrud, AppProperties appProperties) {
		super();
		this.userCrud = userCrud;
		this.appProperties = appProperties;
	}

	@Override
	@Transactional
	public UserEntity create(UserEntity userEntity) {
		userEntity.setKey(userEntity.getUserEmail() +"#" + appProperties.getName());
		if (!this.userCrud.existsById(userEntity.getKey())) {
			UserEntity rv = this.userCrud.save(userEntity);
			return rv;
		}else {
			throw new RuntimeException("User already exists with key: " + userEntity.getKey());
		}
	}
	
	@PostConstruct
	public void adminCreator() {
		UserEntity admin = new UserEntity();
		admin.setRole(UserRole.ADMIN);
		admin.setKey("admin#Smores");
		admin.setAvatar(":)");
		admin.setPoints(0);
		admin.setUserName("Owner");
		this.userCrud.save(admin);
	}
	
	@PostConstruct
	public void managerCreator() {
		UserEntity manager = new UserEntity();
		collection.downloadCSV(data);
		collection.downloadCSV(users);
		manager.setRole(UserRole.MANAGER);
		manager.setKey("manager#Smores");
		manager.setAvatar(":)");
		manager.setPoints(0);
		manager.setUserName("Roy");		
		this.userCrud.save(manager);
	}
	
	@Override
	@Transactional
	public void deleteAll() {
		// SQL: DELETE
		this.userCrud.deleteAll();
	}
	
	@Override
	@Transactional(readOnly=true)
	public Optional<UserEntity> readById(String userKey) {
		return this.userCrud.findById(userKey);
	}

	@Override
	@Transactional(readOnly=true)
	public List<UserEntity> readAll() {
		List<UserEntity> rv = new ArrayList<>();
		this.userCrud
			.findAll()
			.forEach(user->rv.add(user));
		return rv;
	}

	@Override
	@Transactional
	public void update(UserEntity userToUpdate) {
		UserEntity existing = 
				this.readById(userToUpdate.getKey())
				  .orElseThrow(()-> new RuntimeException("No user with key: " + userToUpdate.getKey()));
		// Patching
		if (userToUpdate.getAvatar() != null) {
			existing.setAvatar(userToUpdate.getAvatar());
		}
		
		if (userToUpdate.getRole() != null) {
			existing.setRole(userToUpdate.getRole());
		}
		
		if (userToUpdate.getUserName() != null) {
			existing.setUserName(userToUpdate.getUserName());
		}	
		if (Long.valueOf(userToUpdate.getPoints()) != null) {
			existing.setPoints(userToUpdate.getPoints());
		}
		this.userCrud.save(existing);
	}
	
	@Override
	@Transactional
	public void updateWithoutPoints(UserEntity userToUpdate) {
		UserEntity existing = 
				this.readById(userToUpdate.getKey())
				  .orElseThrow(()-> new RuntimeException("No user with key: " + userToUpdate.getKey()));
		// Patching
		if (userToUpdate.getAvatar() != null) {
			existing.setAvatar(userToUpdate.getAvatar());
		}
		
		if (userToUpdate.getRole() != null) {
			existing.setRole(userToUpdate.getRole());
		}
		
		if (userToUpdate.getUserName() != null) {
			existing.setUserName(userToUpdate.getUserName());
		}		
		this.userCrud.save(existing);
	}

	@Override
	@Transactional(readOnly=true)
	public List<UserEntity> readAll(int size, int page) {
		return this.userCrud
			.findAll(PageRequest.of(page, size))
			.getContent();
	}

	@Override
	@Transactional(readOnly=true)
	public List<UserEntity> readUserWithSmartspaceContaining(@Param("userSmartspace") String userSmartspace,
			int size, 
			int page) {
		
		return this.userCrud
				.findAllByUserSmartspace(
						userSmartspace,
						PageRequest.of(page, size));
	}


	@Override
	public List<UserEntity> readAll(String sortBy, int size, int page) {
		return this.userCrud
			.findAll(PageRequest.of(
					page, size, 
					Direction.ASC, sortBy))
			.getContent();
	}
	

	@Override
	public UserEntity updateOrInsert(UserEntity user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Transactional
	public UserEntity insert(UserEntity userEntity) {
		if (!this.userCrud.existsById(userEntity.getKey())) {
			UserEntity rv = this.userCrud.save(userEntity);
			return rv;
		}else {
			this.update(userEntity);
			return userEntity;
		}
	}

	@Override
	public UserRole getUserRole(String adminSmartspace, String adminEmail) {
		return this.readById(adminEmail+ "#" + adminSmartspace).get().getRole();
	}

	@Override
	public void deleteById(String key) {
		this.userCrud.deleteById(key);
	}

}
