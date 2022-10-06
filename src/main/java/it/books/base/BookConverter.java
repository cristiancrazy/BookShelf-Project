/* =================================================
 * Author: Cristian Capraro
 * ------------------------------------------------
 * This class contains static method which could be
 * used to convert old legacy book type to the newer
 * ones. This class is a sort of converter toolkit
 * ================================================= */




package it.books.base;

public class BookConverter {

	/**
	 * Convert old type of book to EvBook format.
	 * @param book object to be converted to the new format
	 * @param id the new object requires a unique identifier
	 * @return The new EvBook object, correctly converted;
	 **/
	public static EvBook ConvertToEvBook(Book book, long id){
		EvBook newBook = new EvBook(id, book.getCode(), book.getTitle(), book.getAuthor());
		newBook.addDetails(book.getOriginalTitle(), book.getGenre(), book.getYear(), book.getEdition(), book.getEditor(), book.getSeries(), book.getPages(), book.getPageFormat(), book.getNation(), book.getShelf(), book.getOwnDate());
		newBook.setComments(book.getComments());
		if(book.getLeasing().equals("Si"))
			try{
					newBook.setLease(book.getLeasingDate(), null, book.getLeasedTo().split(" ")[0], book.getLeasedTo().split(" ")[1], null);
			}catch (ArrayIndexOutOfBoundsException e){
				newBook.setLease(book.getLeasingDate(), null, book.getLeasedTo(), null, null);
			}
		return newBook;
	}
}
