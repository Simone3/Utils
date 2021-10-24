package com.utils.logic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.utils.generated.ContactRecords;
import com.utils.generated.ContactRecords.Contact;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UtilLogicRunner implements CommandLineRunner {

	@Override
	public void run(String... args) throws Exception {
		
		log.info("Start contact parser util");
		
		Assert.isTrue(args != null && args.length == 2, "Util requires 2 arguments, source and target files");
		
		var sourceFile = getSourceFile(args);
		var targetFile = getTargetFile(args);

		var contactRecords = parseSourceFile(sourceFile);
		if(contactRecords == null || contactRecords.getContact() == null || contactRecords.getContact().isEmpty()) {
			
			log.warn("No contact found");
			return;
		}
		
		var mappedContacts = mapContacts(contactRecords);
		
		writeContacts(mappedContacts, targetFile);
	}

	private File getSourceFile(String... args) {
		
		var sourceFilePath = args[0];
		var sourceFile = new File(sourceFilePath);
		
		Assert.isTrue(sourceFile.exists(), "Source file does not exist");
		Assert.isTrue(sourceFile.isFile(), "Source file is not a file");
		
		return sourceFile;
	}

	private File getTargetFile(String... args) {
		
		var targetFilePath = args[1];
		var targetFile = new File(targetFilePath);
		
		if(targetFile.exists()) {
			
			Assert.isTrue(targetFile.isFile(), "Target file is not a file");
			
			log.warn("Replacing target file");
		}
		
		return targetFile;
	}

	@SneakyThrows
	private ContactRecords parseSourceFile(File sourceFile) {
		
		log.info("Start parsing {}", sourceFile.getAbsolutePath());

		var jaxbContext = JAXBContext.newInstance(ContactRecords.class);
		var jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		var contactRecords = (ContactRecords) jaxbUnmarshaller.unmarshal(sourceFile);

		log.info("Parsed {}", sourceFile.getAbsolutePath());
		
		return contactRecords;
	}
	
	private List<MappedContact> mapContacts(ContactRecords contactRecords) {
		
		Map<String, MappedContact> mappedContactsMap = new HashMap<>();
		for(Contact contact: contactRecords.getContact()) {
			
			var mappedContact = mapContact(contact);
			if(mappedContact != null) {
				
				var name = mappedContact.getName();
				var existingMappedContact = mappedContactsMap.get(name);
				if(existingMappedContact == null) {
					
					mappedContactsMap.put(name, mappedContact);
				}
				else {
					
					margeContacts(mappedContact, existingMappedContact);
				}
			}
		}
		
		return mappedContactsMap.values()
			.stream()
			.sorted(Comparator.comparing(MappedContact::getName))
			.collect(Collectors.toList());
	}

	private MappedContact mapContact(Contact contact) {
		
		if(contact == null) {
			
			return null;
		}
		
		var mappedContact = new MappedContact();
		
		mappedContact.setName("");
		var structuredName = contact.getStructuredName();
		if(structuredName != null) {
			
			List<String> nameValues = new ArrayList<>();
			addIfNotBlank(nameValues, structuredName.getPrefixName());
			addIfNotBlank(nameValues, structuredName.getMiddleName());
			addIfNotBlank(nameValues, structuredName.getPhoneticMiddleName());
			addIfNotBlank(nameValues, structuredName.getFamilyName());
			addIfNotBlank(nameValues, structuredName.getPhoneticFamily());
			addIfNotBlank(nameValues, structuredName.getGivenName());
			addIfNotBlank(nameValues, structuredName.getPhoneticGivenName());
			addIfNotBlank(nameValues, structuredName.getSuffixName());
			
			if(nameValues.isEmpty()) {
				
				if(!StringUtils.isBlank(structuredName.getDisplayName())) {
					
					mappedContact.setName(structuredName.getDisplayName().trim());
				}
			}
			else {
				
				mappedContact.setName(String.join(" ", nameValues));
			}
		}
		
		if(contact.getPhones() != null && contact.getPhones().getPhone() != null) {
			
			for(var phone: contact.getPhones().getPhone()) {
				
				if(phone != null && !StringUtils.isBlank(phone.getNumber())) {
					
					var number = phone.getNumber().replace(" ", "").replace("-", "").replace("+39", "");
					
					if(!Pattern.compile("\\d+").matcher(number).matches()) {
						
						log.warn("Phone number for {} is non-numeric: {}", mappedContact.getName(), number);
					}
					
					mappedContact.getPhoneNumbers().add(number);
				}
			}
		}
		
		if(contact.getEmails() != null && contact.getEmails().getEmail() != null) {
			
			for(var email: contact.getEmails().getEmail()) {
				
				if(email != null && !StringUtils.isBlank(email.getAddress())) {
					
					mappedContact.getEmailAddresses().add(email.getAddress().trim());
				}
			}
		}
		
		return mappedContact;
	}
	
	private void addIfNotBlank(List<String> list, String value) {
		
		if(!StringUtils.isBlank(value)) {
			
			list.add(value.trim());
		}
	}

	private void margeContacts(MappedContact source, MappedContact target) {
		
		target.getPhoneNumbers().addAll(source.getPhoneNumbers());
		target.getEmailAddresses().addAll(source.getEmailAddresses());
	}
	
	@SneakyThrows
	private void writeContacts(List<MappedContact> mappedContacts, File targetFile) {
		
		log.info("Writing {} unique contacts to {}", mappedContacts.size(), targetFile.getAbsolutePath());
		
		var fos = new FileOutputStream(targetFile);
		var osw = new OutputStreamWriter(fos);
		try(var bw = new BufferedWriter(osw)) {
	 
			for(var mappedContact: mappedContacts) {

				var row = mappedContactToTargetFileRow(mappedContact);
				bw.write(row);
				bw.newLine();
			}
		}

		log.info("Written {} unique contacts to {}", mappedContacts.size(), targetFile.getAbsolutePath());
	}
	
	private String mappedContactToTargetFileRow(MappedContact mappedContact) {
		
		List<String> values = new ArrayList<>();
		values.add(mappedContact.getName());
		values.add(String.join("; ", mappedContact.getPhoneNumbers()));
		values.add(String.join("; ", mappedContact.getEmailAddresses()));
		return String.join("\t", values);
	}
}
