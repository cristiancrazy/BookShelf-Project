/* ==========================================
 * Author: Cristian Capraro
 * ------------------------------------------
 * This class supply functions to  connect
 * and retrieve information from a Microsoft
 * Access Database.
 * ========================================== */

package it.books;

import com.healthmarketscience.jackcess.*;
import it.books.base.Book;
import it.books.base.EvBook;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

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

    /** Connect and retrieve the entire list of books from a given database
     * @param dbFile specify the database file to work on
     * @return the list of specified book
     * **/
    public static ArrayList<Book> connectAndGet(File dbFile){
        return connectAndGet(dbFile, 0, 0);
    }

    /** Connect and search on the entire list of books from a given database
     * @param dbFile specify the database file to work on
     * @param author specify the book's author
     * @param code specify the code of book
     * @param genre specify the genre of book (could be Decimal/Dewey)
     * @param title specify the book's title
     * @return the list of books which matches with specified fields.
     * **/
    public static ArrayList<Book> connectAndSearch(File dbFile, String title, String code, String author, String genre){
        String db = dbFile.getAbsolutePath().replace("\\", "/");
        try(Connection conn = DriverManager.getConnection("jdbc:ucanaccess://"+db+";memory=true")){
            Statement statement = conn.createStatement();
            String query;
            query = "SELECT * FROM LIBRI WHERE " + ((title.isEmpty())? "" : "titolo LIKE '%" + title + "%' ");
            query += (title.isEmpty()&&(!code.isEmpty()))? "codice LIKE '%"+code+"%' " : (!code.isEmpty())? "AND WHERE codice LIKE '%" + code + "%' " : "";
            query += (title.isEmpty()&&code.isEmpty()&&(!author.isEmpty()))? "autore LIKE '%" + author + "%' " : (!author.isEmpty())? "AND WHERE autore = '%" + author + "%' " : "";
            query += (title.isEmpty()&&code.isEmpty()&&author.isEmpty()&&(!genre.isEmpty()))? "genere LIKE '%" + genre + "%' " : (!genre.isEmpty())? "AND WHERE genere LIKE '%" + genre + "%'" : "";
            ResultSet res = statement.executeQuery(query);
            ArrayList<Book> bookList = new ArrayList<>();
            //Fetch results
            while(res.next()){
                Book book = new Book(res.getString("codice"), res.getString("titolo"));
                book.setDetails(res.getString("autore"), res.getBoolean("unico_autore"), res.getString("collana"), res.getString("genere"), res.getString("editore"), res.getString("edizione"), res.getString("anno"));
                book.setDetails(res.getString("scaffale"), (res.getString("pro_dal_gg") +"/" + (res.getString("pro_dal_mm") + "/" + res.getString("pro_dal_aa"))), res.getString("commento"));
                book.setLocale(res.getString("nazione"), res.getString("traduttore"), res.getString("titolo_orig"));
                book.setPages(res.getInt("pagine"), res.getString("formato"));
                if(res.getBoolean("prestito")){
                    book.setLeasing(true, (res.getString("pre_dal_gg") + "/" + res.getString("pre_dal_mm") + "/" + res.getString("pre_dal_aa")), res.getString("prestitario"));
                }else{
                    book.setLeasing(false, "", "");
                }
                bookList.add(book);
            }
            return bookList;

        }catch (SQLException e){
            return null;
        }

    }

    /** This method must be used to create a new BookShelf Database.
     * @param dbFile specify the database file to init
     * @return true if the operation will end successfully, false
     * otherwise.
     * **/
    public static boolean CreateAndInit(File dbFile){
        try(Database db = DatabaseBuilder.create(Database.FileFormat.V2016, dbFile)){
            TableBuilder table1 = new TableBuilder("LIBRI");

            table1.addColumns(List.of(
                    new ColumnBuilder("ID", DataType.LONG).setAutoNumber(true),
                    new ColumnBuilder("Codice", DataType.TEXT),
                    new ColumnBuilder("Titolo", DataType.TEXT),
                    new ColumnBuilder("Autore", DataType.TEXT),
                    new ColumnBuilder("Genere", DataType.TEXT),
                    new ColumnBuilder("Editore", DataType.TEXT),
                    new ColumnBuilder("Edizione", DataType.TEXT),
                    new ColumnBuilder("Collezione", DataType.TEXT),
                    new ColumnBuilder("Posseduto_Dal", DataType.TEXT),
                    new ColumnBuilder("Pagine", DataType.INT),
                    new ColumnBuilder("Anno_Pubblicazione", DataType.INT),
                    new ColumnBuilder("Nazione", DataType.TEXT),
                    new ColumnBuilder("Scaffale", DataType.TEXT),
                    new ColumnBuilder("Commento", DataType.TEXT),
                    new ColumnBuilder("Formato_Pagine", DataType.TEXT),
                    new ColumnBuilder("Traduttore", DataType.TEXT),
                    new ColumnBuilder("Titolo_Originale", DataType.TEXT)
            )).toTable(db);

            TableBuilder table2 = new TableBuilder("PRESTITI");
            table2.addColumns(List.of(
                    new ColumnBuilder("Codice", DataType.TEXT),
                    new ColumnBuilder("In_Prestito", DataType.BOOLEAN),
                    new ColumnBuilder("Data_Inizio_Prestito", DataType.TEXT),
                    new ColumnBuilder("Data_Fine_Prestito", DataType.TEXT),
                    new ColumnBuilder("Prestato_A", DataType.TEXT)
            )).toTable(db);

            System.out.println(db.getTable("LIBRI").getColumns());

            System.out.println("OK");
            return true;
        }catch (IOException exc){
            exc.printStackTrace();
            return false;
        }
    }

    /** [*** NEW DATABASE] Connect and retrieve a list of books from a given database
     * @param dbFile specify the database file to work on
     * @param limit specify the maximum number of book given (set to 0 for infinity)
     * @param offset specify the offset from the first row in the table
     * @return the list of specified book
     * **/

    public static ArrayList<EvBook> connectAndGetNew(File dbFile, int offset, int limit){
        String db = dbFile.getAbsolutePath().replace("\\", "/"); //Solve bugs
        try(Connection conn= DriverManager.getConnection("jdbc:ucanaccess://"+db+";memory=true")){
            Statement query = conn.createStatement();
            ResultSet res = query.executeQuery((limit == 0)? ("SELECT * FROM LIBRI OFFSET " + offset + " ROWS") : ("SELECT * FROM LIBRI LIMIT " + offset + ", " + limit));
            ArrayList<EvBook> books = new ArrayList<>();
            while(res.next()){
                EvBook book = new EvBook(res.getInt("ID"), res.getString("Codice"), res.getString("Titolo"), res.getString("Autore"));
                book.addDetails(res.getString("Titolo_Originale"), res.getString("Genere"), res.getString("Anno_Pubblicazione"), res.getString("Edizione"), res.getString("Editore"), res.getString("Collezione"), res.getInt("Pagine"), res.getString("Formato_Pagine"), res.getString("Nazione"), res.getString("Scaffale"), res.getString("Posseduto_Dal"));
                ResultSet pre = query.executeQuery("SELECT * FROM PRESTITI WHERE Codice=\""+book.getCode()+"\"");
                if(pre.next()){
                    book.setLease(pre.getString("Data_Inizio_Prestito"), pre.getString("Data_Fine_Prestito"), pre.getString("Prestato_A").split(";")[0], pre.getString("Prestato_A").split(";")[1], pre.getString("Prestato_A").split(";")[2]);
                }

                books.add(book);
            }
            return books;
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    /** [*** NEW DATABASE] Connect and retrieve the entire list of books from a given database
     * @param dbFile specify the database file to work on
     * @return the list of specified book
     * **/
    public static ArrayList<EvBook> connectAndGetNew(File dbFile){
        return connectAndGetNew(dbFile, 0, 0);
    }

    /** [*** NEW DATABASE] Used to delete an entry in the specified book database.
     * Use at least a param to ensure the book deletion;
     * @param dbFile necessary to work on the specified db
     * @param id the book id required to remove the book instantly (could be null)
     * @param title the book title required to remove the book instantly (could be null)
     * @param code required to remove the specified book (it could be null)
     * @return true whether the action was correctly applied. False otherwise.
     * **/
    public static boolean DeleteEntry(File dbFile, int id, String code, String title) {
        try(Database db = DatabaseBuilder.open(dbFile)){
            Iterator<Row> rows = db.getTable("LIBRI").iterator();
            Iterator<Row> prestitiRows = db.getTable("PRESTITI").iterator();
            Row row, row2;
            while(rows.hasNext()){
                row = rows.next();
                if((row.getString("Codice").equals(code)) || (row.getString("Titolo").equals(title))){
                    db.getTable("LIBRI").deleteRow(row);
                    db.flush();
                }
            }
            while(prestitiRows.hasNext()){
                row2 = prestitiRows.next();
                if((row2.getString("Codice").equals(code))){
                    db.getTable("PRESTITI").deleteRow(row2);
                    db.flush();
                }
            }
            return false; //Not found
        }catch (IOException e){
            return false;
        }
    }

    /**
     * This method is used to connect to local db and retrieve the last id number.
     * @param dbFile specify the database file to work on
     * @return integer result of the last registered id. Return -1 if an error occurred.
     **/
    public static int connectAndGetLastID(File dbFile){
        String path = dbFile.getAbsolutePath();
        path = path.replace("\\", "/"); //Solve a bug
        try(Connection db = DriverManager.getConnection("jdbc:ucanaccess://"+path+";memory=true")){
            ResultSet res = db.createStatement().executeQuery("SELECT * FROM LIBRI ORDER BY ID DESC LIMIT 1");
            res.next(); //Skip and read the first row
            return res.getInt("ID");
        }catch (SQLException exc){
            //Debug exc.printStackTrace();
            return -1;
        }
    }

    /** Connect to the database file and add a new book entry
     * @param dbFile specify the database which work on;
     * @param book the adv. book element to add to the database;
     * @return true whether the procedure was successful. Otherwise, if error occurred, it returns false.
     * **/
    public static int connectAndUploadANewBook(File dbFile, EvBook book){
        try(Database db = DatabaseBuilder.open(dbFile)){
            db.getTable("LIBRI").addRow(null, book.getCode(), book.getTitle(), book.getAuthors(), book.getGenre(), book.getPublisher(), book.getEdition(), book.getSeries(), book.getOwnDate(), Short.parseShort(Integer.toString(book.getPages())), Short.parseShort(book.getYear()), book.getCountry(), book.getShelf(), null, book.getPagesFormat(), null, book.getOriginal());
            db.flush();
            if(book.isLeasing()){
                //Codice, is in leasing, data inzio, data fine, a...
                String[] personData = book.leasedTo().split(";");
                db.getTable("PRESTITI").addRow(book.getCode(), book.isLeasing(), book.getBeginDate(), book.getEndDate(), personData[0]+";"+personData[1]+";"+personData[2]);
                db.flush();
            }
        }catch (IOException e){
            System.out.println("Impossibile aggiungere il libro al database.");
            //Debug e.printStackTrace();
            return -1;
        }
        return 0;
    }

    /** Search by code (on newest database)
     * @param dbFile specify the database which work on;
     * @param code specify the book code;
     * **/
    public static EvBook connectAndSearch(File dbFile, String code){
        String path = dbFile.getAbsolutePath();
        path = path.replace("\\", "/"); //Solve a bug
        try(Connection db = DriverManager.getConnection("jdbc:ucanaccess://"+path+";memory=true")){
            Statement st = db.createStatement();
            ResultSet res = st.executeQuery("SELECT * FROM LIBRI WHERE Codice = '"+code+"'");
            while (res.next()){
                EvBook book = new EvBook(res.getInt("ID"), res.getString("Codice"), res.getString("Titolo"), res.getString("Autore"));
                book.addDetails(res.getString("Titolo_Originale"), res.getString("Genere"), res.getString("Anno_Pubblicazione"), res.getString("Edizione"), res.getString("Editore"), res.getString("Collezione"), res.getInt("Pagine"), res.getString("Formato_Pagine"), res.getString("Nazione"), res.getString("Scaffale"), res.getString("Posseduto_Dal"));
                return book;
            }
        }catch (Exception e){
            return null;
        }
        return null;
    }

    /** Connect to the database and add a new lease entry
     * @param dbFile specify the database which work on;
     * @param code specify the book code
     * @param beginDate specify the beginning of leasing;
     * @param expectedEnd specify the expected end of leasing;
     * @param name specify the name of user;
     * @param surname specify the surname of user;
     * @param tel specify the phone number of user;
     * **/
    public static void connectAndAddLease(File dbFile, String code, LocalTime beginDate, LocalTime expectedEnd, String name, String surname, String tel){
        ArrayList<EvBook> books = connectAndGetNew(dbFile);
        try{
            EvBook data = books.stream().filter(e -> e.getCode().equals(code)).findFirst().orElse(null);
            if(data == null) return;
            data.setLease(beginDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), expectedEnd.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), name, surname, tel);
            DeleteEntry(dbFile, -1, null, data.getTitle());
            connectAndUploadANewBook(dbFile, data);
        }catch (NoSuchElementException e){
            return;
        }
    }
}
