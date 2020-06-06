package smartspace.plugin;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smartspace.com.AwsAndRecommendation;
import smartspace.dao.EnhancedActionDao;
import smartspace.dao.EnhancedElementDao;
import smartspace.dao.EnhancedUserDao;
import smartspace.dao.rdb.RdbActionDao;
import smartspace.data.ActionEntity;
import smartspace.data.ElementEntity;
import smartspace.data.UserEntity;
import smartspace.infra.ActionService;
import smartspace.infra.UserService;

@Component
public class CheckInPlugin implements Plugin {

	private EnhancedUserDao<String> users;
	private ActionService actionService;
	private EnhancedActionDao actions;
	private EnhancedElementDao<String> elements;
	private AwsAndRecommendation collection = new AwsAndRecommendation();

	@Autowired
	public CheckInPlugin(EnhancedUserDao<String> users, UserService userService, EnhancedElementDao<String> elements, RdbActionDao actions, ActionService actionService) {
		this.users = users;
		this.elements = elements;
		this.actions = actions;
		this.actionService = actionService;
	}

	@Override
	public ActionEntity process(ActionEntity action) {

		try {
			ElementEntity currentElement = this.elements.readById(action.getElementId()+ "#" +action.getElementSmartspace())
					.orElseThrow(() -> new NullPointerException("Element Doesn't exist"));
			UserEntity logedinUser = this.users.readById(action.getPlayerEmail()+"#"+action.getPlayerSmartspace())
					.orElseThrow(() -> new NullPointerException("User Doesn't exist"));
			ActionEntity relatedCheckInActionEntity = this.actionService.getActionByTypeAndEmail(Integer.MAX_VALUE, 0, logedinUser.getUserEmail(), "CheckIn");
			if(!relatedCheckInActionEntity.getMoreAttributes().containsKey("CheckOut"))
				throw new RuntimeException("Can't perform CheckIn more than one time without CheckOut!");
			collection.AddFacesToCollection(logedinUser, 2);
			this.actions.create(action);
			currentElement.getMoreAttributes().put("Login"+action.getActionId(), "User "+ logedinUser.getUserEmail() + " Logged in at "+ LocalDateTime.now());
			elements.update(currentElement);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return action;
}
	
}
