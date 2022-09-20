/* ========================================================
 * Author: Cristian Capraro
 * Date: September 2022
 * This class refers to a single book as a physical object.
 * THIS IS THE EVOLVED VERSION of Book class, which exist
 * only for legacy reasons.
 * --------------------------------------------------------
 * This class will be used to store standard book entry
 * ======================================================== */

package it.books.base;

import javax.naming.ldap.InitialLdapContext;

public class EvBook {
	//Lease structure
	private class Lease{
		public Lease(){}; //Init (not in lease)

		public void setLease(String LeaseBegin, String LeaseExpectedEnd, String Name, String Surname, String Tel){
			InLeasing = true;
			this.LeaseBegin = LeaseBegin;
			this.LeaseExpectedEnd = LeaseExpectedEnd;
			this.Name = Name;
			this.Surname = Surname;
			this.Tel = Tel;
		}

		public void endLease(){
			InLeasing = false;
			LeaseBegin = null;
			LeaseExpectedEnd = null;
			Name = null;
			Surname = null;
			Tel = null;
		}

		//Lease details
		private boolean InLeasing = false;
		private String LeaseBegin;
		private String LeaseExpectedEnd;

		//User data
		private String Name, Surname, Tel;

		public boolean isInLeasing() {
			return InLeasing;
		}

		public String getLeaseBegin() {
			return LeaseBegin;
		}

		public String getLeaseExpectedEnd() {
			return LeaseExpectedEnd;
		}

		public String getName() {
			return Name;
		}

		public String getSurname() {
			return Surname;
		}

		@Override
		public String toString(){
			if(isInLeasing()){
				return "Attualmente in prestito";
			}else{
				return "Non in prestito";
			}
		}
	}

	//Book structure
	private long id;

	private String code; //ISBN 13+5 characters allowed max
	private String title; //Italian title
	private String authors;
	private String original; //Original title
	private String genre; //Depends on memorization style

	private String year;
	private String edition;
	private String publisher; // Editor

	private String series;

	private int pages;
	private String pagesFormat;

	//Localization
	private String country;
	private String shelf;

	private String ownDate;

	//Constructor and getter
	public EvBook(long id, String code, String title, String authors) {
		this.id = id;
		this.code = code;
		this.title = title;
		this.authors = authors.replace(";", ", ");
	}

	public Lease lease = new Lease();

	public void endLease(){
		lease.endLease();
	}

	public void setLease(String LeaseBegin, String LeaseExpectedEnd, String Name, String Surname, String Tel){
		lease.setLease(LeaseBegin, LeaseExpectedEnd, Name, Surname, Tel);
	}

	public Lease getLease() {
		return lease;
	}

	public void addDetails(String originalTitle, String genre, String year, String edition, String publisher, String series, int pages, String pageFormat, String country, String shelf, String ownDate){
		this.original = originalTitle;
		this.genre = genre;
		this.year = year;
		this.edition = edition;
		this.publisher = publisher;
		this.series = series;
		this.pages = pages;
		this.pagesFormat = pageFormat;
		this.country = country;
		this.shelf = shelf;
		this.ownDate = ownDate;
	}


	@Override
	public String toString(){
		return "EvBook ID: " + id + "\nTitle: " + title + "/" + original + "\n";
	}

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public int getPages() {
		return pages;
	}

	public String getCode() {
		return code;
	}

	public String getCountry() {
		return country;
	}

	public String getEdition() {
		return edition;
	}

	public String getGenre() {
		return genre;
	}

	public String getOriginal() {
		return original;
	}

	public String getOwnDate() {
		return ownDate;
	}

	public String getPagesFormat() {
		return pagesFormat;
	}

	public String getPublisher() {
		return publisher;
	}

	public String getSeries() {
		return series;
	}

	public String getShelf() {
		return shelf;
	}

	public String getYear() {
		return year;
	}

	public String getAuthors() {
		return authors;
	}

}
