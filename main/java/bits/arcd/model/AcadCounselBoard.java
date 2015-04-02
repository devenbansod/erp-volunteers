package bits.arcd.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import bits.arcd.main.WindowLoader;

public class AcadCounselBoard {
	private String studentId;
	private EligibilitySheetQueries elSheet;
	private DBConnector dbConnector = DBConnector.getInstance();
	private boolean cgpaCondition;
	private boolean eGradeCondition;
	private boolean isACB;
	private boolean courseNumCondition;
	private boolean isBackLog;
	private ArrayList<Course> backLogCourses;
	private int noOfTotalCourses;
	private int noOfCompletedCourses;


	public boolean isBackLog() {
		return isBackLog;
	}

	private void setBackLog() {
		if(this.getNoOfCompletedCourses() < this.getNoOfTotalCourses())
			this.isBackLog = true;
		else
			this.isBackLog = false;
	}

	public boolean isCourseNumCondition() {
		return courseNumCondition;
	}

	private void setCourseNumCondition() {
		if(3*this.getNoOfCompletedCourses() < 2*this.getNoOfTotalCourses()){
			this.courseNumCondition = true;
		} else {
			this.courseNumCondition = false;
		}

	}

	//	private ArrayList<Course> eGradeList;
	private ArrayList<String> reasonList = new ArrayList<String>();

	public boolean iseGradeCondition() {
		return eGradeCondition;
	}

	private void seteGradeCondition() {
		if(eGrades().size() >=2){
			this.eGradeCondition = true;
		} else {
			this.eGradeCondition = false;
		}
	}

	public boolean getIsAcb(){
		return this.isACB;
	}


	public AcadCounselBoard(String studentId, int term){
		backLogCourses = new ArrayList<Course>();
		setStudentId(studentId);
		elSheet = new EligibilitySheetQueries(studentId,term);
		setNoOfTotalCourses();
		setNoOfCompletedCourses();
		setCgpaConstraint();
		seteGradeCondition();
		setCourseNumCondition();
		setACB();
		setBackLog();

	}

	public AcadCounselBoard(EligibilitySheetQueries e) {
		elSheet = e;
		backLogCourses = new ArrayList<Course>();
		setStudentId(e.getStudentId());
		setNoOfTotalCourses();
		setNoOfCompletedCourses();
		setCgpaConstraint();
		seteGradeCondition();
		setCourseNumCondition();
		setACB();
		setBackLog();
	}

	public String getStudentId() {
		return studentId;
	}

	private void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public boolean getCgpaConstraint() {
		return cgpaCondition;
	}

	public void setCgpaConstraint() {
		Double cgpa = Double.parseDouble(elSheet.getCgpa());
		if(cgpa <= 4.5){
			this.cgpaCondition = true;
		} else {
			this.cgpaCondition = false;
		}

	}

	private ArrayList<Course> eGrades(){
		//eGrades
		ArrayList<Course> eGradesList = new ArrayList<Course>();
		for(Semester sem: elSheet.getChart().getSemsInChart()){
			for(Course c : sem.getAllCourses()){
				if(c.getIsDoneInPrevSem() && c.getGrade().equalsIgnoreCase("E")){
					eGradesList.add(c);
				}
			}
		}
		return eGradesList;
	}


	private void setACB(){

		int reasons = 0;
		//CGPA Condition
		if(cgpaCondition){
			this.isACB = true;
			reasons++;
			String sr = reasons + ". " + "CGPA is less than 4.5";
			reasonList.add(sr);
		}

		//Number of E Grades Condition
		if(eGradeCondition){
			if(eGradeCondition){
				this.isACB = true;
				reasons++;
				String sr1 = reasons + ". " + "E grade in " +eGrades().size()+ " courses:";
				int i = 0;
				for(Course c: eGrades()){
					i++;
					sr1 = sr1 + "\n\t[" +i+ "] " + c.getSubject()+ " " +c.getCatalog()+ "\t" +c.getDescription();
				}
				reasonList.add(sr1);
			}

		}
		if(courseNumCondition){
			this.isACB = true;
			reasons++;
			String sr2 = reasons + ". " + "Less than 2/3rd structured courses have been completed";

			reasonList.add(sr2);
		}
	}


	public int[] getSemTerm(int term){
		int[] ys = {-1,-1};
		String query = "SELECT sem_description FROM terms WHERE semester = '" + term + "'";
		ResultSet rs = null;

		String x = new String();
		try {
			rs = dbConnector.queryExecutor(query, false);
		}
		catch (Exception e){
			WindowLoader.showExceptionDialog("Semester Description could not be fetched", e);
			e.printStackTrace();
		}

		try {
			while (rs.next()){
				x = rs.getString(1);
			}
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("ResultSet could not be used", e);
			e.printStackTrace();
		}
		
//		x = x.trim();

		System.out.println(x.length());
		System.out.println(studentId);
		if(x.contains("Summer")){
			ys[0] = 0;
			ys[1] = 0;
		} else {
			if(x.contains("First")){
				ys[0] = Integer.parseInt(x.substring(x.length() - 2)) + 2000 - Integer.parseInt(studentId.substring(0, 4));
				ys[1] = 1;
			}
			if(x.contains("Second")){
				ys[0] = Integer.parseInt(x.substring(x.length() - 2)) + 2000 - Integer.parseInt(studentId.substring(0, 4));
				ys[1] = 2;
			}
		}
		return ys;
	}


