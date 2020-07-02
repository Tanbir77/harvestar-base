package io.naztech.jobharvestar.model;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.URL;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * The persistent class for the urls database table.
 * 
 * @author Tanbirul Hashan
 * @since 2020-06-28
 */
@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="urls")
@ToString(callSuper = true)
public class Url  {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Setter(value = AccessLevel.PRIVATE)
	@Column(updatable = false, nullable = false)
	private Long id;
	
	@Column(nullable = false, unique = true)
	@NotNull @NonNull @URL
	private String url;

	private boolean isScrapped = false;
	
}