package accounts;
import util.Division;

public class Patient extends Person {
	private String docId;
	public Patient(String name, String id, Division div, String docId) {
		super(name,id,div);
		this.docId = docId;
	}
	public String getDocId() {
		return docId;
	}
}
