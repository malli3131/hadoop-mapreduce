/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.examples;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class Search {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		Configuration conf = new Configuration();
		
		String otherArgs[] = new GenericOptionsParser(conf, args).getRemainingArgs();
		
		if(otherArgs.length != 3)
		{
			System.out.println("Usage: search <input> <output> <seach Keyword>");
			System.exit(1);
		}
		
		String query = args[2];
		conf.set("query", query);

		Job job = new Job(conf, "Search the content for particular pattern");

		job.setJarByClass(Search.class);

		job.setMapperClass(MyMapper.class);
		job.setReducerClass(MyReducer.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		System.exit(job.waitForCompletion(true) ? 0 : 1);		
	}

	public static class MyMapper extends Mapper<LongWritable, Text, Text, Text>
	{
		String mySearch = "";
		Text kword = new Text();
		Text vword = new Text();
		public void setup(Context context)
		{
			Configuration myconf = context.getConfiguration();
			mySearch = myconf.get("query");
		}
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			String line = value.toString();
			if(line.contains(mySearch))
			{
				kword.set(mySearch);
				vword.set(line);
				context.write(kword, vword);
			}
		}
	}

	public static class MyReducer extends Reducer<Text, Text, Text, Text>
	{
		Text kword = new Text();
		Text vword = new Text();
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
		{
			int sum = 0;
			String results = "";
			for (Text value : values)
			{
				results = results + value.toString() + "\n";
				sum++;
			}
			String myKey = "For the Query " + key.toString() + " the results found: " +  sum;
			kword.set(myKey);
			vword.set(results);
			context.write(kword, vword);
		}
	}
}
