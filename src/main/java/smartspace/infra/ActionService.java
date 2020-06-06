package smartspace.infra;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import smartspace.data.ActionEntity;

public interface ActionService {

	public ActionEntity newAction(ActionEntity entity);
	public Collection<ActionEntity> store(String adminSmartspace, String adminEmail, Collection<ActionEntity> actionEntitiesToImport);
	public List<ActionEntity> getActionUsingPagination(String key, int size, int page);
	public ActionEntity invoke(ActionEntity newAction);
	public void deleteByKey(String key);
	public ActionEntity getActionByTypeAndEmail(int size,int page, String email, String type);
	public List<ActionEntity> getAllActionsBetweenCheckInAndCheckOutByTime(int size,int page, String email);
	public List<ActionEntity> getActionsListByType(int size,int page, String email, String type);
	public List<ActionEntity> getActionsListByTimeStampAndType(int size, int page, String userEmail, String type,
			String action1, String action2, String smartspace);
	public List<ActionEntity> getAllActionsListByTimeStampAndType(int size, int page, String userEmail, String type,
			Date fromDate, Date toDate, String smartspace);
	public List<ActionEntity> getAllActionsListByType(int size, int page, String managerEmail, String type,
			String managerSmartspace);
	}
