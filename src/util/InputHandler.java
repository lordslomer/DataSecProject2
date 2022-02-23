package util;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import accounts.*;

public class InputHandler {
	private AccountManager accMan;
	private RecordsManager allRec;
	private Logger logger;

	public InputHandler() {
		accMan = new AccountManager();
		allRec = new RecordsManager(accMan);
		logger = new Logger();
	}

	public void save() {
		allRec.saveRecords();
	}

	public String hanldeInput(String input, Person user) {
		String option = input.split(" ").length > 0 ? input.split(" ")[0] : "";
		String inputPatId =  input.split(" ").length > 1 ? input.split(" ")[1] : "";
		String inputNurId =  input.split(" ").length > 2 ? input.split(" ")[2] : "";
		StringBuilder response = new StringBuilder();
		switch (option) {
		case "1": {
			StringBuilder patientName = new StringBuilder();
			List<Patient> patients = allRec.getPatientsForPerson(user);
			if (user instanceof Doctor || user instanceof Nurse) {
				if (!patients.isEmpty()) {
					response.append("Here are the following patient that have records associated with you:\n");
					for (Patient p : patients) {
							response.append(p + "\n");
							patientName.append(p.getName() + ",");
					}
					logger.log(user.getName(), patientName.deleteCharAt(patientName.length() - 1).toString(),"listed patients that have associated records:");
				} else {
					response.append("There were no patient-records associated with you.\n");
				}
			}
			return response.toString() + "\n" + listAllOptions(user);
		}
		case "2": {
			StringBuilder patientName = new StringBuilder();
			List<Patient> patients = allRec.getPatientsForDivision(user);
			if (user instanceof Doctor || user instanceof Nurse) {
				if(!patients.isEmpty()) {
					response.append("The following patients have records in " + user.getDiv() + ": \n");
					for(Patient p : patients) {
						response.append(p + "\n");
						patientName.append(p.getName() + ",");
					}
					logger.log(user.getName(), patientName.deleteCharAt(patientName.length() - 1).toString(),"listed records of " + user.getDiv());
				}else {
					response.append("Your division has no patient-records.\n");
				}
			}
			return response.toString() + "\n" + listAllOptions(user);
		}
		case "3": {
			List<Records> records;
			if(user instanceof Patient) {
				records = allRec.getAllPatientRecords(user.getId());
				inputPatId = user.getId();
				if(records == null)records = new ArrayList<Records>();
			}else  {
				if(user instanceof Government ) {
					records = allRec.getAllPatientRecords(inputPatId);
					if(records == null)records = new ArrayList<Records>();
				}else {
					records = new ArrayList<Records>();
					Records rec = allRec.getRecord(inputPatId,user.getId());
					if(rec != null)records.add(rec);
				}
			} 
			Person patient = accMan.getPersonFromId(inputPatId);
			if(records.size() > 0 && patient != null) {
				response.append("Here are the records:\n");
				logger.log(user.getName(), patient.getName()+ " records", "viewed");
			}else {
				response.append("No patient records found for " + (patient != null ? (user instanceof Patient ? "you." : ("patient-id="+patient.getId() +".")) : "the given patient-id.") + "\n");
			}
			for(Records r : records)if(r!=null)response.append(r+"\n");
			return response.toString() + "\n" + listAllOptions(user);
		}
		case "4": {
			if((user instanceof Doctor || user instanceof Nurse)) {
				if(allRec.getRecord(inputPatId, user.getId())==null) {
					response.append("No such record associated with you.\n");
				}else {
					return "Write in the record information:";
				}
			}
			return response.toString() + "\n" + listAllOptions(user);
		}
		case "5": {
			if(user instanceof Doctor && inputPatId != null && inputNurId != null) {
				if(allRec.addRecords(inputPatId, (Doctor) user, inputNurId)) {
					response.append("Record was successfully created.\n");
					logger.log(user.getName(), accMan.getPersonFromId(inputPatId).getName() + " " + accMan.getPersonFromId(inputNurId).getName(), "created patient record for");
				}else {
					response.append("Unable to create record with the given patient and nurse.\n");
					logger.log(user.getName(), accMan.getPersonFromId(inputPatId).getName() + " " + accMan.getPersonFromId(inputNurId).getName(), "tried to create patient record for");
				}
			}
			return response.toString() + "\n" + listAllOptions(user);
		}
		case "6": {
			if(user instanceof Government) {
				if(allRec.deleteRecords(inputPatId)) {
					response.append("Patient record was deleted successfully.\n");
					logger.log(user.getName(), accMan.getPersonFromId(inputPatId).getName(), "deleted patient record for");
				}else {
					response.append("No such patient record with the patient-id you have given.\n");
					logger.log(user.getName(), accMan.getPersonFromId(inputPatId).getName(), "tried to delete patient record for");
				}
			}
			return response.toString() + "\n" + listAllOptions(user);
		}
		default: {
			response.append("Choose out of the following options: \n\n");
			return  response.toString()+ listAllOptions(user);
		}
		}
	}
	
	public String writeToRecord(String patientId, String entryInformation, Person user) {
		String time = Logger.getTimeDate();
		allRec.getRecord(patientId, user.getId()).addRecord(entryInformation, time);
		logger.log(user.getName(), accMan.getPersonFromId(patientId).getName(), "wrote in record for");
		return "Record successfully written.\n\n" + listAllOptions(user);
	}

	public String listAllOptions(Person person) {
		StringBuilder options = new StringBuilder();
		if (person instanceof Doctor) {
			options.append("Enter 1 to list all associated patient-records\n");
			options.append("Enter 2 to list all records associated with your division\n");
			options.append("Enter 3 followed by a patient id to read a patient record\n");
			options.append("Enter 4 followed by a patient id to write in patient record\n");
			options.append("Enter 5 followed by a patient id and a nurse id to create a patient receord\n");
		} else if (person instanceof Nurse) {
			options.append("Enter 1 to list all associated patient-records\n");
			options.append("Enter 2 to list all records associated with your division\n");
			options.append("Enter 3 followed by a patient id to read a patient record\n");
			options.append("Enter 4 followed by a patient id to write in patient record\n");
		} else if (person instanceof Government) {
			options.append("Enter 3 followed by a patient id to read a patient record\n");
			options.append("Enter 6 followed by a patient id to delete a patient record\n");
		} else if (person instanceof Patient) {
			options.append("Enter 3 to read your patient record\n");
		}
		options.append("\nEnter \"quit\" or \"exit\" to log off\n");
		return options.toString();
	}

	public Person getPerson(Certificate cert) {
		return accMan.getPersonFromSerialNumber(((X509Certificate) cert).getSerialNumber());
	}

}
