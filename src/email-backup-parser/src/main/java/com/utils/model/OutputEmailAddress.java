package com.utils.model;

import java.time.LocalDate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
@EqualsAndHashCode
public class OutputEmailAddress {

	@Getter
	private final String domain;
	
	@Getter
	private final String address;
	
	private String name;
	
	@Getter
	private int occurrences;
	
	private LocalDate first;
	
	private LocalDate last;
	
	public OutputEmailAddress(String address, String name, LocalDate firstEmailDate) {
		
		this.occurrences = 1;
		this.first = this.last = firstEmailDate;
		
		this.address = address;
		
		this.name = name;
		
		if(address.contains("@")) {
			
			String[] parts = address.split("@");
			this.domain = parts[parts.length - 1];
		}
		else {
			
			log.error("Address {} does not contain an @!", address);
			this.domain = "???";
		}
	}
	
	public void addOccurrence(String name, LocalDate emailDate) {
		
		this.occurrences += 1;
		
		if(emailDate != null) {
			
			if(emailDate.isBefore(this.first)) {
				
				this.first = emailDate;
			}
			
			if(emailDate.isAfter(this.last)) {
				
				this.last = emailDate;
			}
		}
		
		if(this.name == null) {
			
			this.name = name;
		}
	}

	public String getFirst() {
		
		return first == null ? "" : first.toString();
	}

	public String getLast() {
		
		return last == null ? "" : last.toString();
	}
	
	public String getName() {
		
		return name == null ? "" : name;
	}
}
