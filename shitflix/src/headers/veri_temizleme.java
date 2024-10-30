package headers;

import java.io.*;
import java.util.*;


public class veri_temizleme
{
    public static float min_destek = 0.4F;
    public static int min_izlenme_film = 1000;
    public static int min_izleme_user = 1;


    static float num_after_comma(String line, int TH_comma)
    //just use commas as index(for 1. number use 0)
    {
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

    static float[] num_after_comma(String line, int start_comma, int end_comma)
    //just use commas as index(for 1. number use 0,1 or for 4th,5th and 6th numbers use 3,6)
    //if you want to get until end(like from 1 to end of string) use -1(like 0,-1)
    {
        try {
            String[] split_line = line.split(",");
            if(end_comma <=-1)
            {
                end_comma = split_line.length;
            }
            float[] the_numbers = new float[end_comma-start_comma];
            for (int i = start_comma; i < end_comma; i++)
            {
                the_numbers[i-start_comma] = Float.parseFloat(split_line[i]);
            }
            return the_numbers;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new float[]{};
        }
    }

    static public int chunk_size = 200000;//IMPORTANT: DO NOT CHANGE BEFORE READİNG, ONLY CHANGE BEFORE MAKING. chunk size for making and reading maps
    static void make_map(String main_path, String map_path, int lines_to_jump)//lines_to_jump is the  number of declaration lines
    //main_path is a csv file that you want mapped(so it can be used with find_by_id)
    //map_path is where map goes, you should give an exact path(one that end with a xxx_map.bin), it will crate or overwrite that file
    {
        HashMap<Integer,Long> reading_map = new HashMap<Integer,Long>(chunk_size+1);

        try(RandomAccessFile reader = new RandomAccessFile(main_path,"r"))
        {
            File directory = new File(map_path).getParentFile();
            if (!directory.exists())
            {
                directory.mkdirs(); // Creates the directory and any necessary parent directories
            }

            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(map_path));

            String reading_line;
            boolean working=true;
            long start_point;
            int last_id=-1;

            for (int i = 0; i < lines_to_jump; i++) {
                reader.readLine();//passing type declaration at the start(like "id","movie_name","genres")
            }

            while (working)
            {
                start_point = reader.getFilePointer();
                reading_line = reader.readLine();
                if(reading_line != null)
                {
                    int temp_id = (int)num_after_comma(reading_line,0);
                    if(temp_id!=last_id)
                    {
                        reading_map.put(temp_id,start_point);
                        last_id = temp_id;
                        //System.out.println("I like seeing data on console so i printed the last id i finished: "+temp_id);
                        if(reading_map.size() >= chunk_size)
                        {
                            outputStream.writeObject(reading_map);
                            System.out.println("wrote a chunk with size: " + reading_map.size());
                            reading_map.clear();
                        }
                    }
                }
                else
                {
                    reader.close();
                    working = false;
                }
            }

            outputStream.writeObject(reading_map);
            outputStream.close();
            System.out.println("Finished mapping: " + map_path);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    static HashMap<Integer,Long> read_map(String map_path,int must_key)
    //reads a map crated by make_map function, returns the map  chunk containing have must_key,
    //where each id's line start position on file that mapped, if cant find it, throws RuntimeException and returns a null
    {
        try(ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(map_path)))
        {
            while (true)
            {
                HashMap<Integer, Long> outcome = (HashMap<Integer, Long>) inputStream.readObject();

                if(outcome.containsKey(must_key))
                {
                    inputStream.close();
                    return outcome;
                }
            }

        }
        catch (IOException e)
        {
            // End of file or stream issue
            throw new RuntimeException("Couldn't find the " + must_key + " in the map: " + map_path);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    static public void movie_elimination(String main_path, String[] genres)
    /* main_path is where the cvs files going to read and written, not the to the cvs file itself.*/
    {
        //String[] genres = {"Adventure","Animation","Children","Comedy","Fantasy","Drama","Romance","Crime","Thriller","Action"};
        Set<Integer> deleted_movies = new HashSet<>(22558);//the number is got after testing, can be not accurate
        try
        {
            BufferedReader movie_reader = new BufferedReader(new FileReader(main_path + "\\movie.csv"));

            BufferedWriter movie_writer = new BufferedWriter(new FileWriter(main_path + "\\movie_0.csv"));

            movie_reader.readLine();//passing the first line (the line column names set)
            movie_writer.write("\"movieId\",\"title\",\"genre...\"\n");
            boolean working=true;
            String reading_line;
            StringBuilder writing_line= new StringBuilder();

            while(working)
            {
                reading_line = movie_reader.readLine();
                if(reading_line != null)
                {
                    String[] split_line = reading_line.split(",");//split lines by ","
                    split_line[2] = split_line[2].replace("\"","");//delete " parts from string so ve can filter
                    String[] tags = split_line[2].split("\\|");//split the 3. part of split line by "|"
                    StringBuilder filtered_tags = new StringBuilder();

                    for(String tag : tags)//looking if movie have the tags ı want and deleting extra ones
                    {
                        for(String filter : genres)
                        {
                            if(tag.equals(filter))
                            {
                                filtered_tags.append((filtered_tags.isEmpty()) ? "\"" + tag : "|" + tag);
                            }
                        }
                    }

                    if(!filtered_tags.isEmpty())
                    {
                        writing_line.append(split_line[0]).append(",")
                                    .append(split_line[1]).append(",")
                                    .append(filtered_tags).append("\"").append("\n");

                        movie_writer.write(writing_line.toString());
                        writing_line.setLength(0);
                    }
                    else{deleted_movies.add(Integer.parseInt(split_line[0]));}

                }
                else
                {
                    working =false;
                    movie_writer.close();
                    movie_reader.close();
                }
            }

            make_map(main_path + "\\movie_0.csv",main_path + "\\maps\\movie_0_map.bin", 1);

            working = true;

            BufferedReader rating_reader = new BufferedReader(new FileReader(main_path + "\\rating.csv"));

            BufferedWriter rating_writer = new BufferedWriter(new FileWriter(main_path + "\\rating_-1.csv"));

            rating_reader.readLine();//passing the first line (the line column names set)
            rating_writer.write("\"userId\",\"movieId\",\"rating\",\"timestamp\"\n");

            while(working)
            {
                reading_line = rating_reader.readLine();

                if(reading_line != null) {
                    if (!deleted_movies.contains((int) num_after_comma(reading_line, 1))) {
                        rating_writer.write(reading_line+"\n");
                    }
                }
                else
                {
                    rating_reader.close();
                    rating_writer.close();
                    working=false;
                }
            }

            make_map(main_path + "\\rating_-1.csv",main_path + "\\maps\\rating_-1_map.bin", 1);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static public int temizle(String main_path,int combination)
    /* main_path is where the cvs files going to read and written, not the to the cvs file itself.
    * combination is the number of how many items is joined, it needs the previous step to be completed
    * (if you want to do 3, you should be already done with step 2)*/
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(main_path + "\\rating"
                    + "_" + Integer.toString(combination-1) + ".csv"));

            BufferedWriter writer = new BufferedWriter(new FileWriter(main_path + "\\rating_"
                    + Integer.toString(combination) + ".csv"));

            boolean working=true;
            String reading_line;
            StringBuilder writing_line= new StringBuilder();

            if(combination == 0)
            {
                int current_user_id =1;
                ArrayList<Integer> movie_ids = new ArrayList<>(145);//starts with initial size of 145
                ArrayList<Float> movie_scores = new ArrayList<>(145);//ı get this number from ratings line count % the biggest user_id

                reader.readLine();//passing the first line (the line column names set)

                writer.write("\"user_id\",\"movie_id...\"\n" +
                                "\"user_id\",\"rating...\"\n");

                while(working)
                {
                    reading_line = reader.readLine();
                    if(reading_line != null)
                    {
                        int temp_user_id = (int) num_after_comma(reading_line,0);
                        if(temp_user_id != current_user_id)
                        {
                            if(movie_ids.size() < min_izleme_user)
                            {
                                current_user_id = temp_user_id;
                                movie_ids.clear();
                                movie_scores.clear();
                                continue;
                            }
                            writing_line.append(current_user_id);
                            for (Integer movie_id : movie_ids)
                            {
                                writing_line.append(",").append(movie_id);
                            }
                            writing_line.append("\n");
                            writer.write(writing_line.toString());

                            writing_line.setLength(0);

                            writing_line.append(current_user_id);
                            for(Float movie_score : movie_scores)
                            {
                                writing_line.append(",").append(movie_score);
                            }
                            writing_line.append("\n");
                            writer.write(writing_line.toString());


                            writing_line.setLength(0);

                            current_user_id = temp_user_id;
                            movie_ids.clear();
                            movie_scores.clear();
                        }
                        movie_ids.add((int) num_after_comma(reading_line,1));
                        movie_scores.add((float) num_after_comma(reading_line,2));
                    }
                    else
                    {
                        writing_line.append(current_user_id);
                        for (Integer movie_id : movie_ids)
                        {
                            writing_line.append(",").append(movie_id);
                        }
                        writing_line.append("\n");
                        writer.write(writing_line.toString());

                        writing_line.setLength(0);

                        writing_line.append(current_user_id);
                        for(Float movie_score : movie_scores)
                        {
                            writing_line.append(",").append(movie_score);
                        }
                        writing_line.append("\n");
                        writer.write(writing_line.toString());

                        working =false;
                        writer.close();
                        reader.close();
                    }
                }
            }
            else if (combination==1)//also finds the average rating of movies
            {

                BufferedReader movie_reader = new BufferedReader(new FileReader(main_path + "\\movie_0.csv"));

                movie_reader.readLine();//passing the first line (the line column names set)
                LinkedHashMap<Integer, ArrayList<Integer>> movies = new LinkedHashMap<>(18000);
                //movies<movie_id, "watch_amount,rating(base 100),user_ids...">

                while (working)//hashmap initialization
                {
                    reading_line = movie_reader.readLine();
                    if(reading_line != null)
                    {
                        int movie_id = (int) num_after_comma(reading_line, 0);
                        movies.putIfAbsent(movie_id, new ArrayList<Integer>(830));//experimental number, can be changed
                        movies.get(movie_id).add(0);//initialize amount watched, so I can increase it in a loop
                        movies.get(movie_id).add(0);//initialize total ratings, so I can increase it in a loop
                    }
                    else
                    {
                        working=false;
                        movie_reader.close();
                    }
                }

                working=true;
                reader.readLine();//passing the first line (the line column names set)
                reader.readLine();//passing the second line (the line column names set)


                while (working)//filling the arrays
                {
                    reading_line = reader.readLine();//read the line where "user_id","movie_id..."
                    if(reading_line != null)
                    {
                        int user_id = (int)num_after_comma(reading_line,0);
                        float[] movie_ids = num_after_comma(reading_line,1,-1);
                        //it's actually int[] but to be able to read ratings(witch are float values) function returns a float[]
                        reading_line = reader.readLine();//reading the line where "user_id","rating..."
                        float[] movie_ratings = num_after_comma(reading_line,1,-1);
                        if(movie_ids.length != movie_ratings.length)
                        {
                            throw new RuntimeException("the user with id " + user_id + "doesn't have same amount of ratings and movies!!\n" +
                                    "Maybe forget the rate the movie, or rated a movie he didn't watch, in each case, i don't have solution :,(");
                        }
                        for(int i = 0; i<movie_ids.length;i++)
                        {
                            ArrayList<Integer> watchers = movies.get((int)movie_ids[i]);
                            watchers.set(0,watchers.getFirst()+1);
                            watchers.set(1,watchers.get(1) + (int)(movie_ratings[i]*20));//total rating, going to make it average after all of it finished
                            watchers.add(user_id);
                        }
                    }
                    else
                    {
                        working = false;
                        reader.close();
                    }
                }

                writer.write("\"movie_id\",\"views\",\"average_rating(base 100)\",\"user...\"\n");

                for(Map.Entry<Integer, ArrayList<Integer>> entry : movies.entrySet())
                {
                    if(entry.getValue().getFirst() < min_izlenme_film){continue;}

                    ArrayList<Integer> temp = entry.getValue();
                    temp.set(1,temp.get(1)/(temp.size()-2));

                    //if(temp.get(1) < (min_destek*100)) {continue;}

                    writing_line.append(entry.getKey());
                    for(int x:temp)
                    {
                        writing_line.append(",").append(x);
                    }
                    writing_line.append("\n");
                    writer.write(writing_line.toString());
                    writing_line.setLength(0);
                }
                writer.close();
            }
            else
            {
                //------------------------------------Fill me!!!!!!!!!!!----------------------------------
            }

            make_map(main_path + "\\rating_" + Integer.toString(combination) + ".csv",
                    main_path + "\\maps\\rating_" + Integer.toString(combination) + "_map.bin", combination!=0 ? 1:2);

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

    static class movie_obj
    {
        int movie_id;
        int views;
        int average_rating;
        String as_line;

        movie_obj(int movie_id, int views, int average_rating, String as_line)
        {
            this.movie_id = movie_id;
            this.views = views;
            this.average_rating = average_rating;
            this.as_line = as_line;
        }

        @Override
        public String toString() {
            return as_line;
        }
    }

    static public void most_popular_x_of_genre(String main_path,String[] genres, int x)
    /* this function needs the temizle function's combination 1 and movie elimination to be finished
    * main_path is where the cvs files going to read and written, not the to the cvs file itself.
    * genres are not used together, it will find top 50 movie separate for each one, so there can be duplicates
    * like if you give {"a","b"} it will give top 50 movies with genre "a" in top_50_a.csv and makes another one for b*/
    {
        try(RandomAccessFile movie_reader = new RandomAccessFile(main_path + "\\movie_0.csv","r"))
        {
            BufferedReader rating_reader = new BufferedReader(new FileReader(main_path + "\\rating_1.csv"));

            boolean working=true;
            String reading_line;

            ArrayList<PriorityQueue<movie_obj>> genre_que = new ArrayList<PriorityQueue<movie_obj>>(genres.length);
            for(int i=0;i< genres.length;i++)//initialization
            {
                genre_que.add(new PriorityQueue<movie_obj>(x+1,Comparator.comparingDouble(r -> r.average_rating*r.views)));
            }

            HashMap<Integer,Long> map = new HashMap<>();
            rating_reader.readLine();//passing the first line (the line column names set("movie_id","views","average_rating(base 100)","user..."))

            while (working)
            {
                reading_line = rating_reader.readLine();
                if(reading_line != null)
                {
                    movie_obj temp_obj = new movie_obj((int)num_after_comma(reading_line,0),
                            (int)num_after_comma(reading_line,1), (int)num_after_comma(reading_line,2),reading_line);
                    if(!map.containsKey(temp_obj.movie_id))
                    {
                        map = read_map(main_path + "\\maps\\movie_0_map.bin",temp_obj.movie_id);
                    }
                    movie_reader.seek(map.get(temp_obj.movie_id));
                    reading_line = movie_reader.readLine();
                    String[] tags = reading_line.split(",")[2].replace("\"","").split("\\|");

                    for(String tag : tags)
                    {
                        int i = 0;
                        for (; i < genres.length; i++)
                        {
                            if(genres[i].equals(tag))
                            {
                                genre_que.get(i).add(temp_obj);
                                if(genre_que.get(i).size() > x){genre_que.get(i).poll();}
                                break;
                            }
                        }
                    }
                }
                else
                {
                    rating_reader.close();
                    working = false;
                }
            }


            for (int i = 0; i < genres.length; i++)
            {
                File directory = new File(main_path + "\\popular\\"+genres[i]);
                if (!directory.exists())
                {
                    directory.mkdirs(); // Creates the directory and any necessary parent directories
                }

                BufferedWriter writer = new BufferedWriter(new FileWriter(main_path + "\\popular\\" + genres[i] + "\\ratings_1.csv"));
                writer.write("\"movie_id\",\"views\",\"average_rating(base 100)\",\"user...\"\n");

                ArrayList<movie_obj> sorter = new ArrayList<movie_obj>(x+1);

                sorter.addAll(genre_que.get(i));
                sorter.sort(Comparator.comparingInt(movie -> movie.movie_id));

                for (movie_obj movie : sorter){writer.write(movie.toString() + "\n");}

                writer.close();
                make_map(main_path + "\\popular\\" + genres[i] + "\\ratings_1.csv",
                        main_path + "\\popular\\" + genres[i] + "\\maps\\ratings_1_map.bin",1);
            }

        }
        catch (FileNotFoundException e)
        {
            System.out.println("File does not exist: " + e.getMessage() + "Don't forget you should do combination 0 before 1 and 1 before 2 etc.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
