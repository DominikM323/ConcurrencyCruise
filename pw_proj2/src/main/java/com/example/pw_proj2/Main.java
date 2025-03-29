package com.example.pw_proj2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.concurrent.*;
import static java.lang.Thread.sleep;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.File;
import java.io.IOException;

public class Main extends Application {
    static Stage s;
    public static Kapitan kap;
    public static Pasażer pas;
    static FxManager man = new FxManager();//obiekt odpowiedzialny za wyswietlanie
    public static volatile int N = 5;// maks na statku
    public static volatile int K = 2;// maks na moscie
    public static volatile int n = 0;// obecnie na statku
    public static volatile int k = 0;// obecnie na moscie
    public static volatile boolean czyWysiadaja = false;//okresla wybrany przypadek
    public static volatile boolean czyPoRejsie = false;// zmienna okresla kiedy pas. wysiadaja w przypadku czyWys. = true
    static volatile Semaphore WejscieMost;//pozwala pasazerom wejsc na most
    static volatile Semaphore SlotMost;//okresla zapełnienie mostu, (K permitów)
    static volatile Semaphore kryt;//ochrona sek. krytycznych
    static volatile Semaphore spr;//ustawiana po wejsciu grupy na statek; pozwala kapitanowi sprawdzic pojemnosc
    static volatile Semaphore wrocil;//okresla kiedy pas. moga wysiadac przy czyWysiadaja = true
    static volatile Semaphore display;//zapewnia wyswietlanie 1 animacji na raz

    @Override
    public void start(Stage stage) throws IOException {//odpal 1-szy ekran
        Scene scene = man.s1(stage);
        s=stage;
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {

        WejscieMost = new Semaphore(1);
        kryt = new Semaphore(1);
        spr = new Semaphore(1);
        wrocil = new Semaphore(1);
        display = new Semaphore(1);
        try {//poczatkowo wrocil zajete
            wrocil.acquire();
        } catch (InterruptedException e) {
        }
        config();//ustaw N,K i czyWysiadaja z xml
        launch();//odpal aplikacje

        try {//wykonuj alg. 3 sekundy i wykonaj interrupt
            sleep(3000);
        } catch (InterruptedException e) {

        }finally {//zamknij procesy
            pas.interrupt();
            kap.interrupt();
        }

    }

    private static void config(){//odczytaj dane domyslne z XML
        try {

            File con = new File("./config.xml");//otworz plik xml
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document configDoc = db.parse(con);
            configDoc.getDocumentElement().normalize();
            NodeList nl = configDoc.getElementsByTagName("config");
            Element el =(Element) nl.item(0);
            //odczytaj zmienne z pliku xml
            N=Integer.valueOf(el.getElementsByTagName("N").item(0).getTextContent());
            K=Integer.valueOf(el.getElementsByTagName("K").item(0).getTextContent());
            czyWysiadaja=Boolean.valueOf(el.getElementsByTagName("czy").item(0).getTextContent());

            System.out.println("N="+N+" K="+K+" czy="+czyWysiadaja);
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}