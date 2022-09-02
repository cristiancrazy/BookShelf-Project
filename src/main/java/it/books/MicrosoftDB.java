/* ==========================================
 * Author: Cristian Capraro
 * ------------------------------------------
 * This class supply functions to  connect
 * and retrieve information from a Microsoft
 * Access Database.
 * ========================================== */

package it.books;

import it.books.base.Book;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

public class MicrosoftDB {
    /** Connect and retrieve a list of books from a given database
     * @param dbFile specify the database file to work on
     * @param limit specify the maximum number of book given (set to 0 for infinity)
     * @param offset specify the offset from the first row in the table
     * @return the list of specified book
     * **/
    public static ArrayList<Book> connectAndGet(File dbFile, int offset, int limit){
        // Supply bug
        String db = dbFile.getAbsolutePath().replace("\\", "/");

        try(Connection conn= DriverManager.getConnection("jdbc:ucanaccess://"+db+";memory=true")){
            //Connect and get raw results
            Statement query = conn.createStatement();
            ResultSet rawResults;
            if(limit == 0){ //No limit - load entirely
                rawResults = query.executeQuery("SELECT * FROM LIBRI OFFSET " + offset + " ROWS");
            }else{
                rawResults = query.executeQuery("SELECT * FROM LIBRI LIMIT " + offset + ", " + limit);
            }

            //Cycle on results
            ArrayList<Book> fetched = new ArrayList<>();
            while(rawResults.next()){
                //Set book data
                Book book = new Book(rawResults.getString("codice"), rawResults.getString("titolo"));
                book.setDetails(rawResults.getString("autore"), rawResults.getBoolean("unico_autore"), rawResults.getString("collana"), rawResults.getString("genere"), rawResults.getString("editore"), rawResults.getString("edizione"), rawResults.getString("anno"));
                book.setDetails(rawResults.getString("scaffale"), (rawResults.getString("pro_dal_gg") +"/" + (rawResults.getString("pro_dal_mm") + "/" + rawResults.getString("pro_dal_aa"))), rawResults.getString("commento"));
                book.setLocale(rawResults.getString("nazione"), rawResults.getString("traduttore"), rawResults.getString("titolo_orig"));
                book.setPages(rawResults.getInt("pagine"), rawResults.getString("formato"));
                if(rawResults.getBoolean("prestito")){
                    book.setLeasing(true, (rawResults.getString("pre_dal_gg") + "/" + rawResults.getString("pre_dal_mm") + "/" + rawResults.getString("pre_dal_aa")), rawResults.getString("prestitario"));
                }else{
                    book.setLeasing(false, "", "");
                }
                //Add book to the list
                fetched.add(book);
            }

            //Trying an automatic optimization
            System.gc();
            return fetched;

        }catch (SQLException exc){
            return null; //Got error -> return null
        }
    }

}
