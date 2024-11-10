package headers;
import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class gui
{

    LinkedHashMap<Integer,ArrayList<veri_temizleme.rating>> user_ratings;

    public gui(String main_path, String[] genres)
    {
        JButton do_the_job = new JButton("Film Öner");

        SwingWorker<LinkedHashMap<Integer,ArrayList<veri_temizleme.rating>>,Void> read_user_ratings = new SwingWorker<>()
        {
            long start_time;
            @Override
            protected LinkedHashMap<Integer, ArrayList<veri_temizleme.rating>> doInBackground() throws Exception
            {
                try(ObjectInputStream reader = new ObjectInputStream(new FileInputStream(main_path + "\\rating_0.bin")))
                {
                    start_time = System.currentTimeMillis();
                    System.out.println("start to read ratings!");
                    return (LinkedHashMap<Integer,ArrayList<veri_temizleme.rating>>) reader.readObject();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void done()
            {
                try
                {
                    System.out.println("finished reading ratings in " + (System.currentTimeMillis() - start_time) + " milliseconds!");
                    user_ratings = get();
                    do_the_job.setEnabled(true);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        };

        try
        {
            //altyapı
            JFrame main_frame = new JFrame();
            main_frame.setLayout(null);
            main_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            main_frame.setSize(888,666);
            main_frame.setResizable(false);
            main_frame.setName("Shitflix");
            main_frame.getContentPane().setBackground(Color.BLACK);
            ImageIcon logo = new ImageIcon("shitflix_logo.png");
            main_frame.setIconImage(logo.getImage());

            long start_time = System.currentTimeMillis();

            ObjectInputStream reader = new ObjectInputStream(new FileInputStream(main_path + "\\users.bin"));
            ArrayList<Integer> user_list = (ArrayList<Integer>) reader.readObject();
            reader.close();

            System.out.println("finished reading users in " +(System.currentTimeMillis()-start_time)+ " milliseconds!");
            start_time = System.currentTimeMillis();

            reader = new ObjectInputStream( new FileInputStream(main_path + "\\movie_0.bin"));
            LinkedHashMap<Integer,ArrayList<String>> movie_list = (LinkedHashMap<Integer,ArrayList<String>>) reader.readObject();
            reader.close();

            System.out.println("finished reading movies in " +(System.currentTimeMillis()-start_time)+ " milliseconds!");

            read_user_ratings.execute();

            //kişileştirilmiş-birliktelik seçim paneli
            JPanel panel1 = new JPanel();
            panel1.setLayout(null);
            panel1.setBackground(Color.BLUE);
            panel1.setBounds(0,0,444,444);

            JRadioButton personified = new JRadioButton("Kişiselleştirilmiş Öneriler");
            JRadioButton popular = new JRadioButton("Birliktelik Kurallarına Dayalı Öneriler");

            personified.setOpaque(false);
            popular.setOpaque(false);

            personified.setBounds(44,22,333,22);
            popular.setBounds(44,44,333,22);

            ButtonGroup first_choice = new ButtonGroup();
            first_choice.add(personified);
            first_choice.add(popular);

            panel1.add(personified);
            panel1.add(popular);

            JComboBox<Integer> select_user = new JComboBox<>();
            for(Integer entry : user_list)
            {
                select_user.addItem(entry);
            }
            select_user.setBounds(44,88,333,22);
            panel1.add(select_user);
            select_user.setVisible(false);

            personified.addActionListener(a ->
            {
                select_user.setVisible(true);
                if(user_ratings == null)
                {
                    do_the_job.setEnabled(false);
                }
            });
            popular.addActionListener((a->
            {
                select_user.setVisible(false);
                do_the_job.setEnabled(true);
            }));

            main_frame.add(panel1);



            //türegöre-ismegöre seçim paneli
            JPanel panel2 = new JPanel();
            panel2.setLayout(null);
            panel2.setBackground(Color.RED);
            panel2.setBounds(444,0,444,444);

            JRadioButton for_genre = new JRadioButton("Film Türüne Göre Öneriler");
            JRadioButton for_name = new JRadioButton(" Film İsmine Göre Öneriler");

            for_genre.setOpaque(false);
            for_name.setOpaque(false);

            for_genre.setBounds(44,22,333,22);
            for_name.setBounds(44,44,333,22);


            ButtonGroup second_choice = new ButtonGroup();
            second_choice.add(for_genre);
            second_choice.add(for_name);

            panel2.add(for_genre);
            panel2.add(for_name);

            JComboBox<String> select_genre = new JComboBox<>(genres);
            select_genre.setBounds(44,88,333,22);
            select_genre.setEditable(false);
            panel2.add(select_genre);
            select_genre.setVisible(false);

            JComboBox<ArrayList<String>> select_movie = new JComboBox<>();
            for(Map.Entry<Integer,ArrayList<String>> entry : movie_list.entrySet())
            {
                entry.getValue().add(entry.getKey().toString());
                select_movie.addItem(entry.getValue());
            }
            select_movie.setBounds(44,88,333,22);
            select_movie.setEditable(false);
            panel2.add(select_movie);
            select_movie.setVisible(false);

            for_name.addActionListener(a ->
            {
                select_movie.setVisible(true);
                select_genre.setVisible(false);
            });
            for_genre.addActionListener(a ->
            {
                select_genre.setVisible(true);
                select_movie.setVisible(false);
            });

            main_frame.add(panel2);


            //çıktı paneli
            JPanel panel3 = new JPanel();
            //panel3.setLayout(null);
            panel3.setBackground(Color.GREEN);
            panel3.setBounds(0,444,888,222);

            JComboBox<String> movies = new JComboBox<>();
            movies.setVisible(false);
            panel3.add(movies);

            do_the_job.setHorizontalAlignment(JButton.CENTER);
            do_the_job.setVerticalAlignment(JButton.TOP);

            panel3.add(do_the_job);

            do_the_job.addActionListener(a->
            {
                movies.removeAllItems();
                LinkedHashSet<Integer> temp;
                if(popular.isSelected() && for_genre.isSelected())
                {
                    temp = recommend.B_T(main_path + "\\popular\\" + select_genre.getSelectedItem() + "\\rating_1.bin");
                    for(Integer x: temp)
                    {
                        movies.addItem(movie_list.get(x).getFirst());
                    }
                    movies.setVisible(true);
                }
                else if(popular.isSelected() && for_name.isSelected())
                {
                    int temp2 = Integer.parseInt(((ArrayList<String>) select_movie.getSelectedItem()).getLast());
                    temp=recommend.B_I(main_path,temp2,genres);
                    for(Integer x: temp)
                    {
                        movies.addItem(movie_list.get(x).getFirst());
                    }
                    movies.setVisible(true);
                }
                else if(personified.isSelected() && for_genre.isSelected())
                {
                    ArrayList<veri_temizleme.rating> ratings = user_ratings.get((Integer) select_user.getSelectedItem());
                    temp=recommend.K_T(main_path,ratings,(String) select_genre.getSelectedItem());
                    for(Integer x: temp)
                    {
                        movies.addItem(movie_list.get(x).getFirst());
                    }
                    movies.setVisible(true);
                }
                else
                {
                    int temp2 = Integer.parseInt(((ArrayList<String>)select_movie.getSelectedItem()).getLast());
                    ArrayList<veri_temizleme.rating> ratings = user_ratings.get((Integer) select_user.getSelectedItem());
                    temp=recommend.K_I(main_path,ratings,temp2,7);
                    for(Integer x: temp)
                    {
                        movies.addItem(movie_list.get(x).getFirst());
                    }
                    movies.setVisible(true);
                }
            });

            main_frame.add(panel3);



            main_frame.setVisible(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
