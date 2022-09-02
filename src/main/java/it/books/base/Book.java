/* ========================================================
 * Author: Cristian Capraro
 * Date: September 2022
 * This class refers to a single book as a physical object.
 * ======================================================== */

package it.books.base;
import java.util.List;

public class Book extends MetaBook {
    private String code;
    private String title;
    private String author;
    private String genre;
    private String editor;
    private String series;
    private int pages;
    private String year;
    private String nation;
    private String shelf;
    private String ownDate; //Built composite string
    private String leasing;
    private String leasingDate; //Built composite string
    private String leasedTo;
    private String comments;
    private String uniqueAuthor;
    private String edition;
    private String pageFormat;
    private String translator;
    private String originalTitle;


    //Constructor
    public Book(String code, String title){
        this.code = code;
        this.title = title;
    }

    //Static

    /** Get field translation for local language **/
    public static List<String> getLocale(){
        return localeConfigField;
    }

    //Non-static


    public void setLeasing(boolean lease, String leasingDate, String leasedTo) {
        this.leasing = (lease)? "Si" : "No";
        this.leasingDate = leasingDate;
        this.leasedTo = leasedTo;
    }

    public void setDetails(String author, boolean uniqueAuthor,String series, String genre, String editor, String edition, String year){
        this.author = author;
        this.uniqueAuthor = (uniqueAuthor)? "Si" : "No";
        this.series = series;
        this.genre = genre; //Dewey format
        this.editor = editor;
        this.edition = edition;
        this.year = year;
    }

    public void setDetails(String shelf, String ownDate, String comments){
        this.shelf = shelf;
        this.ownDate = ownDate;
        this.comments = comments;
    }

    public void setPages(int pages, String pageFormat){
        this.pages = pages;
        this.pageFormat = pageFormat;
    }

    public void setLocale(String nation, String translator, String originalTitle){
        this.nation = nation;
        this.translator = translator;
        this.originalTitle = originalTitle;
    }

    @Override
    public String toString(){
        return "Titolo -> " + this.title + System.lineSeparator() + "Codice -> " + this.code;
    }

    //Getter

    public String getCode() {
        return code;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public int getPages() {
        return pages;
    }

    public String getComments() {
        return comments;
    }

    public String getEdition() {
        return edition;
    }

    public String getEditor() {
        return editor;
    }

    public String getLeasingDate() {
        return leasingDate;
    }

    public String getNation() {
        return nation;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getOwnDate() {
        return ownDate;
    }

    public String getPageFormat() {
        return pageFormat;
    }

    public String getSeries() {
        return series;
    }

    public String getShelf() {
        return shelf;
    }

    public String getTranslator() {
        return translator;
    }

    public String getYear() {
        return year;
    }

    public String getLeasing() {
        return leasing;
    }

    public String getUniqueAuthor() {
        return uniqueAuthor;
    }

    public String getLeasedTo() {
        return leasedTo;
    }
}
