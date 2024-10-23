package headers;

import java.io.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class veri_temizleme
{
    static int start_of_number = 0;
    static int comma_number = 0;
    static void reset_num_after_comma(){start_of_number =0;comma_number=0;}
    static float first_num_after_comma(String line, int TH_comma)
    //---------------------------------------change to use line.split() !!!--------------------------------------------
    {
        String the_number = "";
        if(TH_comma == 0){reset_num_after_comma();}
        else if(TH_comma<=comma_number){
            reset_num_after_comma();}
        else
        {
            for (; start_of_number <= line.length(); start_of_number++) {
                if (line.charAt(start_of_number) == ',') {
                    comma_number++;
                    if (comma_number >= TH_comma) {
                        start_of_number++;
                        break;
                    }
                }
            }
            if (start_of_number >= line.length()) {
                throw new IllegalArgumentException("Not enough commas in the input line. Did you forget to reset the reader ?");
            }
        }
        while(line.charAt(start_of_number)!=',')
        {
            the_number += line.charAt(start_of_number);
            start_of_number++;
        }
        start_of_number--;
        return Float.parseFloat(the_number);
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

            movie_reader.readLine();
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

            rating_reader.readLine();

            while(working)
            {
                reading_line = rating_reader.readLine();

                if(reading_line !=null) {
                    if (!deleted_movies.contains((int) first_num_after_comma(reading_line, 1))) {
                        rating_writer.write(reading_line+"\n");
                    }
                    reset_num_after_comma();
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

                while(working)
                {
                    reading_line = reader.readLine();
                    reset_num_after_comma();
                    if(reading_line!=null)
                    {
                        int temp_user_id = (int)first_num_after_comma(reading_line,0);
                        if(temp_user_id != current_user_id)
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


                            writing_line.setLength(0);

                            current_user_id = temp_user_id;
                            movie_ids.clear();
                            movie_scores.clear();
                        }
                        movie_ids.add((int)first_num_after_comma(reading_line,1));
                        movie_scores.add((float)first_num_after_comma(reading_line,2));
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
                //------------------------------------Fill me!!!!!!!!!!!----------------------------------
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
