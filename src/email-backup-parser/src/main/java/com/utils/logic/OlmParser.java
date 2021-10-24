package com.utils.logic;

import java.io.File;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.khubla.olmreader.olm.OLMFile;
import com.khubla.olmreader.olm.OLMMessageCallback;
import com.khubla.olmreader.olm.OLMRawMessageCallback;
import com.khubla.olmreader.olm.generated.Appointments.Appointment;
import com.khubla.olmreader.olm.generated.Categories;
import com.khubla.olmreader.olm.generated.Contacts.Contact;
import com.khubla.olmreader.olm.generated.EmailAddress;
import com.khubla.olmreader.olm.generated.Emails.Email;
import com.khubla.olmreader.olm.generated.Groups.Group;
import com.khubla.olmreader.olm.generated.Notes.Note;
import com.khubla.olmreader.olm.generated.Tasks.Task;
import com.utils.model.OutputEmailAddress;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OlmParser implements OLMMessageCallback, OLMRawMessageCallback {

	private final OLMFile olmFile;
	private final int logFrequency;
	private int emailsProcessed;
	private final Map<String, OutputEmailAddress> allEmailAddressesMap = new HashMap<>();
	
	@SneakyThrows
	public OlmParser(File file, int logFrequency) {
		
		this.olmFile = new OLMFile(file.getAbsolutePath());
		this.logFrequency = logFrequency;
		olmFile.readOLMFile(this, this);
	}
	
	public Collection<OutputEmailAddress> getAllEmailAddresses() {
		
		return allEmailAddressesMap.values();
	}

	@Override
	public void email(Email email, HashMap<String, byte[]> attachments) {

		LocalDate emailDate = null;
		if(email.getOPFMessageCopySentTime() != null && email.getOPFMessageCopySentTime().getValue() != null) {
			
			var xmlDate = email.getOPFMessageCopySentTime().getValue();
			
			emailDate = LocalDate.of(xmlDate.getYear(), xmlDate.getMonth(), xmlDate.getDay());
		}
		else {
			
			log.error("Email has no date! {}", email);
		}
		
		if(email.getOPFMessageCopyBCCAddresses() != null) {
			
			addAddresses(emailDate, email.getOPFMessageCopyBCCAddresses().getEmailAddress());
		}
		
		if(email.getOPFMessageCopyCCAddresses() != null) {
			
			addAddresses(emailDate, email.getOPFMessageCopyCCAddresses().getEmailAddress());
		}
		
		if(email.getOPFMessageCopyFromAddresses() != null) {
			
			addAddresses(emailDate, email.getOPFMessageCopyFromAddresses().getEmailAddress());
		}
		
		if(email.getOPFMessageCopyReplyToAddresses() != null) {
			
			addAddresses(emailDate, email.getOPFMessageCopyReplyToAddresses().getEmailAddress());
		}
		
		if(email.getOPFMessageCopySenderAddress() != null) {
			
			addAddresses(emailDate, email.getOPFMessageCopySenderAddress().getEmailAddress());
		}
		
		if(email.getOPFMessageCopyToAddresses() != null) {
			
			addAddresses(emailDate, email.getOPFMessageCopyToAddresses().getEmailAddress());
		}
		
		emailsProcessed++;
		
		if(emailsProcessed % logFrequency == 0) {
			
			log.info("Processed {} emails", emailsProcessed);
		}
	}
	
	private void addAddresses(LocalDate emailDate, List<EmailAddress> emailAddresses) {
		
		if(emailAddresses != null) {
			
			for(var emailAddress: emailAddresses) {

				if(emailAddress != null) {
					
					var address = emailAddress.getOPFContactEmailAddressAddress();
					var name = emailAddress.getOPFContactEmailAddressName();
					
					if(!StringUtils.isBlank(address)) {
						
						address = address.trim().toLowerCase();
						name = StringUtils.isBlank(name) ? null : name.trim();
						
						if(allEmailAddressesMap.containsKey(address)) {
							
							allEmailAddressesMap.get(address).addOccurrence(name, emailDate);
						}
						else {
							
							allEmailAddressesMap.put(address, new OutputEmailAddress(address, name, emailDate));
						}						
					}
				}
			}
		}
	}
	
	@Override
	public void rawMessage(String olmMessage) {
		// Do nothing for now
	}

	@Override
	public void appointment(Appointment appointment) {
		// Do nothing for now
	}

	@Override
	public void categories(Categories categories) {
		// Do nothing for now
	}

	@Override
	public void contact(Contact contact) {
		// Do nothing for now
	}

	@Override
	public void group(Group group) {
		// Do nothing for now
	}

	@Override
	public void note(Note note) {
		// Do nothing for now
	}

	@Override
	public void task(Task task) {
		// Do nothing for now
	}
}
