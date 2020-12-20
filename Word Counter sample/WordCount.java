import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


/* The following class has the implementation of Mapper,Combiner,Reducer and the main method. 
For simplicity, we will use the same class for Reducer and Combiner.
*/
public class WordCount extends Configured implements Tool
{
    /* Extend the custom class with Mapper<IpKey, IpValue, OpKey, OpValue >
    IpKey ->InputKey to mapper class (Object in most cases)
    IpValue ->InputValue to mapper class(Text in most cases)
    OpKey ->OutputKey from mapper class
    (Will be the InputKey for Reducer)OpValue ->OutputValue from mapper class
    Will be InputValue for Reducer)*/
       public static class TokenizerMapper
        extends Mapper<Object, Text, Text, IntWritable>
       {
           //Create new IntWritable object
           private final static IntWritable one = new IntWritable(1);

           //Create new Text object
           private Text word = new Text();

           /*User logic is placed inside map function.Output (Key,value) pair is written 
           to context in every stage,
           context facilitates the data movement fromstage to another stage.
           Eg: from mapper class to reducer class
           */
           public void map(Object key, Text value, Context context)throws IOException, InterruptedException 
           {


               /*Splits strings based on a token. Words separated by spaces in a
                sentence are divided into an array of words here. Iterate through the array of words 
                and assign value '1'to every word. Here word is the key and 1 is the value for the
                corresponding key.*/
                StringTokenizer itr = new StringTokenizer(value.toString());
                while (itr.hasMoreTokens())
                 {
                     word.set(itr.nextToken());

                     // context.write(key,value)
                     context.write(word, one);
                    }
            }
        }





        /* Extend the custom class with Reducer<IpKey, IpValue, OpKey, OpValue >
        IpKey ->InputKey to reducer class (OutputKey from mapper class)
        IpValue ->InputValue to reducer class (OuputValue from mapper class)
        OpKey ->OutputKey from reducer class (Will be written to file)
        OpValue ->OutputValue from reducer class(Will bewritten to file)
        */
          public static class IntSumReducer extends Reducer<Text,IntWritable,Text,IntWritable>
          {
              private IntWritable result = new IntWritable();
              /*User logic is placed inside reducefunction.Output (Key,value) pair is
              written to context.DataType of key in reduce is Text because outputKey from
              mapper is a Text. So the dataType must be changed according to the mapper
              output.DataType of Value is Iterable<IntWritable>because outputValue from mapper
              is a IntWritable. As a particular key can have multiple values,we adopt
              Iterable<InputValue DataType>as the standard.*/
              public void reduce(Text key, Iterable<IntWritable>values,Context context) throws IOException, InterruptedException
              {
                  int sum = 0;
                  //For every key, sum the value
                  for (IntWritable val : values)
                   {sum += val.get();}
                   result.set(sum);
                   context.write(key, result);
                }
            }
            public int run(String[] args) throws Exception {
                // Configuration processed by ToolRunner
                Configuration conf = getConf();
                
                
                
                // Process custom command-line options
                Path in = new Path(args[0]);
                Path out = new Path(args[1]);
                Job job = Job.getInstance(conf, "word count");
                // Set the class name in which main method resides.
                job.setJarByClass(WordCount.class);
                // Set the mapper class namejob.setMapperClass(TokenizerMapper.class);
                //Set the combiner class namejob.setCombinerClass(IntSumReducer.class);
                //Set the reducer class namejob.setReducerClass(IntSumReducer.class);
                // Set the mapper class name
                job.setMapperClass(TokenizerMapper.class);
                //Set the combiner class name
                job.setCombinerClass(IntSumReducer.class);
                //Set the reducer class name
                job.setReducerClass(IntSumReducer.class);
                /*Output from reducer is the final result and Output is Key,Value pair.
                Inform Hadoop about the dataType ofKey and Value from reducer*/
                job.setOutputKeyClass(Text.class); 
                
       
                // Submit the job, then poll for progress until the job is complete
                job.waitForCompletion(true);
                return 0;
              }
            public static void main(String[] args) throws Exception 
            {
                //Create an object of org.apache.hadoop.conf.Configuration
                int res = ToolRunner.run(new Configuration(), new WordCount(), args); 
                System.exit(res);
            }
}

        
                
                 
                
