package com.example.pw_proj2;

import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.Duration;

import java.io.File;
import java.net.MalformedURLException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;


public class FxManager {
    int osMost = 0;//zmienne do wyswietlania stanu
    int osStatek = 0;
    int blMost = 0;
    GridPane grid = new GridPane();

    Label stan = new Label("stan:");
    Label most = new Label("stan mostu: " + osMost + " osob + " + blMost + " blokad / " + Main.K);
    Label lSt = new Label("stan statku: " + osStatek + " osob / " + Main.N);
    ImageView kap = kap();//załaduj obrazy kapitana i blokady
    ImageView blok = blok();


    Scene s1(Stage s) {// zapytanie o dane wejsciowe
        s.setTitle("podaj dane");
        TilePane pane = new TilePane();
        pane.setPrefColumns(2);
        pane.setAlignment(Pos.CENTER);

        Label Nl = new Label("pojemnosc statku N:");
        pane.getChildren().add(Nl);
        TextField Nf = new TextField(Integer.toString(Main.N));
        pane.getChildren().add(Nf);

        Label Kl = new Label("pojemnosc mostu K:");
        pane.getChildren().add(Kl);
        TextField Kf = new TextField(Integer.toString(Main.K));
        pane.getChildren().add(Kf);

        Label Cl = new Label("czy wysiadają mostem:");
        pane.getChildren().add(Cl);
        CheckBox Cf = new CheckBox();
        Cf.setSelected(Main.czyWysiadaja);
        pane.getChildren().add(Cf);

        Button okButton = new Button("OK!");
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {//przycisk: wez dane i wyjdz
                try {
                    //ustaw zmienne dla maina, thready tutaj by 2gie okno z animacja działalo jednoczesnie z kapitanem i pasazerem
                    Main.N = Integer.valueOf(Nf.getText());
                    Main.K = Integer.valueOf(Kf.getText());
                    Main.czyWysiadaja = Cf.isSelected();

                    Main.SlotMost = new Semaphore(Main.K);
                    Main.kap = new Kapitan(Main.WejscieMost, Main.SlotMost, Main.kryt, Main.spr, Main.display);
                    Main.pas = new Pasażer(Main.WejscieMost, Main.SlotMost, Main.kryt, Main.spr, Main.display);

                    System.out.println("Statek: " + Main.n + "/" + Main.N + ", most: " + Main.k + "/" + Main.K);

                    Main.kap.start();
                    Main.pas.start();
                    s.setScene(s2(s));
                } catch (Exception e) {
                    System.out.println("niepoprawne dane");
                    e.printStackTrace();
                }
            }
        });
        pane.getChildren().add(okButton);

        Scene scene1 = new Scene(pane, 400, 400);
        return scene1;
    }

    Scene s2(Stage s) {//Scena z animacjami
        s.setTitle("wykonywanie..");
        grid.setAlignment(Pos.TOP_CENTER);
        //załąduj tlo
        BackgroundImage statek = null;
        try {
            statek = new BackgroundImage(new Image((new File("./src/main/resources/scene.png").
                    toURI().toURL().toString()))
                    , BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER, new BackgroundSize(1.0, 1.0, true, true, false, false));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        grid.setBackground(new Background(statek));

        //Labele stanu
        grid.add(stan, 1, 0);
        grid.add(most, 1, 1);
        grid.add(lSt, 1, 2);

        Button end = new Button("INTERRUPT");//przycisk do zakonczenia
        end.setOnAction(e->{

            Main.pas.interrupt();
            Main.kap.interrupt();
            s.close();
            System.out.println("KONIEC");
        });
        grid.add(end,1,3);
        //kapitan
        kap.setTranslateY(130);
        grid.add(kap, 1, 10);

        Scene scene2 = new Scene(grid, 1000, 600);
        return scene2;
    }

    ImageView pas() {//załaduj obraz pasażera
        Image pas_lad = null;
        try {
            pas_lad = new Image((new File("./src/main/resources/pa.png").
                    toURI().toURL().toString()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        ImageView pas = new ImageView((pas_lad));
        return pas;
    }

    ImageView kap() {//załaduj obraz kapitana
        Image kap = null;
        try {
            kap = new Image((new File("./src/main/resources/ka.png").
                    toURI().toURL().toString()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        ImageView kapitan = new ImageView((kap));
        return kapitan;
    }

    ImageView blok() {//załaduj obraz blokady
        Image bl = null;
        try {
            bl = new Image((new File("./src/main/resources/bl.png").
                    toURI().toURL().toString()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        ImageView blokada = new ImageView((bl));
        return blokada;
    }

    public Duration getDuration(int liczba, int max) {//przyspieszanie animacji dla dużej liczby powtorzen
        double x = 1;//domyslna dlugosc = 1s
        if (x * liczba > max) {//skroc by suma powtorzen trwala max sekund
            x = (double) max / (double) liczba;
        }
        return Duration.seconds(x);
    }

    public void lad_most(int liczba) {//animacja: pasazerowie z ladu na most: <liczba> powtorzen
        ImageView pasazer = pas();
        Duration d = getDuration(liczba, 5);
        pasazer.setTranslateX(-300);
        pasazer.setTranslateY(280);
        grid.add(pasazer, 1, 10);
        TranslateTransition ttpm = new TranslateTransition(d, pasazer);
        ttpm.setByX(240);
        ttpm.setCycleCount(liczba);
        updateLabels(most, lSt, stan, "wchodzenie na most");
        ttpm.play();
        ttpm.setOnFinished(e -> {
            osMost += liczba;//update daych dla labeli
            updateLabels(most, lSt, stan, "wchodzenie na most");
            grid.getChildren().remove(pasazer);
            Main.display.release();

        });
    }

    public void most_statek(int liczba) {//animacja: pasazerowie z mostu na statek
        ImageView pasazer = pas();
        Duration d = getDuration(liczba, 5);
        pasazer.setTranslateX(150);
        pasazer.setTranslateY(280);
        grid.add(pasazer, 1, 10);
        TranslateTransition ttms = new TranslateTransition(d, pasazer);
        ttms.setByX(100);
        ttms.setCycleCount(liczba);
        updateLabels(most, lSt, stan, "wchodzenie na statek");
        ttms.play();
        ttms.setOnFinished(e -> {
            osMost -= liczba;//update daych dla labeli
            osStatek += liczba;
            updateLabels(most, lSt, stan, "");
            grid.getChildren().remove(pasazer);
            Main.display.release();
        });
    }

    public void czesciowaBlokada(int liczba) {//animacja: kapitan czesciowo blokuje most: <liczba> blokad
        ImageView blokada = blok();
        Duration d = getDuration(liczba, 5);
        blokada.setTranslateX(0);
        blokada.setTranslateY(120);
        grid.add(blokada, 1, 10);
        TranslateTransition ttbc = new TranslateTransition(d, blokada);
        ttbc.setByX(-50);
        ttbc.setByY(160);
        ttbc.setCycleCount(liczba);
        updateLabels(most, lSt, stan, "blokada częsci mostu");
        ttbc.play();
        ttbc.setOnFinished(e -> {
            blMost += liczba;//update daych dla labeli
            updateLabels(most, lSt, stan, "");
            grid.getChildren().remove(blokada);
            Main.display.release();
        });
    }

    public void zalozBlokadeMostu() {
        blok.setTranslateX(0);
        blok.setTranslateY(120);
        grid.add(blok, 1, 10);
        TranslateTransition ttbca = new TranslateTransition(Duration.seconds(1), blok);//blokada na wejscie
        ttbca.setByX(-200);
        ttbca.setByY(170);
        ttbca.play();
        ttbca.setOnFinished(e -> {
            Main.display.release();
        });
    }

    public void zdejmijBlokadeMostu(String nowystan) {

        TranslateTransition ttbca = new TranslateTransition(Duration.seconds(1), blok);//blokada na wejscie
        ttbca.setByX(200);
        ttbca.setByY(-170);
        updateLabels(most, lSt, stan, nowystan);
        ttbca.play();
        ttbca.setOnFinished(e -> {
            grid.getChildren().remove(blok);
            Main.display.release();
        });
    }


    public void rejs() {//animacja: rejs

        updateLabels(most, lSt, stan, "rejs trwa");

        TranslateTransition ttks = new TranslateTransition(Duration.seconds(2), kap);//kapitan na statek
        ttks.setByX(350);
        ttks.play();
        ttks.setOnFinished(e2 -> {
            Main.display.release();
        });

    }

    public void koniecRejsuFalse() {//koniec rejsu dla czyWysiadaja=False
        TranslateTransition ttks = new TranslateTransition(Duration.seconds(2), kap);//kapitan na lad
        ttks.setByX(-350);
        ttks.play();
        ttks.setOnFinished(e2 -> {
                osMost = 0;
                osStatek = 0;
                blMost = 0;
            updateLabels(most, lSt, stan, "koniec resju");
            TranslateTransition ttbca = new TranslateTransition(Duration.seconds(1), blok);//blokada z wejscia
            ttbca.setByX(200);
            ttbca.setByY(-160);

            ttbca.play();
            ttbca.setOnFinished(e -> {
                grid.getChildren().remove(blok);
                updateLabels(most, lSt, stan, "");
                Main.display.release();
            });

        });
    }

    public void koniecRejsuTrue() {//koniec rejsu dla czyWysiadaja=True
        TranslateTransition ttks = new TranslateTransition(Duration.seconds(2), kap);//kapitan na lad
        ttks.setByX(-350);
        ttks.play();
        ttks.setOnFinished(e2 -> {
            blMost = 0;
            updateLabels(most, lSt, stan, "koniec resju");
            Main.display.release();
        });
    }

    public void Wysiadanie_statek_most(int liczba) {//animacja: pasazerowie z statku na most (czyWysiadaja=True)
        ImageView pasazer = pas();
        Duration d = getDuration(liczba, 5);
        pasazer.setTranslateX(250);
        pasazer.setTranslateY(280);
        grid.add(pasazer, 1, 10);
        TranslateTransition ttms = new TranslateTransition(d, pasazer);
        ttms.setByX(-100);
        ttms.setCycleCount(liczba);
        updateLabels(most, lSt, stan, "wysiadanie ze statku");
        ttms.play();
        ttms.setOnFinished(e -> {
            osMost += liczba;
            osStatek -= liczba;
            updateLabels(most, lSt, stan, "");
            grid.getChildren().remove(pasazer);
            Main.display.release();

        });
    }

    public void Wysiadanie_most_lad(int liczba) {//animacja: pasazerowie z mostu na lad (czyWysiadaja=True)
        ImageView pasazer = pas();
        Duration d = getDuration(liczba, 5);
        pasazer.setTranslateX(-60);
        pasazer.setTranslateY(280);
        grid.add(pasazer, 1, 10);
        TranslateTransition ttpm = new TranslateTransition(d, pasazer);
        ttpm.setByX(-300);
        ttpm.setByY(200);
        ttpm.setCycleCount(liczba);
        updateLabels(most, lSt, stan, "wysiadanie na lad");
        ttpm.play();
        ttpm.setOnFinished(e -> {
            osMost -= liczba;
            updateLabels(most, lSt, stan, "wysiadanie na lad");
            grid.getChildren().remove(pasazer);
            Main.display.release();
        });


    }

    void updateLabels(Label lmost, Label lstatek, Label lstan, String stan) {//update tekstu w labelach
        lmost.setText("stan mostu: " + osMost + " osob + " + blMost + " blokad / " + Main.K);
        lstatek.setText("stan statku: " + osStatek + " osob / " + Main.N);
        lstan.setText("stan: " + stan);
    }

}
