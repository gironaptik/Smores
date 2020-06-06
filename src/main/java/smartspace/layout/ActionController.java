package smartspace.layout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import smartspace.data.ActionEntity;
import smartspace.infra.ActionService;
import smartspace.infra.UserService;

@RestController
public class ActionController {
	private ActionService actionService;
	private UserService userService;

	@Autowired
	public ActionController(ActionService actionService) {
		this.actionService = actionService;
	}

	@Autowired
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@RequestMapping(path = "/smartspace/admin/actions/{adminSmartspace}/{adminEmail}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ActionBoundary[] importActions(@RequestBody ActionBoundary[] actionsToImport,
			@PathVariable("adminSmartspace") String adminSmartspace, @PathVariable("adminEmail") String adminEmail) {
		Collection<ActionEntity> actionEntitiesToImport = ((Arrays.asList(actionsToImport))).stream()
				.map(action -> new ActionBoundary().convertToEntity(action)).collect(Collectors.toList());

		return this.actionService.store(adminSmartspace, adminEmail, actionEntitiesToImport).stream()
				.map(ActionBoundary::new).collect(Collectors.toList()).toArray(new ActionBoundary[0]);
	}

	@RequestMapping(path = "/smartspace/admin/actions/{adminSmartspace}/{adminEmail}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ActionBoundary[] getUsingPagination(@PathVariable(name = "adminSmartspace") String adminSmartspace,
			@PathVariable(name = "adminEmail") String adminEmail,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) throws IOException {
		return this.actionService.getActionUsingPagination(adminEmail + "#" + adminSmartspace, size, page).stream()
				.map(ActionBoundary::new).collect(Collectors.toList()).toArray(new ActionBoundary[0]);
	}

	@RequestMapping(path = "/smartspace/actions", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ActionBoundary newAction(@RequestBody ActionBoundary action) {
		if (action.getActionKey().getId() == null && action.getActionKey().getSmartspace() == null)
			return new ActionBoundary(this.actionService.invoke(action.convertToEntity(action)));
		else {
			throw new NullPointerException("action key must be Null");
		}
	}

	@RequestMapping(path = "/smartspace/actions/delete/{key}", method = RequestMethod.DELETE)
	public void deleteByKey(@PathVariable("key") String key) {
		this.actionService.deleteByKey(key);
	}

	// Get Action by Type and Mail
	@RequestMapping(path = "/smartspace/actions/{userSmartspace}/{userEmail}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {
			"search=type" })
	public ActionBoundary getAllActionsWithSpecifiedType(@PathVariable("userSmartspace") String userSmartspace,
			@PathVariable("userEmail") String userEmail, @RequestParam(name = "search", required = false) String name,
			@RequestParam(name = "value", required = true) String type,
			@RequestParam(name = "size", required = false, defaultValue = "10000") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		userService.login(userEmail, userSmartspace);
		return new ActionBoundary(this.actionService.getActionByTypeAndEmail(size, page, userEmail, type));
//			return this.actionService.getAllByType(size, page, type).stream().map(ActionBoundary::new)
//					.collect(Collectors.toList()).toArray(new ActionBoundary[0]);
	}

	// Live ShoppingList
	@RequestMapping(path = "/smartspace/shoppinglist/{userSmartspace}/{userEmail}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ActionBoundary[] getAllActionsWithSpecifiedTimeAndEmailAndType(
			@PathVariable("userSmartspace") String userSmartspace, @PathVariable("userEmail") String userEmail) {
		userService.login(userEmail, userSmartspace);
		return this.actionService.getAllActionsBetweenCheckInAndCheckOutByTime(Integer.MAX_VALUE, 0, userEmail).stream()
				.map(ActionBoundary::new).collect(Collectors.toList()).toArray(new ActionBoundary[0]);
	}

	// See list of actions from same type
	@RequestMapping(path = "/smartspace/actions/typeList/{userSmartspace}/{userEmail}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {
			"search=type" })
	public ActionBoundary[] getAllActionListByType(@PathVariable("userSmartspace") String userSmartspace,
			@PathVariable("userEmail") String userEmail, @RequestParam(name = "type", required = true) String type) {
		userService.login(userEmail, userSmartspace);
		return this.actionService.getActionsListByType(Integer.MAX_VALUE, 0, userEmail, type).stream()
				.map(ActionBoundary::new).collect(Collectors.toList()).toArray(new ActionBoundary[0]);
	}

	// Creating list of types between timestamps
	@RequestMapping(path = "/smartspace/actionsbetween/{userSmartspace}/{userEmail}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ActionBoundary[] getAllActionsBetweenTimeAndEmailAndType(
			@PathVariable("userSmartspace") String userSmartspace, @PathVariable("userEmail") String userEmail,
			@RequestParam(name = "type", required = true) String type,
			@RequestParam(name = "from", required = true) String action1,
			@RequestParam(name = "to", required = false, defaultValue = "current") String action2) {
		userService.login(userEmail, userSmartspace);
		return this.actionService
				.getActionsListByTimeStampAndType(Integer.MAX_VALUE, 0, userEmail, type, action1, action2,
						userSmartspace)
				.stream().map(ActionBoundary::new).collect(Collectors.toList()).toArray(new ActionBoundary[0]);
	}

	@RequestMapping(method = RequestMethod.GET, path = "/smartspace/actions/amount/{userSmartspace}/{userEmail}", produces = MediaType.APPLICATION_JSON_VALUE)
	public int getActionsAmountByUserAndType(@PathVariable("userSmartspace") String userSmartspace,
			@PathVariable("userEmail") String userEmail, @RequestParam(name = "type", required = true) String type) {
		return new ArrayList<ActionEntity>(
				this.actionService.getActionsListByType(Integer.MAX_VALUE, 0, userEmail, type)).size();
	}

	// Get actions by type and time of All users
	@RequestMapping(path = "/smartspace/manager/{managerSmartspace}/{managerEmail}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {
			"search=all" })
	public ActionBoundary[] getAllActionsBetweenTimeAndType(@PathVariable("managerSmartspace") String managerSmartspace,
			@PathVariable("managerEmail") String managerEmail,
			@RequestParam(name = "type", required = true) String type,
			@RequestParam(value = "fromDate", required = true) @DateTimeFormat(pattern = "MMyyyy") Date dateFrom,
			@RequestParam(value = "toDate", required = true) @DateTimeFormat(pattern = "MMyyyy") Date dateTo) {
		userService.login(managerEmail, managerSmartspace);
		return this.actionService
				.getAllActionsListByTimeStampAndType(Integer.MAX_VALUE, 0, managerEmail, type, dateFrom, dateTo,
						managerSmartspace)
				.stream().map(ActionBoundary::new).collect(Collectors.toList()).toArray(new ActionBoundary[0]);
	}

	// Get actions by type of All users
	@RequestMapping(path = "/smartspace/manager/{managerSmartspace}/{managerEmail}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {
			"search=allbytype" })
	public ActionBoundary[] getAllActionsByType(@PathVariable("managerSmartspace") String managerSmartspace,
			@PathVariable("managerEmail") String managerEmail,
			@RequestParam(name = "type", required = true) String type) {
		userService.login(managerEmail, managerSmartspace);
		return this.actionService
				.getAllActionsListByType(Integer.MAX_VALUE, 0, managerEmail, type,
						managerSmartspace)
				.stream().map(ActionBoundary::new).collect(Collectors.toList()).toArray(new ActionBoundary[0]);
	}

}
