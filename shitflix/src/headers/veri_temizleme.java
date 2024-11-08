package headers;

import java.io.*;
import java.util.*;


public class veri_temizleme
{
    public static float min_guven = 0.4F;
    public static int total_user = 140000;
    public static int min_izlenme_film = 14000;
    public static int min_izleme_user = 1;

    private static ArrayList<Integer> find_common_Integers(ArrayList<Integer> data1, ArrayList<Integer> data2)
    //finds the same numbers in sorted arrays
    {
        ArrayList<Integer> common_users = new ArrayList<>(Math.min(data1.size(),data2.size()));

        int i=0,j=0;

        while (i < data1.size() && j < data2.size())
        {
            if(data1.get(i) == data2.get(j))
            {
                common_users.add(data1.get(i));

                i++;
                j++;
            }
            else if (data1.get(i) < data2.get(j)){i++;}
            else{j++;}
        }

        return common_users;
    }

    static float num_after_comma(String line, int TH_comma)
    //just use commas as index(for 1. number use 0)
    {
        if(TH_comma<0){return -1.0f;}
        String the_number = "";
        int start_of_number = 0, comma_number =0;

        if(TH_comma != 0)
        {
            for (; start_of_number <= line.length(); start_of_number++)
            {
                if (line.charAt(start_of_number) == ',')
                {
                    comma_number++;
                    if (comma_number >= TH_comma)
                    {
                        start_of_number++;
                        break;
                    }
                }
            }
        }


        if (start_of_number >= line.length())
        {
            throw new IllegalArgumentException("Not enough commas in the input line. Did you forget to reset the reader ?");
        }

        while(line.charAt(start_of_number)!=',')
        {
            the_number += line.charAt(start_of_number);
            start_of_number++;
        }

        return Float.parseFloat(the_number);

    }

    static class rating implements Serializable
    {
        @Serial
        private static final long serialVersionUID = 1L;//I don't know what it does, it recommended when making objects serializable

        int movie_id;
        int rating;

        rating(int movie_id, int rating)
        {
            this.movie_id = movie_id;
            this.rating = rating;
        }

    }

