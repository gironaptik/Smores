package smartspace.dao.rdb;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
//import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class GenericIdGenerator {
	private Long id;
	
	public GenericIdGenerator() {
	}

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	//@GeneratedValue//(strategy=GenerationType.AUTO)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	
}
