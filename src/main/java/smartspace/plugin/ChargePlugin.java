package smartspace.plugin;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.sarxos.webcam.Webcam;

import smartspace.dao.EnhancedElementDao;
import smartspace.dao.EnhancedUserDao;
import smartspace.data.ActionEntity;
import smartspace.data.ElementEntity;
import smartspace.data.UserEntity;

@Component
public class ChargePlugin implements Plugin {

	private EnhancedUserDao<String> users;
	private EnhancedElementDao<String> elements;
	
	@Autowired
	public ChargePlugin(EnhancedUserDao<String> users, EnhancedElementDao<String> elements) {
		this.users = users;
		this.elements = elements;
	}


	@Override
	public ActionEntity process(ActionEntity action) {

		try {
			ElementEntity currentElement = this.elements.readById(action.getElementId()+ "#"+ action.getElementSmartspace())
					.orElseThrow(() -> new NullPointerException("Element Doesn't exist"));
			UserEntity user = users.readById(action.getPlayerEmail() + "#" + action.getPlayerSmartspace())
					.orElseThrow(() -> new NullPointerException("User Doesn't exist"));
			if(!currentElement.getType().equals("audio"))
				throw new NullPointerException("This element doesn't have audio option");
			if(currentElement.getMoreAttributes().get("volume").equals(null))
				throw new NullPointerException("This element doesn't have sound attribute");

			currentElement.getMoreAttributes().remove("volume");
			currentElement.getMoreAttributes().put("volume", action.getMoreAttributes().get("volume"));
		elements.update(currentElement);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return action;
	}
}
