package headers;

import java.io.*;
import java.util.ArrayList;

public class veri_temizleme
{
    static int start_of_number = 0;
    static int comma_number = 0;
    static void reset_num_after_comma(){start_of_number =0;comma_number=0;}
    static float first_num_after_comma(String line, int TH_comma)
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
    static public int temizle(String main_path,int combination)
        /* main_path is where the cvs files going to read and written, not the to the cvs file itself.
        * combination is the number of how many items is joined, it needs the previous step to be completed
        * (if you want to do 3, you should be already done with step 2)*/
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(main_path + "\\rating"
                    + (combination==0?"": "_" + Integer.toString(combination-1)) + ".csv"));

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

                            System.out.println("writed:" + current_user_id);

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

                        System.out.println("son satıra kadar okudum");
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
