package smartspace.dao.rdb;

import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import smartspace.data.ElementEntity;

public interface ElementCrud extends PagingAndSortingRepository<ElementEntity, String> {

	public List<ElementEntity> findAllByName(
			@Param("pattern") String pattern, Pageable pageable);
	
	public List<ElementEntity> findAllByTypeLike(
			@Param("pattern") String pattern, Pageable pageable);

	public List<ElementEntity> findAllByCreationTimestampBetween(
			@Param("fromDate") Date fromDate,
			@Param("toDate") Date toDate, Pageable pageable);
}

