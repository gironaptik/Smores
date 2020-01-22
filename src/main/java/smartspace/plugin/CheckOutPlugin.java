package smartspace.plugin;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import smartspace.com.AwsRekognition;
import smartspace.dao.EnhancedActionDao;
import smartspace.dao.EnhancedElementDao;
import smartspace.dao.EnhancedUserDao;
import smartspace.data.ActionEntity;
import smartspace.data.ElementEntity;
import smartspace.data.UserEntity;
import smartspace.infra.UserService;

@Component
public class CheckOutPlugin implements Plugin {

	private EnhancedUserDao<String> users;
	private UserService userService;
	private EnhancedActionDao actions;
	private EnhancedElementDao<String> elements;
	private AwsRekognition collection = new AwsRekognition();

	@Autowired
	public CheckOutPlugin(EnhancedUserDao<String> users, UserService userService, EnhancedElementDao<String> elements, EnhancedActionDao actions) {
		this.users = users;
		this.userService = userService;
		this.elements = elements;
		this.actions = actions;
	}


	@Override
	public ActionEntity process(ActionEntity action) {

		try {
			ElementEntity currentElement = this.elements.readById(action.getElementId()+ "#" +action.getElementSmartspace())
					.orElseThrow(() -> new NullPointerException("Element Doesn't exist"));
			UserEntity logedinUser = this.users.readById(action.getPlayerEmail()+"#"+action.getPlayerSmartspace())
					.orElseThrow(() -> new NullPointerException("User Doesn't exist"));
			this.userService.login(logedinUser.getUserEmail(), logedinUser.getUserSmartspace());
			logedinUser.setPoints(0);
			users.update(logedinUser);
			collection.DeleteFacesFromCollection(logedinUser);	//Check!
			this.actions.create(action);
			currentElement.getMoreAttributes().put("Logout"+action.getActionId(), "User "+ logedinUser.getUserEmail() + " Logout in at "+ LocalDateTime.now());
			elements.update(currentElement);
		return action;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
}
}
