/* ====================================================
 * Author: Cristian Capraro
 * Date: September 2022
 * ----------------------------------------------------
 * Superclass of Book - This contains only useful data
 * that could be useful for displaying information.
 * ==================================================== */



package it.books.base;

import java.util.List;

abstract class MetaBook {
    protected static List<String> localeConfigField;

    //Init
    static {
        localeConfigField = List.of("Codice", "Titolo", "Autore", "Genere", "Editore", "Collana", "Pagine", "Anno", "Nazione", "Scaffale", "In possesso dal", "In prestito", "In prestito dal", "Prestato a", "Commenti", "Singolo autore", "Edizione", "Formato", "Traduttore", "Titolo originale");
    }
}
