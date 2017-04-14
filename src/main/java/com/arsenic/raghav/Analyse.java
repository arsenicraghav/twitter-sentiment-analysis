package com.arsenic.raghav;

/**
 * Created by raghav on 14/04/17.
 */
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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

    DoccatModel model;
    static int positiveTweetCount = 0;
    static int negativeTweetCount = 0;

    public static void main(String[] args) throws IOException, TwitterException {

        Analyse analyser = new Analyse();

        /* Train the analyser first */

        analyser.trainAnalyser();

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

         /* query twitter for tweets */

        Query query = new Query("USA");
        QueryResult result = twitter.search(query);


        /* process each tweet from the search result */

        for (Status status : result.getTweets()) {

            if (analyser.checkPositiveOrNegtiveTweet(status.getText()) == 1) {
                positiveTweetCount++;
            } else {
                negativeTweetCount++;
            }
        }

        /* write the final count to the result.csv file */

        BufferedWriter bw = new BufferedWriter(new FileWriter("/Users/raghav/Desktop/TwitterSentiment/results.csv"));
        bw.write("Positive Tweets," + positiveTweetCount);
        bw.newLine();
        bw.write("Negative Tweets," + negativeTweetCount);
        bw.close();
    }

    public void trainAnalyser() {

        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream("/Users/raghav/Desktop/TwitterSentiment/lookupfile.txt");

            ObjectStream lineStream = new PlainTextByLineStream(inputStream, "UTF-8");

            ObjectStream sampleStream = new DocumentSampleStream(lineStream);

            int cutoff = 2;
            int trainingIterations = 20;

            model = DocumentCategorizerME.train("en", sampleStream, cutoff, trainingIterations);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try{
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int checkPositiveOrNegtiveTweet(String tweet) throws IOException {

        DocumentCategorizerME categorizer = new DocumentCategorizerME(model);

        double[] outcomes = categorizer.categorize(tweet);
        String category = categorizer.getBestCategory(outcomes);

        System.out.print("Checking category for the Tweet:" + tweet + " ===> ");

        if (category.equalsIgnoreCase("1")) {
            System.out.println(" Positive Tweet ");
            return 1;
        } else {
            System.out.println(" Negative Tweet ");
            return 0;
        }

    }
}