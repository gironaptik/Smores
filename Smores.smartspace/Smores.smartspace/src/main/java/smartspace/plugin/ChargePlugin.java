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

import smartspace.dao.EnhancedActionDao;
import smartspace.dao.EnhancedElementDao;
import smartspace.dao.EnhancedUserDao;
import smartspace.data.ActionEntity;
import smartspace.data.ElementEntity;
import smartspace.data.UserEntity;
import smartspace.infra.UserService;

@Component
public class ChargePlugin implements Plugin {

	private EnhancedUserDao<String> users;
	private EnhancedElementDao<String> elements;
	private EnhancedActionDao actions;

	
	@Autowired
	public ChargePlugin(EnhancedUserDao<String> users, UserService userService, EnhancedElementDao<String> elements, EnhancedActionDao actions) {
		this.users = users;
		this.elements = elements;
		this.actions = actions;
	}



	@Override
	public ActionEntity process(ActionEntity action) {

		try {
			UserEntity user = users.readById(action.getPlayerEmail() + "#" + action.getPlayerSmartspace())
					.orElseThrow(() -> new NullPointerException("User Doesn't exist"));
			this.actions.create(action);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return action;
	}
}
