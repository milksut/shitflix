import headers.veri_temizleme;
public class Main
{
    public static void main(String[] args)
    {
        String[] genres = {"Adventure","Animation","Children","Comedy","Fantasy","Drama","Romance","Crime","Thriller","Action"};
        //veri_temizleme.movie_elimination("C:\\Users\\altay\\Desktop\\projeler\\java(film_oneri)-proje\\20M Veri",genres);
        //System.out.println(veri_temizleme.temizle("C:\\Users\\altay\\Desktop\\projeler\\java(film_oneri)-proje\\20M Veri",0));
        //System.out.println(veri_temizleme.temizle("C:\\Users\\altay\\Desktop\\projeler\\java(film_oneri)-proje\\20M Veri",1));
        veri_temizleme.most_popular_x_of_genre("C:\\Users\\altay\\Desktop\\projeler\\java(film_oneri)-proje\\20M Veri",genres,50);
    }
}