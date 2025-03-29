package com.example.pw_proj2;

import javafx.application.Platform;

import java.util.concurrent.Semaphore;

public class Pasażer extends Thread {

    static Semaphore WejscieMost; //zezwol na wejscie na most
    static Semaphore SlotMost;//miejsca na moscie
    static Semaphore kryt;
    static Semaphore spr;//zezwala kapitanowi sprawdzic zapelnienie statku
    static Semaphore display;//zezwala zlecić animacje

    public Pasażer(Semaphore s1, Semaphore s2, Semaphore s3, Semaphore s4,Semaphore s5) {
        this.WejscieMost = s1;
        this.SlotMost = s2;
        this.kryt = s3;
        this.spr = s4;
        this.display=s5;
    }

    @Override
    public void run() {
        while (true) {
            try {//wejscie z mostu na statek
                kryt.acquire();
                if (Main.k > 0) {
                    Main.n += Main.k;//wszyscy na statek
                    int x =Main.k;//argument dla animacji: liczba powtorzen
                    while (Main.k != 0) {//wszyscy z mostu
                        SlotMost.release();
                        Main.k--;
                    }
                    spr.release();//most pusty i zamkniety: kapitan sprawdza pojemnosc statku
                    System.out.println("wejscie na statek");
                    System.out.println("Statek: " + Main.n + "/" + Main.N + ", most: " + Main.k + "/" + Main.K);
                    display.acquire();
                    Platform.runLater(()->{Main.man.most_statek(x);});//animacja
                }
                kryt.release();
            } catch (InterruptedException e) {
                break;
            }

            try {//wejscie na most z ladu

                if(WejscieMost.tryAcquire()){
                    kryt.acquire();
                    while (SlotMost.availablePermits() > 0) {//pasazerowie wchodza, zapelniajac most
                        SlotMost.acquire();
                        Main.k++;
                    }
                    int y =Main.k;//argument dla animacji

                    System.out.println("wejscie na most");
                    System.out.println("Statek: " + Main.n + "/" + Main.N + ", most: " + Main.k + "/" + Main.K);

                    display.acquire();
                    Platform.runLater(()->{Main.man.lad_most(y);});//animacja wchodzenia

                    display.acquire();
                    Platform.runLater(()->{Main.man.zalozBlokadeMostu();});//animacja zamkniecia mostu za pasazerami (ilustracja WejscieMost.tryAcquire())
                    kryt.release();
                }

            } catch (InterruptedException e) {
                break;
            }

            try {
                if(Main.wrocil.tryAcquire()) {//wysiadanie w przypadku czyWysiadaja = true
                    kryt.acquire();

                    if (Main.czyWysiadaja && Main.czyPoRejsie) {//wysiadanie tym samym mostem jesli czyWysiadaja = 1

                        while (Main.n != 0) {//wysiadaja wszyscy ze statku
                            while (SlotMost.availablePermits() > 0 && Main.n!=0) {//pasazerowie wchodza, zapelniajac most
                                try {
                                    SlotMost.acquire();
                                } catch (InterruptedException e) {
                                    break;
                                }
                                Main.k++;
                                Main.n--;
                            }
                            int z=Main.k;
                            System.out.println("wyjscie na most");
                            System.out.println("Statek: " + Main.n + "/" + Main.N + ", most: " + Main.k + "/" + Main.K);
                            display.acquire();
                            Platform.runLater(()->{Main.man.Wysiadanie_statek_most(z);});//animacja

                            if (Main.k > 0) {//zejscie na lad
                                while (Main.k != 0) {
                                    SlotMost.release();
                                    Main.k--;
                                }
                                System.out.println("wejscie na lad");
                                System.out.println("Statek: " + Main.n + "/" + Main.N + ", most: " + Main.k + "/" + Main.K);
                                display.acquire();
                                Platform.runLater(()->{Main.man.Wysiadanie_most_lad(z);});//animacja
                            }
                            kryt.release();
                        }
                    } else kryt.release();
                }
            }catch(InterruptedException e){
                break;
            }


        }
    }
}
