package smartspace.dao.rdb;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import smartspace.data.ActionEntity;
import smartspace.data.ElementEntity;

public interface ActionCrud extends
	PagingAndSortingRepository<ActionEntity, String>{

	public List<ActionEntity> findAllByElementId(
			@Param("pattern") String pattern, 
			Pageable pageable);

	public List<ActionEntity> findAllByActionTypeLikeAndPlayerEmail(
			@Param("pattern") String pattern, @Param("name") String pattern2, Pageable pageable);

	public List<ActionEntity> findAllByCreationTimestampBetween(
			@Param("fromDate") Date fromDate, 
			@Param("toDate") Date toDate, 
			Pageable pageable);

	public List<ActionEntity> findAllByCreationTimestampBetweenAndPlayerEmailAndActionType(
			@Param("fromDate") Date fromDate,
			@Param("toDate") Date toDate, 
			@Param("type") String type, 
			@Param("email") String email, 
			Pageable pageable);
	
	public List<ActionEntity> findAllByCreationTimestampBetweenAndPlayerSmartspaceAndActionType(
			@Param("fromDate") Date fromDate,
			@Param("toDate") Date toDate, 
			@Param("type") String type, 
			@Param("email") String smartsapce, 
			Pageable pageable);
	
	public List<ActionEntity> findAllByPlayerSmartspaceAndActionType(
			@Param("type") String type, 
			@Param("email") String smartsapce, 
			Pageable pageable);

}
