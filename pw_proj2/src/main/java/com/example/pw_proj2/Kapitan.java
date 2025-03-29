package com.example.pw_proj2;

import javafx.application.Platform;

import java.util.concurrent.Semaphore;

public class Kapitan extends Thread {

    Semaphore WejscieMost; //zezwol na wejscie na most
    Semaphore SlotMost;//miejsca na moscie
    Semaphore kryt; //semafor do ochrony sek. krytycznych
    Semaphore spr;
    Semaphore display;
    public Kapitan(Semaphore s1, Semaphore s2, Semaphore s3, Semaphore s4,Semaphore s5) {
        this.WejscieMost = s1;
        this.SlotMost = s2;
        this.kryt = s3;
        this.spr = s4;
        this.display = s5;
    }

    @Override
    public void run() {
        while (true) {

            try {//jesli konczy sie miejsce na statku zablokuj miejsca na moscie, by wpuscic ostatnie N-n osob
                spr.acquire();//weszłą grupa
                kryt.acquire();

                if(SlotMost.availablePermits()==Main.K){//jesli most pusty
                    if (Main.N - Main.n < Main.K ) {//wpusc na most tylko N-n, gdy poprzedni wejda
                        int blokady = Main.K - (Main.N - Main.n);
                        SlotMost.acquire(Main.K - (Main.N - Main.n));
                        System.out.println("zablokowano czesc mostu");
                        System.out.println("Statek: " + Main.n + "/" + Main.N + ", most: " + Main.k + "/" + Main.K);
                        display.acquire();
                        Platform.runLater(()->{Main.man.czesciowaBlokada(blokady);});//animacja
                    }

                    if(WejscieMost.availablePermits()==0)WejscieMost.release();//odblokuj most po sprawdzeniu liczby
                    //System.out.println("sprawdzenie "+WejscieMost.availablePermits());
                    display.acquire();
                    Platform.runLater(()->{Main.man.zdejmijBlokadeMostu("Kapitan ustalił pojemnosc");});//animacja
                }

                //System.out.println(WejscieMost.availablePermits());
                kryt.release();
            } catch (InterruptedException e) {
                break;
            }

            try {//zablokuj most na czas rejsu (wysiadaje gdzies indziej)
                kryt.acquire();
                if (Main.n == Main.N) {

                    WejscieMost.tryAcquire();
                    //System.out.println("a");
                    while (Main.k > 0) {//czekaj na ostatnich pasazerow
                        sleep(2);
                    }
                    System.out.println("rejs sie rozpoczal");
                    System.out.println("Statek: " + Main.n + "/" + Main.N + ", most: " + Main.k + "/" + Main.K);
                    display.acquire();
                    Platform.runLater(()->{Main.man.rejs();});//animacja
                    kryt.release();
                    sleep(500); //rejs trwa
                    kryt.acquire();
                    if (!Main.czyWysiadaja) {//pasazerowie wysiadaja gdzies indziej, statek wraca pusty
                        Main.n = 0;
                        WejscieMost.release();//statek wrocił, otwarcie mostu
                        while (SlotMost.availablePermits() != Main.K) {
                            SlotMost.release();
                        }
                        System.out.println("statek wrocil");
                        System.out.println("-----------");
                        System.out.println("Statek: " + Main.n + "/" + Main.N + ", most: " + Main.k + "/" + Main.K);
                        display.acquire();
                        Platform.runLater(()->{Main.man.koniecRejsuFalse();});//animacja

                    }else if (Main.czyWysiadaja){//pasazerowie wysiadaja tym samym mostem
                        //otworz wyjscie i czekaj az pasazerowie wysiada
                        // (wysiadajacy nie czekaja na wejscieMost, czekaja tylko na Sloty)
                        Main.czyPoRejsie = true;

                        while (SlotMost.availablePermits() != Main.K) {//zwolnij blokady na moscie
                            SlotMost.release();
                        }

                        System.out.println("statek wrocil");
                        System.out.println("-----------");
                        System.out.println("Statek: " + Main.n + "/" + Main.N + ", most: " + Main.k + "/" + Main.K);
                        display.acquire();
                        Platform.runLater(()->{Main.man.koniecRejsuTrue();});//animacja
                        kryt.release();
                        Main.wrocil.release();
                        while(Main.n!=0 || Main.k!=0){//po otwarciu mostu w 1 strone busy-wait az wszyscy wysiada
                            sleep(1);
                        }

                        kryt.acquire();
                        display.acquire();
                        Platform.runLater(()->{Main.man.zdejmijBlokadeMostu("koniec rejsu");});//animacja
                        Main.czyPoRejsie = false;

                        WejscieMost.release();//gdy wszyscy wysiedli otworz most
                    }

                }
                kryt.release();
            } catch (InterruptedException e) {
                break;
            }

        }
    }
}
