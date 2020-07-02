package io.naztech.jobharvestar.model;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.ToString;


/**
 * The persistent class for the urls database table.
 * 
 * @author Tanbirul Hashan
 * @since 2020-06-28
 */
@Data
//@RequiredArgsConstructor
@Entity
@Table(name="property_details")
@ToString(callSuper = true)
public class PropertyDetails  {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Setter(value = AccessLevel.PRIVATE)
	@Column(updatable = false, nullable = false)
	private Long id;
	
	@Column( nullable = false, unique = true)
	private String url;
	
	private String address;
	
	private String city;
	
	private String state;
	
	private String zip;
	
	private String price;
	
	private String bed;
	
	private String bath;

	private String type;
	
	private String builtYear;
	
	private String heating;
	
	private String cooling;
	
	private String parking;
	
	private String lot;
	
}