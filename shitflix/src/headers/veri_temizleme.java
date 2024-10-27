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
        try {
            String the_number = line.split(",")[TH_comma];
            return Float.parseFloat(the_number);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }

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
    static public void movie_elimination(String main_path)
    /* main_path is where the cvs files going to read and written, not the to the cvs file itself.*/
    {
        String[] genres = {"Adventure","Animation","Children","Comedy","Fantasy","Drama","Romance","Crime","Thriller","Action"};
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
                if(reading_line!=null)
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

            working = true;

            BufferedReader rating_reader = new BufferedReader(new FileReader(main_path + "\\rating.csv"));

            BufferedWriter rating_writer = new BufferedWriter(new FileWriter(main_path + "\\rating_-1.csv"));

            rating_reader.readLine();//passing the first line (the line column names set)
            rating_writer.write("\"userId\",\"movieId\",\"rating\",\"timestamp\"\n");

            while(working)
            {
                reading_line = rating_reader.readLine();

                if(reading_line !=null) {
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
                    if(reading_line!=null)
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
            else if (combination==1)
            {

                BufferedReader movie_reader = new BufferedReader(new FileReader(main_path + "\\movie_0.csv"));

                movie_reader.readLine();//passing the first line (the line column names set)
                LinkedHashMap<Integer, ArrayList<Integer>> movies = new LinkedHashMap<>(18000);//movies<movie_id, "watch_amount,user_ids...">

                while (working)//hashmap initialization
                {
                    reading_line = movie_reader.readLine();
                    if(reading_line!=null)
                    {
                        int movie_id = (int) num_after_comma(reading_line, 0);
                        movies.putIfAbsent(movie_id, new ArrayList<Integer>(830));//experimental number, can be changed
                        movies.get(movie_id).add(0);//initialize amount watched, so I can increase it in a loop
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
                    if(reading_line!=null)
                    {
                        int user_id = (int)num_after_comma(reading_line,0);
                        float[] movie_ids = num_after_comma(reading_line,1,-1);
                        //it's actually int[] but to be able to read ratings(witch are float values) function returns a float[]
                        for(float x: movie_ids)
                        {
                            ArrayList<Integer> watchers = movies.get((int)x);
                            watchers.set(0,watchers.getFirst()+1);
                            watchers.add(user_id);
                        }
                    }
                    else
                    {
                        working = false;
                        reader.close();
                        break;
                    }
                    reader.readLine();//pass the line where "user_id","rating..."
                }

                writer.write("\"movie_id\",\"views\",\"user...\"");

                for(Map.Entry<Integer, ArrayList<Integer>> entry : movies.entrySet())
                {
                    if(entry.getValue().getFirst() <= min_izlenme_film){continue;}
                    writing_line.append(entry.getKey());
                    for(int x:entry.getValue())
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
}
