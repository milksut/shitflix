package headers;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.*;

public class recommend
{

    public static LinkedHashSet<Integer>B_T(String path)
    {
        try
        {
            ObjectInputStream reader = new ObjectInputStream(new FileInputStream(path));
            LinkedHashMap<ArrayList<Integer>,String> map = (LinkedHashMap<ArrayList<Integer>,String>) reader.readObject();
            reader.close();

            reader = new ObjectInputStream(new FileInputStream(map.firstEntry().getValue()));
            LinkedHashMap<ArrayList<Integer>,veri_temizleme.movie_obj> movies =
                    (LinkedHashMap<ArrayList<Integer>,veri_temizleme.movie_obj>) reader.readObject();
            reader.close();

            LinkedHashSet<Integer> recommendations = new LinkedHashSet<Integer>(movies.size()+1);
            for (ArrayList<Integer> entry: movies.keySet())
            {
                recommendations.add(entry.getFirst());
            }

            return recommendations;

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static LinkedHashSet<Integer>B_I(String path, int movie_id, String[] tags)
    {
        try
        {
            LinkedHashSet<Integer> recommendations = new LinkedHashSet<>(50);

            for (String tag : tags)
            {
                ObjectInputStream reader = new ObjectInputStream(new FileInputStream(path+"\\popular\\"+tag+"\\rules_2.bin"));
                LinkedHashMap<ArrayList<Integer>,String> rule_map = (LinkedHashMap<ArrayList<Integer>,String>)reader.readObject();
                reader.close();

                ArrayList<Integer> temp = new ArrayList<>(1);
                temp.add(movie_id);
                if(!rule_map.containsKey(temp)){continue;}

                reader = new ObjectInputStream(new FileInputStream(rule_map.get(temp)));
                ArrayList<veri_temizleme.rule> rules = (ArrayList<veri_temizleme.rule>) reader.readObject();
                reader.close();

                for(veri_temizleme.rule x : rules)
                {
                    recommendations.add(x.target.getFirst());
                }
            }

            if(recommendations.isEmpty())
            {
                return B_T(path+"\\popular\\"+tags[0]+"\\rating_1.bin");
            }

            return recommendations;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static LinkedHashSet<Integer>K_T(String path,ArrayList<veri_temizleme.rating> watched_movies, String tag)
    {
        try
        {
            watched_movies.sort(Comparator.comparingInt(r->r.rating));
            ArrayList<Integer> watched_movies2 = new ArrayList<>(watched_movies.size());
            for(veri_temizleme.rating x: watched_movies){watched_movies2.add(x.movie_id);}

            ObjectInputStream reader = new ObjectInputStream(new FileInputStream(path+"\\popular\\"+tag+"\\rules_2.bin"));
            LinkedHashMap<ArrayList<Integer>,String> rule_map = (LinkedHashMap<ArrayList<Integer>,String>) reader.readObject();
            reader.close();

            LinkedHashSet<Integer> recommendations = new LinkedHashSet<>(50);
            for (Integer x:watched_movies2)
            {
                ArrayList<Integer> temp = new ArrayList<>(1);
                temp.add(x);
                if(!rule_map.containsKey(temp)){continue;}

                reader = new ObjectInputStream(new FileInputStream(rule_map.get(temp)));
                ArrayList<veri_temizleme.rule> rules = (ArrayList<veri_temizleme.rule>) reader.readObject();
                reader.close();

                for (veri_temizleme.rule y : rules)
                {
                    if(!watched_movies2.contains(y.target.getFirst())){recommendations.add(y.target.getFirst());}
                }
            }
            if(recommendations.isEmpty())
            {
                return B_T(path+"\\popular\\"+tag+"\\rating_1.bin");
            }
            return recommendations;

        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static LinkedHashSet<Integer>K_I(String path,ArrayList<veri_temizleme.rating> watched_movies,int movie_id,int max_rule_length)
    {
        try
        {
            int max_target =1;
            watched_movies.sort(Comparator.comparingInt(r->r.rating));
            ArrayList<Integer> watched_movies2 = new ArrayList<>(watched_movies.size());
            for(veri_temizleme.rating x: watched_movies){watched_movies2.add(x.movie_id);}

            LinkedHashSet<Integer> recommendations = new LinkedHashSet<>(11);

            HashMap<Integer,LinkedHashMap<ArrayList<Integer>,String>> maps = new HashMap<>(max_rule_length);

            for (int i = max_rule_length; i <= 2;)
            {
                if(!maps.containsKey(i))
                {
                    ObjectInputStream reader = new ObjectInputStream(new FileInputStream(path+"\\rules_"+i+".bin"));
                    maps.put(i,(LinkedHashMap<ArrayList<Integer>,String>) reader.readObject());
                    reader.close();
                }
                for(Map.Entry<ArrayList<Integer>,String> entry : maps.get(i).entrySet())
                {
                    if(entry.getKey().size()>i-max_target){continue;}
                    if(entry.getKey().size()<i-max_target)
                    {
                        if(i<max_rule_length)
                        {
                            i++;
                            max_target++;
                        }
                        else
                        {
                            i = i-max_target;
                            max_target=1;
                        }
                        break;
                    }

                    if(!entry.getKey().contains(movie_id)){continue;}

                    boolean working = false;
                    for(Integer x : entry.getKey())
                    {
                        if(x==movie_id && !watched_movies2.contains(x)){working = true;break;}
                    }

                    if(working){continue;}

                    ObjectInputStream reader = new ObjectInputStream(new FileInputStream(entry.getValue()));
                    ArrayList<veri_temizleme.rule> rules = (ArrayList<veri_temizleme.rule>) reader.readObject();
                    reader.close();

                    for(veri_temizleme.rule x : rules)
                    {
                        for(Integer y : x.target)
                        {
                            if(!watched_movies2.contains(y))
                            {
                                recommendations.add(y);
                                if(recommendations.size()>10)
                                {
                                    return recommendations;
                                }
                            }
                        }
                    }
                }
            }
            return recommendations;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

}
