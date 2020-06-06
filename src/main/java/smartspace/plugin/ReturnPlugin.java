package smartspace.plugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smartspace.dao.EnhancedActionDao;
import smartspace.dao.EnhancedElementDao;
import smartspace.dao.EnhancedUserDao;
import smartspace.data.ActionEntity;
import smartspace.data.ElementEntity;
import smartspace.data.UserEntity;
import smartspace.infra.UserService;

@Component
public class ReturnPlugin implements Plugin {

	private EnhancedUserDao<String> users;
	private EnhancedElementDao<String> elements;
	private EnhancedActionDao actions;

	@Autowired
	public ReturnPlugin(EnhancedUserDao<String> users, UserService userService, EnhancedElementDao<String> elements, EnhancedActionDao actions) {
		this.users = users;
		this.elements = elements;
		this.actions = actions;
	}

	@Override
	public ActionEntity process(ActionEntity action) {

		try {
			UserEntity user = users.readById(action.getPlayerEmail() + "#" + action.getPlayerSmartspace())
					.orElseThrow(() -> new NullPointerException("User Doesn't exist"));
			ElementEntity productElementEntity = this.elements.readElementWithNameContaining(action.getMoreAttributes().get("Product").toString(), 1, 0).get(0);
			int amount = Integer.parseInt(productElementEntity.getMoreAttributes().get("Amount").toString());
			if(amount < 0) 
				throw new Exception("Element not available");
			productElementEntity.getMoreAttributes().remove("Amount");
			productElementEntity.getMoreAttributes().put("Amount", ++amount);
			this.elements.update(productElementEntity);
			ElementEntity elementEntity = this.elements.readById(action.getElementId()+"#"+ action.getElementSmartspace()).orElseThrow(() -> new NullPointerException("Element Doesn't exist"));
			if(!elementEntity.getType().equals("Shelf"))
				throw new Exception("Element type has to be Shelf!");
			action.getMoreAttributes().put("Shelf", elementEntity.getName());
			action.getMoreAttributes().put("Price", productElementEntity.getMoreAttributes().get("Price"));
			this.actions.create(action);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return action;
	}
}
