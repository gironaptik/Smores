package smartspace.plugin;

import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.opencsv.CSVWriter;

import smartspace.com.AwsAndRecommendation;
import smartspace.dao.EnhancedActionDao;
import smartspace.dao.EnhancedElementDao;
import smartspace.dao.EnhancedUserDao;
import smartspace.data.ActionEntity;
import smartspace.data.ElementEntity;
import smartspace.data.UserEntity;
import smartspace.infra.ActionService;
import smartspace.infra.UserService;

@Component
public class CheckOutPlugin implements Plugin {

	private EnhancedUserDao<String> users;
	private UserService userService;
	private ActionService actionService;
	private EnhancedActionDao actions;
	private EnhancedElementDao<String> elements;
	private AwsAndRecommendation collection = new AwsAndRecommendation();
	private final String key_name = "trx_data.csv";

	@Autowired
	public CheckOutPlugin(EnhancedUserDao<String> users, UserService userService, EnhancedElementDao<String> elements,
			EnhancedActionDao actions, ActionService actionService) {

		this.users = users;
		this.userService = userService;
		this.elements = elements;
		this.actions = actions;
		this.actionService = actionService;
	}

	@Override
	public ActionEntity process(ActionEntity action) {
		try {
			UserEntity logedinUser = this.users.readById(action.getPlayerEmail() + "#" + action.getPlayerSmartspace())
					.orElseThrow(() -> new NullPointerException("User Doesn't exist"));
			List<ActionEntity> shoppingList = actionService.getAllActionsBetweenCheckInAndCheckOutByTime(Integer.MAX_VALUE, 0,
					logedinUser.getUserEmail());
			ActionEntity relatedCheckInActionEntity = this.actionService.getActionByTypeAndEmail(Integer.MAX_VALUE, 0, logedinUser.getUserEmail(), "CheckIn");
			if(relatedCheckInActionEntity.getMoreAttributes().containsKey("CheckOut"))
				throw new RuntimeException("Illegal CheckOut, Client already checked out!");
			action.getMoreAttributes().put("CheckIn", relatedCheckInActionEntity.getActionId());
			CSVWriter writer = new CSVWriter(new FileWriter(key_name, true));
			ElementEntity currentElement = this.elements
					.readById(action.getElementId() + "#" + action.getElementSmartspace())
					.orElseThrow(() -> new NullPointerException("Element Doesn't exist"));

			this.userService.login(logedinUser.getUserEmail(), logedinUser.getUserSmartspace());
			collection.DeleteFacesFromCollection(logedinUser); // Check!
			action = this.actions.create(action);
			relatedCheckInActionEntity.getMoreAttributes().put("CheckOut", action.getActionId()); 	////

			currentElement.getMoreAttributes().put("Logout" + action.getActionId(),
					"User " + logedinUser.getUserEmail() + " Logout in at " + LocalDateTime.now());
			elements.update(currentElement);
			this.actions.update(relatedCheckInActionEntity);
			String products = "";
			for (ActionEntity productAction : shoppingList) {
				products = products.equals("") ? products = productAction.getMoreAttributes().get("Product").toString()
						: products + "|" + productAction.getMoreAttributes().get("Product").toString();
			}
			// Updating Recommendation system
			if (!products.equals("")) {
				String[] recordS = (logedinUser.getUserEmail() + "," + products).split(",");
				writer.writeNext(recordS);
				writer.close();
				collection.uploadCSV(key_name);
			}
			return action;
		} catch (Exception e) {
			throw new RuntimeException("Illegal CheckOut!" + e);
		}
	}
}
