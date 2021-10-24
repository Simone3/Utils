package com.utils.logic;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MappedContact {

	private String name;
	private final Set<String> phoneNumbers = new HashSet<>();
	private final Set<String> emailAddresses = new HashSet<>();
}
