import headers.veri_temizleme;
import headers.gui;
public class Main
{
    public static void main(String[] args)
    {
        String[] genres = {"Adventure","Animation","Children","Comedy","Fantasy","Drama","Romance","Crime","Thriller","Action"};
        String path = "C:\\Users\\altay\\Desktop\\projeler\\java(film_oneri)-proje\\20M Veri";
        //veri_temizleme.movie_elimination(path,genres);
        //System.out.println("movie elimination bitti");
        //System.out.println("comb 1 finished: "+veri_temizleme.temizle(path,1));
        //veri_temizleme.most_popular_x_of_genre(path,genres,50);
        //System.out.println("most popular x of genre bitti");

        /*for(String tag: genres)
        {
            veri_temizleme.temizle(path+"\\popular\\"+tag,2);
            veri_temizleme.kural_bul(path+"\\popular\\"+tag,2,1);
        }*/

        //System.out.println("comb 2 finished: "+veri_temizleme.temizle(path,2));
        //System.out.println("comb 3 finished: "+veri_temizleme.temizle(path,3));
        //System.out.println("comb 4 finished: "+veri_temizleme.temizle(path,4));
        //System.out.println("comb 5 finished: "+veri_temizleme.temizle(path,5));
        //System.out.println("comb 6 finished: "+veri_temizleme.temizle(path,6));
        //System.out.println("comb 7 finished: "+veri_temizleme.temizle(path,7));
        //veri_temizleme.kural_bul(path,2,1);
        //veri_temizleme.kural_bul(path,3,2);
        //veri_temizleme.kural_bul(path,4,3);
        //veri_temizleme.kural_bul(path,5,4);
        //veri_temizleme.kural_bul(path,6,5);
        //veri_temizleme.kural_bul(path,7,6);

        new gui(path,genres);
    }
}