    static public void movie_elimination(String main_path, String[] genres)
    /* main_path is where the cvs files going to read and written, not the to the cvs file itself.*/
    {
        //String[] genres = {"Adventure","Animation","Children","Comedy","Fantasy","Drama","Romance","Crime","Thriller","Action"};
        Set<Integer> deleted_movies = new HashSet<>(22558);//the number is got after testing, can be non-accurate
        try
        {
            BufferedReader movie_reader = new BufferedReader(new FileReader(main_path + "\\movie.csv"));

            ObjectOutputStream movie_writer = new ObjectOutputStream(new FileOutputStream(main_path + "\\movie_0.bin"));

            LinkedHashMap<Integer,ArrayList<String>> new_movies =  new LinkedHashMap<>(23000);
            //the number is got after testing, can be non-accurate. object is LinkedHashMap<movie_id,(name,tag...)>

            movie_reader.readLine();//passing the first line (the line column names set)

            boolean working=true;
            String reading_line;

            while(working)
            {
                reading_line = movie_reader.readLine();
                if(reading_line != null)
                {
                    String[] split_line = reading_line.split(",");//split lines by ","
                    split_line[2] = split_line[2].replace("\"","");//delete " parts from string so ve can filter
                    String[] tags = split_line[2].split("\\|");//split the 3. part of split line by "|"

                    ArrayList<String> movie = new ArrayList<>(11);//experimental number
                    movie.add(split_line[1]);

                    for(String tag : tags)//looking if movie have the tags ı want and deleting extra ones
                    {
                        for(String filter : genres)
                        {
                            if(tag.equals(filter))
                            {
                                movie.add(tag);
                            }
                        }
                    }

                    if(movie.size()>1)
                    {
                        new_movies.put(Integer.parseInt(split_line[0]),movie);
                    }
                    else{deleted_movies.add(Integer.parseInt(split_line[0]));}

                }
                else
                {
                    working =false;

                    movie_writer.writeObject(new_movies);
                    movie_writer.close();

                    movie_reader.close();
                    new_movies.clear();
                }
            }

            working = true;

            BufferedReader rating_reader = new BufferedReader(new FileReader(main_path + "\\rating.csv"));

            ObjectOutputStream rating_writer = new ObjectOutputStream(new FileOutputStream(main_path + "\\rating_0.bin"));

            rating_reader.readLine();//passing the first line (the line column names set)

            int current_user_id =1;

            ArrayList<rating> ratings = new ArrayList<>(145);//starts with initial size of 145

            LinkedHashMap<Integer,ArrayList<rating>> new_ratings = new LinkedHashMap<>(150000);
            // number is experimental. object is  LinkedHashMap<user_id,((movie_id-rating(base 100))...)>

            while(working)
            {
                reading_line = rating_reader.readLine();
                if(reading_line != null)
                {
                    if(deleted_movies.contains((int) num_after_comma(reading_line,1))){continue;}

                    int temp_user_id = (int) num_after_comma(reading_line,0);
                    if(temp_user_id != current_user_id)
                    {
                        if(ratings.size() < min_izleme_user)
                        {
                            current_user_id = temp_user_id;
                            ratings.clear();
                            continue;
                        }

                        new_ratings.put(current_user_id,(ArrayList<rating>)ratings.clone());

                        System.out.println("finished gathering the ratings of " + current_user_id);

                        current_user_id = temp_user_id;
                        ratings.clear();
                    }

                    ratings.add(new rating((int) num_after_comma(reading_line,1),//movie_id
                            (int)(20*num_after_comma(reading_line,2))));//rating(base 100)
                }
                else
                {
                    long start_time =  System.currentTimeMillis();
                    System.out.println("Started to write");
                    rating_writer.writeObject(new_ratings);
                    System.out.println("Finished the writing in " + (System.currentTimeMillis() - start_time) +" milliseconds!");

                    working =false;
                    rating_writer.close();
                    rating_reader.close();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static class movie_obj implements Serializable
    {
        @Serial
        private static final long serialVersionUID = 1L;//I don't know what it does, it recommended when making objects serializable

        ArrayList<Integer> movie_ids;
        int views = 0;
        int average_rating = 0;
        ArrayList<Integer> users;

        movie_obj(ArrayList<Integer> movie_ids)
        {
            this.movie_ids = (ArrayList<Integer>) movie_ids.clone();//for safety
            this.users = new ArrayList<>(10000);//experimental number
        }

        void setUsers(ArrayList<Integer> users)
        {
            this.users = (ArrayList<Integer>) users.clone();
        }

        void calcViews()
        {
            this.views = users.size();
        }

        void rating_hesapla()
        {
            average_rating = average_rating/users.size();
        }
        void rating_add(float x){average_rating += (int)(x*20);}
    }

    static public int temizle(String main_path,int combination)
    /* main_path is where the cvs files going to read and written, not the to the cvs file itself.
    * combination is the number of how many items is joined, it needs the previous step to be completed
    * (if you want to do 3, you should be already done with step 2) also combination 1 needs to movie elimination to completed*/
    {
        try( ObjectInputStream reader = new  ObjectInputStream(new FileInputStream(main_path + "\\rating"
                + "_" + Integer.toString(combination-1) + ".bin")))
        //for combination 1, a single file containing al the needed data,
        // for higher combinations is a mapping to needed files(Arraylist<Integer> combination_base(for 1-2-3 its 1-2 and 1-2 its 1),path)
        {
            ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(main_path + "\\rating_"
                    + Integer.toString(combination) + ".bin"));

            if(combination<1)
            {
                throw new RuntimeException("combination 0 is deprecated, negative combinations never existed." +
                        " Please start your combination from 1!");
            }

            else if (combination==1)//also finds the average rating of movies
            {

                ObjectInputStream movie_reader = new ObjectInputStream(new FileInputStream(main_path + "\\movie_0.bin"));

                LinkedHashMap<Integer,ArrayList<String>> readed_movies = (LinkedHashMap<Integer,ArrayList<String>>) movie_reader.readObject();

                movie_reader.close();

                LinkedHashMap<ArrayList<Integer>, movie_obj> movies = new LinkedHashMap<>(18000);
                //movies<movie_ids, (movie_ids,views,average_rating,Arraylist<Integer>(user...))>

                for(Map.Entry<Integer,ArrayList<String>> entry : readed_movies.entrySet() )//hashmap initialization
                {
                    ArrayList<Integer> temp = new ArrayList<>(1);
                    temp.add(entry.getKey());
                    movies.put((ArrayList<Integer>)temp.clone(),new movie_obj(temp));
                }

                readed_movies.clear();//this object is no longer needed

                LinkedHashMap<Integer,ArrayList<rating>> readed_ratings = (LinkedHashMap<Integer,ArrayList<rating>>) reader.readObject();

                reader.close();

                for (Map.Entry<Integer,ArrayList<rating>> entry : readed_ratings.entrySet())//filling the arrays
                {
                    for(rating x : entry.getValue())
                    {
                        ArrayList<Integer> temp = new ArrayList<>(1);
                        temp.add(x.movie_id);

                        movie_obj ref_temp = movies.get(temp);
                        ref_temp.views++;
                        ref_temp.rating_add(x.rating);
                        ref_temp.users.add(entry.getKey());
                    }

                }

                readed_ratings.clear();//this object is no longer needed

                Iterator<Map.Entry<ArrayList<Integer>, movie_obj>> iterator = movies.entrySet().iterator();

                while (iterator.hasNext())
                {
                    Map.Entry<ArrayList<Integer>, movie_obj> entry = iterator.next();

                    movie_obj ref_temp = entry.getValue();

                    if(ref_temp.views < min_izlenme_film)
                    {
                       iterator.remove();
                       continue;
                    }

                    ref_temp.rating_hesapla();

                    /*if(ref_temp.average_rating < (min_guven*100))
                    {
                        iterator.remove();
                        continue;
                    }*/

                }

                ObjectOutputStream real_writer = new ObjectOutputStream(new FileOutputStream(main_path + "\\data_base\\c1_0.bin"));
                real_writer.writeObject(movies);
                real_writer.close();

                LinkedHashMap<ArrayList<Integer>,String> writing_map = new LinkedHashMap<>(1);
                ArrayList<Integer> temp = new ArrayList<>(1);
                temp.add(0);
                writing_map.put(temp,main_path + "\\data_base\\c1_0.bin");
                writer.writeObject(writing_map);//all these lines is for compatibility with other combinations

                movies.clear();
                writing_map.clear();
                writer.close();
            }
            else
            {
                long startTime = System.currentTimeMillis();

                LinkedHashMap<Integer,String> big_read_map = (LinkedHashMap<Integer,String>) reader.readObject();

                LinkedHashMap<ArrayList<Integer>,String> big_write_map = new LinkedHashMap<>(100000);

                reader.close();

                System.out.println("read the map rating_" + Integer.toString(combination-1) + ".bin in "+
                        (System.currentTimeMillis()-startTime) + " milliseconds!");


                for(Map.Entry<Integer,String> guide : big_read_map.entrySet())
                {
                    ObjectInputStream real_reader = new ObjectInputStream(new FileInputStream(guide.getValue()));

                    LinkedHashMap<ArrayList<Integer>,movie_obj> combinations = (LinkedHashMap<ArrayList<Integer>,movie_obj>) real_reader.readObject();

                    ArrayList<ArrayList<Integer>> keys = new ArrayList<>(combinations.keySet());

                    for (int i = 0; i < keys.size(); i++)
                    {
                        startTime = System.currentTimeMillis();

                        movie_obj data1 = combinations.get(keys.get(i));

                        LinkedHashMap<ArrayList<Integer>,movie_obj> new_combinations = new LinkedHashMap<>(keys.size());



                        for (int j = i+1; j < keys.size(); j++)
                        {
                            movie_obj data2 = combinations.get(keys.get(j));
                            ArrayList<Integer> movie_combo = new ArrayList<>(combination);

                            for(int movie_id:data1.movie_ids)
                            {
                                movie_combo.add(movie_id);
                            }

                            movie_combo.add(data2.movie_ids.get(combination-2));
                            movie_obj new_data = new movie_obj(movie_combo);


                            new_data.setUsers(find_common_Integers(data1.users,data2.users));
                            new_data.calcViews();

                            if(new_data.views>min_izlenme_film)
                            {
                                new_combinations.put((ArrayList<Integer>) movie_combo.clone(),new_data);
                            }

                            movie_combo.clear();
                        }
                        if(!new_combinations.isEmpty()) {
                            StringBuilder writing_path = new StringBuilder(main_path + "\\data_base\\c" + combination + "_");

                            for (int movie_id : data1.movie_ids) {
                                writing_path.append(movie_id).append("_");
                            }

                            writing_path.setLength(writing_path.length() - 1);//delete the last "_";
                            writing_path.append(".bin");

                            ObjectOutputStream real_writer = new ObjectOutputStream(new FileOutputStream(writing_path.toString()));
                            real_writer.writeObject(new_combinations);
                            real_writer.close();

                            big_write_map.put(data1.movie_ids, writing_path.toString());

                            System.out.print("finished the combinations of: ");
                            for (int k = 0; k < combination-1; k++)
                            {
                                System.out.print(data1.movie_ids.get(k)+", ");
                            }
                            System.out.print(" in " + (System.currentTimeMillis() - startTime) + " milliseconds\n");
                            System.out.println("wrote to: " + writing_path);
                        }
                        else
                        {
                            System.out.print("finished the combinations of: ");
                            for (int k = 0; k < combination-1; k++)
                            {
                                System.out.print(data1.movie_ids.get(k)+", ");
                            }
                            System.out.print(" in " + (System.currentTimeMillis() - startTime) + " milliseconds\n");
                            System.out.println("\n\nit did not have any combinations :,( \n\n");
                        }


                    }
                    real_reader.close();
                }

                writer.writeObject(big_write_map);
            }
        }
        catch (FileNotFoundException e)
        {
            System.out.println("File does not exist: " + e.getMessage() + "Don't forget you should do combination 0 before 1 and 1 before 2 etc.");
            return -2;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    static public void most_popular_x_of_genre(String main_path,String[] genres, int x)
    /* this function needs the temizle function's combination 1 and movie elimination to be finished
    * main_path is where the .bin files going to read and written, not the to the .bin file itself.
    * genres are not used together, it will find top 50 movie separate for each one, so there can be duplicates
    * like if you give {"a","b"} it will give top 50 movies with genre "a" in top_50_a.bin and makes another one for b*/
    {
        try
        {
            long start_time = System.currentTimeMillis();

            ObjectInputStream movie_reader = new ObjectInputStream(new FileInputStream(main_path + "\\movie_0.bin"));
            LinkedHashMap<Integer,ArrayList<String>> movies = (LinkedHashMap<Integer,ArrayList<String>>) movie_reader.readObject();
            movie_reader.close();

            ObjectInputStream temp_reader = new ObjectInputStream(new FileInputStream(main_path + "\\rating_1.bin"));
            ArrayList<Integer> temp_key =new ArrayList<>(1);
            temp_key.add(0);
            String real_path = ((LinkedHashMap<ArrayList<Integer>,String>)temp_reader.readObject()).get(temp_key);
            temp_key.clear();
            temp_reader.close();

            ObjectInputStream rating_reader = new ObjectInputStream(new FileInputStream(real_path));
            LinkedHashMap<ArrayList<Integer>,movie_obj> ratings = (LinkedHashMap<ArrayList<Integer>,movie_obj>) rating_reader.readObject();
            rating_reader.close();

            ArrayList<PriorityQueue<movie_obj>> genre_que = new ArrayList<>(genres.length);
            for(int i=0;i< genres.length;i++)//initialization
            {
                genre_que.add(new PriorityQueue<>(x + 1, Comparator.comparingDouble(r -> r.average_rating * r.views)));
            }

            for(Map.Entry<ArrayList<Integer>,movie_obj> entry : ratings.entrySet())
            {
                ArrayList<String> tags = movies.get(entry.getKey().getFirst());
                tags.removeFirst();//removing movie name
                for(String tag:tags)
                {
                    for (int i = 0; i < genres.length; i++)
                    {
                        if(genres[i].equals(tag))
                        {
                            genre_que.get(i).add(entry.getValue());
                            if(genre_que.get(i).size() > x){genre_que.get(i).poll();}
                            break;
                        }
                    }
                }
            }

            System.out.println("finished initialization for genres in " + (System.currentTimeMillis() - start_time) + "milliseconds!");

            for (int i = 0; i < genres.length; i++)
            {
                start_time = System.currentTimeMillis();

                File directory = new File(main_path + "\\popular\\"+genres[i]+"\\data_base");
                if (!directory.exists())
                {
                    directory.mkdirs(); // Creates the directory and any necessary parent directories
                }

                ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(main_path + "\\popular\\" + genres[i] + "\\ratings_1.bin"));

                LinkedHashMap<ArrayList<Integer>,String> writing_map = new LinkedHashMap<>(1);
                ArrayList<Integer> temp = new ArrayList<>(1);
                temp.add(0);
                writing_map.put(temp,main_path +  "\\popular\\" + genres[i] + "\\data_base\\c1_0.bin");

                writer.writeObject(writing_map);
                writer.close();
                writing_map.clear();

                ArrayList<movie_obj> sorter = new ArrayList<>(x + 1);

                sorter.addAll(genre_que.get(i));
                sorter.sort(Comparator.comparingInt(movie -> movie.movie_ids.getFirst()));

                LinkedHashMap<ArrayList<Integer>, movie_obj> final_object = new LinkedHashMap<>(x+1);

                for(movie_obj final_object_part : sorter)
                {
                    final_object.put(final_object_part.movie_ids,final_object_part);
                }

                ObjectOutputStream real_writer = new ObjectOutputStream(new FileOutputStream(main_path + "\\popular\\" + genres[i] + "\\data_base\\c1_0.bin"));

                real_writer.writeObject(final_object);

                real_writer.close();

                System.out.println("genre: " + genres[i] + " finished in " + (System.currentTimeMillis() - start_time) + " milliseconds");
            }

        }
        catch (FileNotFoundException e)
        {
            System.out.println("File does not exist: " + e.getMessage() + "Don't forget you should do combination 1 before 2 etc.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    class rule implements Serializable
    {
        @Serial
        private static final long serialVersionUID = 1L;//I don't know what it does, it recommended when making objects serializable

        ArrayList<Integer> source;
        ArrayList<Integer> target;

        float guven;
        float lift;

        public rule(ArrayList<Integer> source, ArrayList<Integer> target) {
            this.source = (ArrayList<Integer>) source.clone();
            this.target = (ArrayList<Integer>) target.clone();
        }
    }

    static private class combination_finder
    {
        int key_size;
        private ArrayList<Integer> key_pointers;
        ArrayList<Integer> key_ids;
        ArrayList<Integer> target_ids;
        ArrayList<Integer> all_ids;

        combination_finder(ArrayList<Integer> all_ids,int key_size)
        {
            //this.all_ids = (ArrayList<Integer>) all_ids.clone();//seems un needed but ı am not sure
            this.all_ids = all_ids;
            this.key_pointers = new ArrayList<>(key_size);
            this.key_size=key_size;

            this.key_ids =new ArrayList<>(key_size);
            this.target_ids =new ArrayList<>(all_ids.size()-key_size);

            for (int i = 0; i < key_size; i++)
            {
                this.key_pointers.add(i);
                this.key_ids.add(all_ids.get(i));
            }
            for (int i = key_size; i < all_ids.size(); i++)
            {
                this.target_ids.add(all_ids.get(i));
            }
        }

        boolean next_comb()
        {
            //----------------------fill me!-------------------------------------------------------------------------------------------------
            return true;//new combinations found
            return false;//no new combinations
        }

    }

    static public int kural_bul(String main_path,int combination,int max_rec_target)
    //max rec target is also finds it other counterpart(like for comb5, 1 also fids 4's and 2 also finds 3's) but as long as it under limit it don't write it
    {
        try
        {
            if(combination<2)
            {
                throw new RuntimeException("you can only find rules starting from combination 2!, pls use minimum of 2 for combination");
            }
            else if(max_rec_target<1 || max_rec_target >= combination)
            {
                throw new RuntimeException("max_rec_target can be minimum 1 and maximum combination-1! use 1 for convenience or bigger for more pain");
            }
            else
            {
                ArrayList<LinkedHashMap<ArrayList<Integer>,String>> maps = new ArrayList<>(combination);
                ObjectInputStream map_reader;
                for (int i = 1; i <= combination; i++)
                {
                    map_reader = new ObjectInputStream(new FileInputStream(main_path+"\\rating_" + i + ".bin"));
                    maps.add((LinkedHashMap<ArrayList<Integer>,String>) map_reader.readObject());
                    map_reader.close();
                }

                LinkedHashMap<ArrayList<Integer>,LinkedHashMap<ArrayList<Integer>,movie_obj>> data =
                        new LinkedHashMap<>(combination-1,1,true)
                        {
                            @Override
                            protected boolean removeEldestEntry(Map.Entry<ArrayList<Integer>, LinkedHashMap<ArrayList<Integer>, movie_obj>> eldest)
                            {
                                return size() > 200;//experimental, pls change
                            }
                        };
                //last access ordered object so ı can store the data's ı need without filling my ram


                LinkedHashMap<ArrayList<Integer>,ArrayList<rule>> rules = new LinkedHashMap<>(50000);
                //experimental number, format is <source,target_rules>

                for(Map.Entry<ArrayList<Integer>,String> entry : maps.get(combination-1).entrySet())
                {
                    ObjectInputStream reader = new ObjectInputStream(new FileInputStream(entry.getValue()));
                    LinkedHashMap<ArrayList<Integer>, movie_obj> combinations = (LinkedHashMap<ArrayList<Integer>, movie_obj>)reader.readObject();
                    reader.close();

                    for(Map.Entry<ArrayList<Integer>,movie_obj> the_combination : combinations.entrySet())
                    {
                        ArrayList<Integer> ids = the_combination.getKey();
                        movie_obj big_obj = the_combination.getValue();
                        movie_obj source;
                        movie_obj target;
                        for (int i = 1; i <= combination-max_rec_target; i++)
                        {
                            combination_finder machine = new combination_finder(ids,i);
                            do
                            {
                                //-------------------------fill me!------------------------------------------------------------------------------------
                                //before doing hard work, check if that combination exist in the first place! you need the pull that info anyway
                                //look from data object, if not present add it, doesn't forget you have all the maps needed in maps object
                                //if needed just reverse the target and source values to do the other part of rule(on a 5 comb, if you are finding 2's, you are also finding the 3's)
                            }while (machine.next_comb());

                        }

                    }

                }


            }
            return 0;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

}
