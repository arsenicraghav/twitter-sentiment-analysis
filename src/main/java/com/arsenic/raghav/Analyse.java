package com.arsenic.raghav;

/**
 * Created by raghav on 14/04/17.
 */
import java.io.*;
import java.util.*;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;


public class Analyse {

    static Map<String, Integer> lookupMap = new HashMap<String, Integer>();

    public static void main(String[] args) throws IOException, TwitterException {

        Analyse analyser = new Analyse();

        /* Load the Lookup in a HashMap first */

        LoadToHashmap();

        System.out.println(lookupMap.size());

        /*

        Credentials from Twitter Developer Console
        =============================================

        Consumer Key (API Key)	yR6WXT37vDXu9yyPzts6rfOtC

        Consumer Secret (API Secret)	bcrjiKPn5fvKkmAXaEvkmIbbz6fPiUXVgMM0Utuxu3UUdE5xUM

        Access Token	3191040134-IT5vqvFrco6sh1GfXavn9rm0mcZYovo9cxaR64q

        Access Token Secret	DTv4LDjNSBnIFnXbioMEJwNPKsBe1iXjRPngUWKbKTGHD

        */


        /* create a configuration object for authentication with twitter */

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();

        configurationBuilder.setDebugEnabled(true)
                .setOAuthConsumerKey("yR6WXT37vDXu9yyPzts6rfOtC")
                .setOAuthConsumerSecret("bcrjiKPn5fvKkmAXaEvkmIbbz6fPiUXVgMM0Utuxu3UUdE5xUM")
                .setOAuthAccessToken("3191040134-IT5vqvFrco6sh1GfXavn9rm0mcZYovo9cxaR64q")
                .setOAuthAccessTokenSecret("DTv4LDjNSBnIFnXbioMEJwNPKsBe1iXjRPngUWKbKTGHD");


         /* create a twitterFactory instance */

        TwitterFactory twitterFactory = new TwitterFactory(configurationBuilder.build());
        Twitter twitter = twitterFactory.getInstance();

        BufferedWriter bw = new BufferedWriter(new FileWriter("/Users/raghav/Desktop/TwitterSentiment/results.csv"));

        /* List of countries to compare */

        List <String> countries =  new ArrayList<String>();

        countries.add("USA");
        countries.add("INDIA");
        countries.add("CANADA");
        countries.add("ENGLAND");
        countries.add("KOREA");
        countries.add("TAIWAN");
        countries.add("RUSSIA");

        Map<String, Integer> countryScore = new HashMap<String, Integer>();

         /* query twitter for tweets for each country */

        for(String country : countries) {

            Query query = new Query(country);
            QueryResult result = twitter.search(query);

            int positiveTweetCount = 0;
            int negativeTweetCount = 0;

        /* process each tweet from the search result */

            for (Status status : result.getTweets()) {

                if (analyser.checkPositiveOrNegtiveTweet(status.getText()) == 1) {
                    positiveTweetCount++;
                } else {
                    negativeTweetCount++;
                }
            }

            countryScore.put(country,negativeTweetCount);

        /* write the final count to the result.csv file */

            bw.write("Country: " + country);
            bw.newLine();
            bw.write("Positive Tweets: " + positiveTweetCount);
            bw.newLine();
            bw.write("Negative Tweets: " + negativeTweetCount);
            bw.newLine();
            bw.newLine();
        }

        /* Let's assume Happiest Country is based on the minimum number of negative tweets(In case of a tie we declare the first one winner */

        int minValueInMap  = Collections.min(countryScore.values());

        for (Map.Entry<String, Integer> entry : countryScore.entrySet()) {

            if (entry.getValue()==minValueInMap) {
                bw.newLine();
                bw.write("Happiest Country is: " + entry.getKey());
                bw.newLine();
                break;
            }
        }

        bw.close();
    }

    /* Function to check negative or positive tweet */

    public int checkPositiveOrNegtiveTweet(String tweet) throws IOException {

        System.out.println("Checking category for the tweetWord:" + tweet + " ===> ");

        int tweetWeight = 0;

        String tweetSplit[] = tweet.split("\\s+");

        for (String tweetWord : tweetSplit) {

            if(lookupMap.containsKey(tweetWord)) {

                int weight = lookupMap.get(tweetWord) ;
                tweetWeight = tweetWeight + weight ;
            }
        }

        System.out.println(tweetWeight);

        if (tweetWeight > 0) {
            System.out.println(" Positive Tweet ");
            return 1;
        } else {
            System.out.println(" Negative Tweet ");
            return 0;
        }
    }

    /* Function to load lookup file in a HashMap */

    public static void LoadToHashmap() throws FileNotFoundException {

        Scanner scanner = new Scanner(new FileReader("/Users/raghav/Desktop/TwitterSentiment/lookupfile.txt"));

        while (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            String lineSplit[] = line.split("\\s+");

            try {
                Integer value = Integer.parseInt(lineSplit[1]);
                lookupMap.put(lineSplit[0], value);
            }catch(Exception e){
            }

        }
    }
}