	public String printACB() {
		String s = "";
		s = "ID: " +studentId + "\tNAME: " +elSheet.getStudentName()+ "\n" + "REQ. GROUP: " +elSheet.getChart().getRequirementGroup()
				+ "\tREQ. NO: "+ elSheet.getRequirementNo() + "\tREQ. DESCRIPTION: " +elSheet.getChart().getRequirementDescription()
				+ "\nCGPA: " +elSheet.getCgpa()
				+"\n\n";

		for(String sr : reasonList){
			s = s + "\n";
			s = s + sr;
		}

		s = s + "\n-------------------------------------------------------------------------\n";
		return s;
	}

	public String printBackLog(){
		int i = 0;
		int rem = this.getNoOfTotalCourses() - this.getNoOfCompletedCourses();
		String s = "";
		s = "ID: " +studentId + "\tNAME: " +elSheet.getStudentName()+ "\n" + "REQ. GROUP: " +elSheet.getChart().getRequirementGroup() 
				+ "\tREQ. NO: "+ elSheet.getRequirementNo() + "\tREQ. DESCRIPTION: " +elSheet.getChart().getRequirementDescription()
				+"\n";
		s = s + "No. of Courses Completed: " + this.getNoOfCompletedCourses() +"\n";
		s = s + "No. of Courses to be done: " + this.getNoOfTotalCourses() +"\n";
		s = s + "Backlog Courses: " + backLogCourses.size();
		for(Course c : this.backLogCourses){
			i++;
			if(c.getCourseCode()==0){
				String desc = c.getElDescr();

				while(c.getElDescr().length() < 10 )
					desc = desc + " ";


				s = s + "\n\t[" + i + "] " + desc;
			}else{
				String printedString = c.getSubject();

				while(printedString.length() < 6)
					printedString = printedString + " ";

				printedString = printedString + c.getCatalog();

				while(printedString.length() < 11)
					printedString = printedString + " ";

				printedString = printedString + c.getDescription();

				while(printedString.length() < 38)
					printedString = printedString + " ";

				s = s + "\n\t[" + i + "] " + printedString;
			}
			rem--;

		}
		if(rem > 0){
			for(Semester sem2  : elSheet.getChart().getSemsInChart()){
				for(int j=0; j< sem2.getNoOfHUEL(); j++){
					s = s + "\n\t[" + ++i+ "] HUEL";
					rem--;
				}
				for(int j=0; j< sem2.getNoOfOEL(); j++){
					s = s + "\n\t[" + ++i+ "] OEL";
					rem--;
				}	
			}

		}
		s = s + "\n---------------------------------------------------------\n";		
		return s;
	}

	public int getNoOfTotalCourses() {
		return noOfTotalCourses;
	}

	public int getNoOfCompletedCourses() {
		return noOfCompletedCourses;
	}

	public void setNoOfTotalCourses (){
		int prevTerm = elSheet.getPrevTerm();
		System.out.println("prev term : "+ prevTerm);
		int [] ys = getSemTerm(prevTerm);
		int count = 0;
		int countOfsems = this.getNoofSems(ys);
		for(int i=0;i<countOfsems;i++){
			Semester s= elSheet.getChart().getSemsInChart().get(i);
			count += s.getNumOfDelCompleted()+s.getNoOfDEL()
					+s.getNoOfHUEL()+s.getNumOfHuelCompleted()+s.getNoOfOEL()
					+s.getNumOfOellCompleted()+s.getCompulsoryCourses().size();
			if(s.hasOptional || s.isSummerTerm)
				count++;
		}
		this.noOfTotalCourses = count;
	}

	public void setNoOfCompletedCourses (){
		int prevTerm = elSheet.getPrevTerm();
		int [] ys = getSemTerm(prevTerm);
		int count = 0;


		int countOfsems = this.getNoofSems(ys);
		for(int i=0;i<countOfsems;i++){

			Semester s= elSheet.getChart().getSemsInChart().get(i);
			for(Course c : s.getAllCourses()){

				c.checkAndSetGradeValidAndGradeComplete();
				if(c.isGradeComplete()){
					count++;
				} else {
					if(c.isInProgress()!=null && c.isInProgress().equalsIgnoreCase("Y")){
						count++;
					} else
						this.backLogCourses.add(c);
				}
			}
		}
		this.noOfCompletedCourses =  count;
	}


	public int getNoofSems(int[] ys){
		int i = 0;
		for(Semester s: elSheet.getChart().getSemsInChart()){
			if (s.getYearNo() < ys[0]){
				i++;
			}
			if((s.getYearNo() == ys[0])&& s.getSemNo()<=ys[1]){
				i++;
			}


		}

		return i;
	}

}