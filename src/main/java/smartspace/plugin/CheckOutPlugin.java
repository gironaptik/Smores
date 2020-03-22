package smartspace.plugin;

import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.List;

import org.aspectj.weaver.patterns.IVerificationRequired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.opencsv.CSVWriter;

import smartspace.com.AwsRekognition;
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
	private AwsRekognition collection = new AwsRekognition();
	private final String key_name = "trx_data.csv";


	@Autowired
	public CheckOutPlugin(EnhancedUserDao<String> users, UserService userService, EnhancedElementDao<String> elements, EnhancedActionDao actions,
			ActionService actionService) {
		
		this.users = users;
		this.userService = userService;
		this.elements = elements;
		this.actions = actions;
		this.actionService = actionService;
	}


	@Override
	public ActionEntity process(ActionEntity action) {

		try {
		    CSVWriter writer = new CSVWriter(new FileWriter(key_name, true));
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
			List<ActionEntity> list = actionService.getActionsList(300, 0, logedinUser.getUserEmail(), "Charge");
			String products = "";
			for(ActionEntity productAction : list) {
				products = products.equals("") ? products = productAction.getElementId().toString() : products + "|" + productAction.getElementId().toString();
			}
			
			//Updating Recommendation system
			String [] recordS = (logedinUser.getUserEmail() + "," + products).split(",");
		     writer.writeNext(recordS);
		     writer.close();
		     collection.uploadCSV(key_name);
		return action;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
}
}
